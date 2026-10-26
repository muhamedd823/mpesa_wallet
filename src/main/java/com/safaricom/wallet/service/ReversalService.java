package com.safaricom.wallet.service;

import com.safaricom.wallet.dto.ReversalRequest;
import com.safaricom.wallet.dto.ReversalResponse;
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
public class ReversalService {

    private final TransferRepository transferRepo;
    private final WalletRepository walletRepo;

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public ReversalResponse reverse(String transferId, ReversalRequest req) {

        Transfer original = transferRepo.findById(transferId)
                .orElseThrow(() -> WalletException.notFound("TRANSFER_NOT_FOUND", transferId + " not found"));

        if (original.getStatus() != Transfer.TransferStatus.COMPLETED) {
            throw WalletException.unprocessable("NOT_REVERSIBLE", transferId + " is not COMPLETED");
        }

        if (transferRepo.findReversalOf(transferId).isPresent()) {
            throw WalletException.conflict("ALREADY_REVERSED", transferId + " has already been reversed");
        }

        String debitId  = original.getToWalletId();
        String creditId = original.getFromWalletId();

        String firstId  = debitId.compareTo(creditId) < 0 ? debitId : creditId;
        String secondId = firstId.equals(debitId) ? creditId : debitId;

        Wallet first  = walletRepo.findByIdForUpdate(firstId)
                .orElseThrow(() -> WalletException.notFound("WALLET_NOT_FOUND", firstId + " not found"));
        Wallet second = walletRepo.findByIdForUpdate(secondId)
                .orElseThrow(() -> WalletException.notFound("WALLET_NOT_FOUND", secondId + " not found"));

        Wallet debit  = firstId.equals(debitId) ? first : second;
        Wallet credit = firstId.equals(debitId) ? second : first;

        if (debit.getStatus() != Wallet.WalletStatus.ACTIVE) {
            throw WalletException.unprocessable("WALLET_NOT_ACTIVE", debit.getWalletId() + " is not ACTIVE");
        }

        if (debit.getAvailable() < original.getAmount()) {
            throw WalletException.unprocessable("INSUFFICIENT_FUNDS",
                    debit.getWalletId() + " available " + debit.getAvailable() + ", requested " + original.getAmount());
        }

        Instant now = Instant.now();
        debit.setBalance(debit.getBalance() - original.getAmount());
        debit.setUpdatedAt(now);
        credit.setBalance(credit.getBalance() + original.getAmount());
        credit.setUpdatedAt(now);

        original.setStatus(Transfer.TransferStatus.REVERSED);

        Transfer reversal = new Transfer();
        reversal.setTransferId("R-" + UUID.randomUUID().toString().replace("-", "").substring(0, 10));
        reversal.setFromWalletId(debitId);
        reversal.setToWalletId(creditId);
        reversal.setAmount(original.getAmount());
        reversal.setReference(req.reason());
        reversal.setStatus(Transfer.TransferStatus.COMPLETED);
        reversal.setReversalOf(transferId);
        reversal.setCreatedAt(now);

        transferRepo.save(reversal);

        return new ReversalResponse(transferId, reversal.getTransferId(), "REVERSED", original.getAmount());
    }
}
