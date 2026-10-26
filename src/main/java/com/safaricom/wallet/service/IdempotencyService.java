package com.safaricom.wallet.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.safaricom.wallet.entity.IdempotencyRecord;
import com.safaricom.wallet.exception.WalletException;
import com.safaricom.wallet.repository.IdempotencyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Optional;
import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
public class IdempotencyService {

    private final IdempotencyRepository repo;
    private final ObjectMapper mapper;

    // Runs the supplier exactly once per key.
    // Concurrent calls with the same key: second caller gets 409 REQUEST_IN_PROGRESS
    // until the first finishes, then gets the cached response.
    public <T> ResponseEntity<T> execute(String key, String bodyJson, Supplier<ResponseEntity<T>> action) {
        String hash = sha256(bodyJson);
        IdempotencyRecord record = getOrCreate(key, hash);

        if (!record.getRequestHash().equals(hash)) {
            throw WalletException.conflict("IDEMPOTENCY_CONFLICT",
                    "Idempotency key reused with a different request body");
        }

        if (record.isCompleted()) {
            return deserializeResponse(record);
        }

        // Another instance/thread is still processing this key
        throw WalletException.conflict("REQUEST_IN_PROGRESS",
                "A request with this key is already in progress");
    }

    // Separate transaction so the row is visible to other instances immediately
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public IdempotencyRecord getOrCreate(String key, String hash) {
        Optional<IdempotencyRecord> existing = repo.findByIdemKey(key);
        if (existing.isPresent()) return existing.get();

        IdempotencyRecord r = new IdempotencyRecord();
        r.setIdemKey(key);
        r.setRequestHash(hash);
        r.setCompleted(false);
        r.setCreatedAt(Instant.now());
        r.setExpiresAt(Instant.now().plusSeconds(86_400)); // 24 h
        try {
            return repo.saveAndFlush(r);
        } catch (DataIntegrityViolationException e) {
            // Lost the race — another instance inserted first
            return repo.findByIdemKey(key).orElseThrow();
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void complete(String key, int status, Object body) {
        repo.findByIdemKey(key).ifPresent(r -> {
            try {
                r.setResponseStatus(status);
                r.setResponseBody(mapper.writeValueAsString(body));
                r.setCompleted(true);
                repo.save(r);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    @SuppressWarnings("unchecked")
    private <T> ResponseEntity<T> deserializeResponse(IdempotencyRecord r) {
        try {
            T body = (T) mapper.readValue(r.getResponseBody(), Object.class);
            return ResponseEntity.status(r.getResponseStatus()).body(body);
        } catch (Exception e) {
            throw new RuntimeException("Failed to deserialize cached response", e);
        }
    }

    private static String sha256(String input) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // Purge expired keys — lightweight, runs every hour
    @Scheduled(fixedDelay = 3_600_000)
    @Transactional
    public void purgeExpired() {
        repo.deleteExpired(Instant.now());
    }
}
