package com.safaricom.wallet.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TransferRequest(
        @NotBlank String from_wallet,
        @NotBlank String to_wallet,
        @Min(1) @Max(5_000_000) long amount,
        @NotBlank @Size(min = 1, max = 64) String reference
) {}
