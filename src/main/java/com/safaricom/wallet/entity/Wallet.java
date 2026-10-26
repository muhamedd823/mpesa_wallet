package com.safaricom.wallet.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "wallets")
@Getter
@Setter
public class Wallet {

    @Id
    @Column(name = "wallet_id", length = 20)
    private String walletId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private WalletStatus status;

    @Column(nullable = false)
    private long balance;

    @Column(nullable = false)
    private long reserved;

    @Column(nullable = false)
    private boolean merchant;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    private long version;

    public long getAvailable() {
        return balance - reserved;
    }

    public enum WalletStatus {
        ACTIVE, FROZEN
    }
}
