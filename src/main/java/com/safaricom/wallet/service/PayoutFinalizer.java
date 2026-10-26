package com.safaricom.wallet.service;

import com.safaricom.wallet.entity.Payout;
import com.safaricom.wallet.repository.PayoutRepository;
import com.safaricom.wallet.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class PayoutFinalizer {

    private final PayoutRepository payoutRepo;
    private final WalletRepository walletRepo;

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public boolean finalize(String payoutId, boolean succeeded, String bankRef) {
        Payout payout = payoutRepo.findByIdForUpdate(payoutId).orElse(null);
        if (payout == null) return false;

        if (payout.getStatus() == Payout.PayoutStatus.SUCCEEDED
                || payout.getStatus() == Payout.PayoutStatus.FAILED) {
            boolean conflict = (succeeded && payout.getStatus() == Payout.PayoutStatus.FAILED)
                    || (!succeeded && payout.getStatus() == Payout.PayoutStatus.SUCCEEDED);
            if (conflict) {
                payout.setConflict(true);
                payout.setUpdatedAt(Instant.now());
            }
            return true;
        }

        walletRepo.findByIdForUpdate(payout.getWalletId()).ifPresent(wallet -> {
            wallet.setReserved(wallet.getReserved() - payout.getAmount());
            if (succeeded) {
                wallet.setBalance(wallet.getBalance() - payout.getAmount());
            }
            wallet.setUpdatedAt(Instant.now());
        });

        payout.setStatus(succeeded ? Payout.PayoutStatus.SUCCEEDED : Payout.PayoutStatus.FAILED);
        if (bankRef != null) payout.setBankRef(bankRef);
        payout.setUpdatedAt(Instant.now());

        return true;
    }
}
