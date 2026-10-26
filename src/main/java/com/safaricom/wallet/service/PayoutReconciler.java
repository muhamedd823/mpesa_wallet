package com.safaricom.wallet.service;

import com.safaricom.wallet.entity.Payout;
import com.safaricom.wallet.repository.PayoutRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PayoutReconciler {

    private final PayoutRepository payoutRepo;
    private final BankClient bankClient;
    private final PayoutFinalizer finalizer;

    @Scheduled(fixedDelay = 20_000)
    public void reconcile() {
        Instant threshold = Instant.now().minusSeconds(30);
        List<Payout> stale = payoutRepo.findStale(threshold);

        for (Payout payout : stale) {
            String bankStatus = bankClient.queryStatus(payout.getPayoutId());
            switch (bankStatus) {
                case "SUCCEEDED" -> finalizer.finalize(payout.getPayoutId(), true, null);
                case "FAILED", "REJECTED", "NOT_FOUND" -> finalizer.finalize(payout.getPayoutId(), false, null);
            }
        }
    }
}
