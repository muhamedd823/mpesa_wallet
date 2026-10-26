package com.safaricom.wallet.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "payouts")
@Getter
@Setter
public class Payout {

    @Id
    @Column(name = "payout_id", length = 30)
    private String payoutId;

    @Column(name = "wallet_id", nullable = false, length = 20)
    private String walletId;

    @Column(name = "bank_account", nullable = false, length = 50)
    private String bankAccount;

    // Santim — integer
    @Column(nullable = false)
    private long amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private PayoutStatus status;

    // bank_ref returned by the bank on ACCEPTED
    @Column(name = "bank_ref", length = 50)
    private String bankRef;

    // Set true when a callback contradicts a recorded final status
    @Column(nullable = false)
    private boolean conflict;

    @Column(name = "idempotency_key", length = 64)
    private String idempotencyKey;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public enum PayoutStatus {
        // Public statuses
        PENDING, SUCCEEDED, FAILED,
        // Internal: bank call sent, awaiting callback or reconciliation
        SUBMITTED,
        // Internal: bank timed out — outcome unknown, reconciler will resolve
        UNKNOWN
    }
}
