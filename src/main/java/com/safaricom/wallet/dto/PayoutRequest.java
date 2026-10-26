package com.safaricom.wallet.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PayoutRequest(
        @NotBlank String wallet_id,
        @NotBlank @Size(max = 50) String bank_account,
        @Min(1) @Max(5_000_000) long amount
) {}
