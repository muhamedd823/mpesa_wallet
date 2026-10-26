package com.safaricom.wallet.dto;

import java.time.Instant;

public record TransferResponse(
        String transfer_id,
        String status,
        long from_available_after,
        Instant created_at
) {}
