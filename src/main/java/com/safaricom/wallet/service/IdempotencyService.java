package com.safaricom.wallet.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.safaricom.wallet.entity.IdempotencyRecord;
import com.safaricom.wallet.exception.WalletException;
import com.safaricom.wallet.repository.IdempotencyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class IdempotencyService {

    private final IdempotencyRepository repo;
    private final ObjectMapper mapper;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public IdempotencyRecord getOrCreate(String key, String hash) {
        Optional<IdempotencyRecord> existing = repo.findByIdemKey(key);
        if (existing.isPresent()) return existing.get();

        IdempotencyRecord r = new IdempotencyRecord();
        r.setIdemKey(key);
        r.setRequestHash(hash);
        r.setCompleted(false);
        r.setCreatedAt(Instant.now());
        r.setExpiresAt(Instant.now().plusSeconds(86_400));
        try {
            return repo.saveAndFlush(r);
        } catch (DataIntegrityViolationException e) {
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

    public static String sha256(String input) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Scheduled(fixedDelay = 3_600_000)
    @Transactional
    public void purgeExpired() {
        repo.deleteExpired(Instant.now());
    }
}
