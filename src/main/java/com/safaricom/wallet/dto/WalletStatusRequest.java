package com.safaricom.wallet.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record WalletStatusRequest(
        @NotNull @Pattern(regexp = "ACTIVE|FROZEN") String status,
        String reason
) {}
