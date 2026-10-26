package com.safaricom.wallet.service;

import com.safaricom.wallet.dto.TransferRequest;
import com.safaricom.wallet.dto.TransferResponse;
import com.safaricom.wallet.entity.Transfer;
import com.safaricom.wallet.entity.Wallet;
import com.safaricom.wallet.exception.WalletException;
import com.safaricom.wallet.repository.TransferRepository;
import com.safaricom.wallet.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransferService {

    private final WalletRepository walletRepo;
    private final TransferRepository transferRepo;

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public TransferResponse transfer(TransferRequest req, String idempotencyKey) {

        if (req.from_wallet().equals(req.to_wallet())) {
            throw WalletException.unprocessable("SAME_WALLET", "from_wallet and to_wallet must differ");
        }

        String firstId  = req.from_wallet().compareTo(req.to_wallet()) < 0 ? req.from_wallet() : req.to_wallet();
        String secondId = firstId.equals(req.from_wallet()) ? req.to_wallet() : req.from_wallet();

        Wallet first  = walletRepo.findByIdForUpdate(firstId)
                .orElseThrow(() -> WalletException.notFound("WALLET_NOT_FOUND", firstId + " not found"));
        Wallet second = walletRepo.findByIdForUpdate(secondId)
                .orElseThrow(() -> WalletException.notFound("WALLET_NOT_FOUND", secondId + " not found"));

        Wallet from = firstId.equals(req.from_wallet()) ? first : second;
        Wallet to   = firstId.equals(req.from_wallet()) ? second : first;

        if (from.getStatus() != Wallet.WalletStatus.ACTIVE) {
            throw WalletException.unprocessable("WALLET_NOT_ACTIVE", from.getWalletId() + " is not ACTIVE");
        }
        if (to.getStatus() != Wallet.WalletStatus.ACTIVE) {
            throw WalletException.unprocessable("WALLET_NOT_ACTIVE", to.getWalletId() + " is not ACTIVE");
        }
        if (from.getAvailable() < req.amount()) {
            throw WalletException.unprocessable("INSUFFICIENT_FUNDS",
                    from.getWalletId() + " available " + from.getAvailable() + ", requested " + req.amount());
        }

        Instant now = Instant.now();
        from.setBalance(from.getBalance() - req.amount());
        from.setUpdatedAt(now);
        to.setBalance(to.getBalance() + req.amount());
        to.setUpdatedAt(now);

        Transfer transfer = new Transfer();
        transfer.setTransferId("T-" + UUID.randomUUID().toString().replace("-", "").substring(0, 10));
        transfer.setFromWalletId(from.getWalletId());
        transfer.setToWalletId(to.getWalletId());
        transfer.setAmount(req.amount());
        transfer.setReference(req.reference());
        transfer.setStatus(Transfer.TransferStatus.COMPLETED);
        transfer.setIdempotencyKey(idempotencyKey);
        transfer.setCreatedAt(now);

        transferRepo.save(transfer);

        return new TransferResponse(
                transfer.getTransferId(),
                "COMPLETED",
                from.getAvailable(),
                now
        );
    }
}
