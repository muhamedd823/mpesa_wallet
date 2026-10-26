package com.safaricom.wallet.controller;

import com.safaricom.wallet.dto.CallbackRequest;
import com.safaricom.wallet.exception.WalletException;
import com.safaricom.wallet.service.PayoutFinalizer;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/mpesa/v1/callbacks")
@RequiredArgsConstructor
public class CallbackController {

    private final PayoutFinalizer finalizer;

    @PostMapping("/bank")
    public ResponseEntity<Void> handle(@Valid @RequestBody CallbackRequest req) {
        boolean found = finalizer.finalize(
                req.payout_id(),
                "SUCCEEDED".equals(req.status()),
                req.bank_ref()
        );
        if (!found) {
            throw WalletException.notFound("PAYOUT_NOT_FOUND", req.payout_id() + " not found");
        }
        return ResponseEntity.ok().build();
    }
}
