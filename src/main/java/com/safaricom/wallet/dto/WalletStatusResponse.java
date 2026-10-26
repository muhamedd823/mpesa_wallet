package com.safaricom.wallet.dto;

import java.time.Instant;

public record WalletStatusResponse(
        String wallet_id,
        String status,
        Instant changed_at
) {}
