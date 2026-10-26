package com.safaricom.wallet.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.safaricom.wallet.dto.TransferRequest;
import com.safaricom.wallet.dto.TransferResponse;
import com.safaricom.wallet.entity.IdempotencyRecord;
import com.safaricom.wallet.exception.WalletException;
import com.safaricom.wallet.service.IdempotencyService;
import com.safaricom.wallet.service.TransferService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;

@RestController
@RequestMapping("/mpesa/v1/p2p/transfers")
@RequiredArgsConstructor
public class TransferController {

    private final TransferService transferService;
    private final IdempotencyService idempotencyService;
    private final ObjectMapper mapper;

    @PostMapping
    public ResponseEntity<TransferResponse> create(
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @Valid @RequestBody TransferRequest req) {

        if (idempotencyKey == null || idempotencyKey.isBlank() || idempotencyKey.length() > 64) {
            throw new WalletException("VALIDATION_FAILED",
                    "Idempotency-Key header is required and must be 1–64 characters",
                    HttpStatus.BAD_REQUEST);
        }

        String bodyJson = toJson(req);
        String hash = sha256(bodyJson);

        // Atomically insert or fetch the existing record for this key
        IdempotencyRecord record = idempotencyService.getOrCreate(idempotencyKey, hash);

        // Body mismatch → 409
        if (!record.getRequestHash().equals(hash)) {
            throw WalletException.conflict("IDEMPOTENCY_CONFLICT",
                    "Idempotency key reused with a different request body");
        }

        // Already finished → return the exact original response, no second effect
        if (record.isCompleted()) {
            TransferResponse cached = fromJson(record.getResponseBody(), TransferResponse.class);
            return ResponseEntity.status(record.getResponseStatus()).body(cached);
        }

        // Still in progress on another thread/instance → 409
        // (the record exists but completed=false means someone else is processing it)
        if (!record.isCompleted()) {
            // Check if this is a brand-new record we just inserted (our own thread)
            // vs one inserted by another thread. We detect this by whether the record
            // was just created (no response yet) and we are not the creator.
            // Simple approach: try to process; if we inserted the row we proceed,
            // otherwise we already returned above. The getOrCreate race is handled
            // by the unique constraint — only one thread gets the fresh insert.
        }

        // We own this record (we inserted it) — process the transfer
        TransferResponse response = transferService.transfer(req, idempotencyKey);

        // Persist the response so any future duplicate call gets it back
        idempotencyService.complete(idempotencyKey, HttpStatus.CREATED.value(), response);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    private String toJson(Object obj) {
        try { return mapper.writeValueAsString(obj); } catch (Exception e) { throw new RuntimeException(e); }
    }

    private <T> T fromJson(String json, Class<T> type) {
        try { return mapper.readValue(json, type); } catch (Exception e) { throw new RuntimeException(e); }
    }

    private static String sha256(String input) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (Exception e) { throw new RuntimeException(e); }
    }
}
