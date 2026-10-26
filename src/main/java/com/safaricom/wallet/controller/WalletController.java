package com.safaricom.wallet.controller;

import com.safaricom.wallet.dto.WalletResponse;
import com.safaricom.wallet.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/mpesa/v1/wallets")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;

    /**
     * GET /mpesa/v1/wallets/{id}
     *
     * Sample request:
     *   GET http://localhost:8081/mpesa/v1/wallets/W001
     *
     * Sample response (200 OK):
     *   {
     *     "wallet_id":  "W001",
     *     "status":     "ACTIVE",
     *     "balance":    1000000,
     *     "reserved":   0,
     *     "available":  1000000
     *   }
     */
    @GetMapping("/{id}")
    public ResponseEntity<WalletResponse> getWallet(@PathVariable String id) {
        return ResponseEntity.ok(walletService.getWallet(id));
    }
}
