package com.safaricom.wallet.dto;

import jakarta.validation.constraints.NotBlank;

public record ReversalRequest(@NotBlank String reason) {}
