package com.safaricom.wallet.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.safaricom.wallet.dto.PayoutRequest;
import com.safaricom.wallet.dto.PayoutResponse;
import com.safaricom.wallet.entity.IdempotencyRecord;
import com.safaricom.wallet.exception.WalletException;
import com.safaricom.wallet.service.IdempotencyService;
import com.safaricom.wallet.service.PayoutService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;

@RestController
@RequestMapping("/mpesa/v1/payouts")
@RequiredArgsConstructor
public class PayoutController {

    private final PayoutService payoutService;
    private final IdempotencyService idempotencyService;
    private final ObjectMapper mapper;

    @PostMapping
    public ResponseEntity<PayoutResponse.Accepted> create(
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @Valid @RequestBody PayoutRequest req) {

        if (idempotencyKey == null || idempotencyKey.isBlank() || idempotencyKey.length() > 64) {
            throw new WalletException("VALIDATION_FAILED",
                    "Idempotency-Key header is required and must be 1-64 characters",
                    HttpStatus.BAD_REQUEST);
        }

        String bodyJson = toJson(req);
        String hash = sha256(bodyJson);

        IdempotencyRecord record = idempotencyService.getOrCreate(idempotencyKey, hash);

        if (!record.getRequestHash().equals(hash)) {
            throw WalletException.conflict("IDEMPOTENCY_CONFLICT",
                    "Idempotency key reused with a different request body");
        }

        if (record.isCompleted()) {
            PayoutResponse.Accepted cached = fromJson(record.getResponseBody(), PayoutResponse.Accepted.class);
            return ResponseEntity.status(record.getResponseStatus()).body(cached);
        }

        PayoutResponse.Accepted response = payoutService.reserve(req, idempotencyKey);
        idempotencyService.complete(idempotencyKey, HttpStatus.ACCEPTED.value(), response);
        payoutService.submitAsync(response.payout_id(), req.bank_account(), req.amount());

        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PayoutResponse.Detail> get(@PathVariable String id) {
        return ResponseEntity.ok(payoutService.get(id));
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
