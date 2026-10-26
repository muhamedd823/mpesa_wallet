package com.safaricom.wallet.service;

import com.safaricom.wallet.entity.Wallet;
import com.safaricom.wallet.repository.IdempotencyRepository;
import com.safaricom.wallet.repository.PayoutRepository;
import com.safaricom.wallet.repository.TransferRepository;
import com.safaricom.wallet.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SeedService {

    private final WalletRepository walletRepo;
    private final TransferRepository transferRepo;
    private final PayoutRepository payoutRepo;
    private final IdempotencyRepository idempotencyRepo;

    @Transactional
    public void reset() {
        idempotencyRepo.deleteAll();
        transferRepo.deleteAll();
        payoutRepo.deleteAll();
        walletRepo.deleteAll();

        List<Wallet> wallets = new ArrayList<>();
        Instant now = Instant.now();

        for (int i = 1; i <= 100; i++) {
            wallets.add(build(String.format("W%03d", i), Wallet.WalletStatus.ACTIVE, 1_000_000, false, now));
        }
        for (int i = 101; i <= 105; i++) {
            wallets.add(build(String.format("W%03d", i), Wallet.WalletStatus.FROZEN, 1_000_000, false, now));
        }
        wallets.add(build("M001", Wallet.WalletStatus.ACTIVE, 50_000_000, true, now));

        walletRepo.saveAll(wallets);
    }

    private Wallet build(String id, Wallet.WalletStatus status, long balance, boolean merchant, Instant now) {
        Wallet w = new Wallet();
        w.setWalletId(id);
        w.setStatus(status);
        w.setBalance(balance);
        w.setReserved(0);
        w.setMerchant(merchant);
        w.setUpdatedAt(now);
        return w;
    }
}
