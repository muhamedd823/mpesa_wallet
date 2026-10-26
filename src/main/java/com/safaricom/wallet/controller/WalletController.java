package com.safaricom.wallet.controller;

import com.safaricom.wallet.dto.WalletResponse;
import com.safaricom.wallet.dto.WalletStatusRequest;
import com.safaricom.wallet.dto.WalletStatusResponse;
import com.safaricom.wallet.service.WalletService;
import com.safaricom.wallet.service.WalletStatusService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/mpesa/v1/wallets")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;
    private final WalletStatusService walletStatusService;

    @GetMapping("/{id}")
    public ResponseEntity<WalletResponse> getWallet(@PathVariable String id) {
        return ResponseEntity.ok(walletService.getWallet(id));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<WalletStatusResponse> setStatus(
            @PathVariable String id,
            @Valid @RequestBody WalletStatusRequest req) {
        return ResponseEntity.ok(walletStatusService.setStatus(id, req));
    }
}
