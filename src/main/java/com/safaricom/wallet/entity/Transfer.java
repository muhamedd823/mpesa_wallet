package com.safaricom.wallet.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "transfers")
@Getter
@Setter
public class Transfer {

    @Id
    @Column(name = "transfer_id", length = 30)
    private String transferId;

    @Column(name = "from_wallet_id", nullable = false, length = 20)
    private String fromWalletId;

    @Column(name = "to_wallet_id", nullable = false, length = 20)
    private String toWalletId;

    @Column(nullable = false)
    private long amount;

    @Column(length = 64)
    private String reference;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private TransferStatus status;

    @Column(name = "reversal_of", length = 30)
    private String reversalOf;

    @Column(name = "idempotency_key", length = 64)
    private String idempotencyKey;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public enum TransferStatus {
        COMPLETED, REVERSED
    }
}
