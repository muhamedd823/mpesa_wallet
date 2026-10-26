package com.safaricom.wallet.dto;

import jakarta.validation.constraints.NotBlank;

public record CallbackRequest(
        @NotBlank String event_id,
        @NotBlank String payout_id,
        String bank_ref,
        @NotBlank String status
) {}
