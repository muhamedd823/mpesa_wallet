package com.safaricom.wallet.service;

import com.safaricom.wallet.entity.Payout;
import com.safaricom.wallet.repository.PayoutRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class BankSubmitter {

    private final PayoutRepository payoutRepo;
    private final BankClient bankClient;
    private final PayoutFinalizer finalizer;

    @Async("payoutExecutor")
    public void submit(String payoutId, String bankAccount, long amount) {
        markSubmitted(payoutId);
        BankClient.BankSubmitResult result = bankClient.submit(payoutId, bankAccount, amount);
        switch (result.outcome()) {
            case ACCEPTED -> markAccepted(payoutId, result.bankRef());
            case REJECTED -> finalizer.finalize(payoutId, false, null);
            case UNKNOWN -> markUnknown(payoutId);
        }
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void markSubmitted(String payoutId) {
        payoutRepo.findByIdForUpdate(payoutId).ifPresent(p -> {
            if (p.getStatus() == Payout.PayoutStatus.PENDING) {
                p.setStatus(Payout.PayoutStatus.SUBMITTED);
                p.setUpdatedAt(Instant.now());
            }
        });
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void markAccepted(String payoutId, String bankRef) {
        payoutRepo.findByIdForUpdate(payoutId).ifPresent(p -> {
            if (p.getStatus() == Payout.PayoutStatus.SUBMITTED) {
                p.setBankRef(bankRef);
                p.setUpdatedAt(Instant.now());
            }
        });
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void markUnknown(String payoutId) {
        payoutRepo.findByIdForUpdate(payoutId).ifPresent(p -> {
            if (p.getStatus() == Payout.PayoutStatus.SUBMITTED) {
                p.setStatus(Payout.PayoutStatus.UNKNOWN);
                p.setUpdatedAt(Instant.now());
            }
        });
    }
}
