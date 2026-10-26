package com.safaricom.wallet.dto;

public record ReversalResponse(
        String transfer_id,
        String reversal_id,
        String status,
        long amount
) {}
