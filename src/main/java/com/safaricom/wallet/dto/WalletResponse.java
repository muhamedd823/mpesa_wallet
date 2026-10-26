package com.safaricom.wallet.dto;

public record WalletResponse(
        String wallet_id,
        String status,
        long balance,
        long reserved,
        long available
) {}
