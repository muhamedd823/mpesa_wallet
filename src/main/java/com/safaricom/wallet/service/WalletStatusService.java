package com.safaricom.wallet.service;

import com.safaricom.wallet.dto.WalletStatusRequest;
import com.safaricom.wallet.dto.WalletStatusResponse;
import com.safaricom.wallet.entity.Wallet;
import com.safaricom.wallet.exception.WalletException;
import com.safaricom.wallet.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class WalletStatusService {

    private final WalletRepository walletRepo;

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public WalletStatusResponse setStatus(String walletId, WalletStatusRequest req) {

        Wallet wallet = walletRepo.findByIdForUpdate(walletId)
                .orElseThrow(() -> WalletException.notFound("WALLET_NOT_FOUND", walletId + " not found"));

        Wallet.WalletStatus requested = Wallet.WalletStatus.valueOf(req.status());

        Instant now = Instant.now();

        if (wallet.getStatus() != requested) {
            wallet.setStatus(requested);
            wallet.setUpdatedAt(now);
        }

        return new WalletStatusResponse(walletId, wallet.getStatus().name(), now);
    }
}
