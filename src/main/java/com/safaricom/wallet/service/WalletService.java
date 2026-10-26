package com.safaricom.wallet.service;

import com.safaricom.wallet.dto.WalletResponse;
import com.safaricom.wallet.exception.WalletException;
import com.safaricom.wallet.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WalletService {

    private final WalletRepository walletRepo;

    @Transactional(readOnly = true)
    public WalletResponse getWallet(String walletId) {
        return walletRepo.findById(walletId)
                .map(w -> new WalletResponse(
                        w.getWalletId(),
                        w.getStatus().name(),
                        w.getBalance(),
                        w.getReserved(),
                        w.getAvailable()   // always balance − reserved, never negative
                ))
                .orElseThrow(() -> WalletException.notFound("WALLET_NOT_FOUND", walletId + " not found"));
    }
}
