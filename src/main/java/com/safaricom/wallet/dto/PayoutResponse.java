package com.safaricom.wallet.dto;

import java.time.Instant;

public class PayoutResponse {

    public record Accepted(String payout_id, String status) {}

    public record Detail(
            String payout_id,
            String wallet_id,
            long amount,
            String status,
            String bank_ref,
            boolean conflict,
            Instant updated_at
    ) {}
}
