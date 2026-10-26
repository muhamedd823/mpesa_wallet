package com.safaricom.wallet.service;

import com.safaricom.wallet.dto.PayoutRequest;
import com.safaricom.wallet.dto.PayoutResponse;
import com.safaricom.wallet.entity.Payout;
import com.safaricom.wallet.entity.Wallet;
import com.safaricom.wallet.exception.WalletException;
import com.safaricom.wallet.repository.PayoutRepository;
import com.safaricom.wallet.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PayoutService {

    private final WalletRepository walletRepo;
    private final PayoutRepository payoutRepo;
    private final BankSubmitter bankSubmitter;

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public PayoutResponse.Accepted reserve(PayoutRequest req, String idempotencyKey) {

        Wallet wallet = walletRepo.findByIdForUpdate(req.wallet_id())
                .orElseThrow(() -> WalletException.notFound("WALLET_NOT_FOUND", req.wallet_id() + " not found"));

        if (wallet.getStatus() != Wallet.WalletStatus.ACTIVE) {
            throw WalletException.unprocessable("WALLET_NOT_ACTIVE", req.wallet_id() + " is not ACTIVE");
        }

        if (wallet.getAvailable() < req.amount()) {
            throw WalletException.unprocessable("INSUFFICIENT_FUNDS",
                    req.wallet_id() + " available " + wallet.getAvailable() + ", requested " + req.amount());
        }

        wallet.setReserved(wallet.getReserved() + req.amount());
        wallet.setUpdatedAt(Instant.now());

        Payout payout = new Payout();
        payout.setPayoutId("P-" + UUID.randomUUID().toString().replace("-", "").substring(0, 10));
        payout.setWalletId(req.wallet_id());
        payout.setBankAccount(req.bank_account());
        payout.setAmount(req.amount());
        payout.setStatus(Payout.PayoutStatus.PENDING);
        payout.setConflict(false);
        payout.setIdempotencyKey(idempotencyKey);
        payout.setCreatedAt(Instant.now());
        payout.setUpdatedAt(Instant.now());

        payoutRepo.save(payout);

        return new PayoutResponse.Accepted(payout.getPayoutId(), "PENDING");
    }

    public void submitAsync(String payoutId, String bankAccount, long amount) {
        bankSubmitter.submit(payoutId, bankAccount, amount);
    }

    @Transactional(readOnly = true)
    public PayoutResponse.Detail get(String payoutId) {
        Payout p = payoutRepo.findById(payoutId)
                .orElseThrow(() -> WalletException.notFound("PAYOUT_NOT_FOUND", payoutId + " not found"));

        String publicStatus = switch (p.getStatus()) {
            case SUBMITTED, UNKNOWN -> "PENDING";
            default -> p.getStatus().name();
        };

        return new PayoutResponse.Detail(
                p.getPayoutId(),
                p.getWalletId(),
                p.getAmount(),
                publicStatus,
                p.getBankRef(),
                p.isConflict(),
                p.getUpdatedAt()
        );
    }
}
