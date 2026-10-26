package com.safaricom.wallet.controller;

import com.safaricom.wallet.service.SeedService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/mpesa/internal")
@RequiredArgsConstructor
public class InternalController {

    private final SeedService seedService;

    @PostMapping("/reset")
    public ResponseEntity<Void> reset() {
        seedService.reset();
        return ResponseEntity.ok().build();
    }
}
