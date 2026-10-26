package com.safaricom.wallet.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "idempotency_records",
       indexes = @Index(name = "idx_idem_key", columnList = "idem_key", unique = true))
@Getter
@Setter
public class IdempotencyRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    // The Idempotency-Key header value
    @Column(name = "idem_key", nullable = false, length = 64, unique = true)
    private String idemKey;

    // SHA-256 of the canonical request body — detects body mismatch (409 IDEMPOTENCY_CONFLICT)
    @Column(name = "request_hash", nullable = false, length = 64)
    private String requestHash;

    // Cached HTTP status code of the original response
    @Column(name = "response_status")
    private Integer responseStatus;

    // Cached JSON response body
    @Column(name = "response_body", columnDefinition = "TEXT")
    private String responseBody;

    // True once the original request has finished and the response is stored
    @Column(nullable = false)
    private boolean completed;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;
}
