package com.safaricom.wallet.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.Map;

@Component
public class BankClient {

    private final RestTemplate http;

    @Value("${bank.url:http://localhost:9090}")
    private String bankUrl;

    public BankClient(RestTemplateBuilder builder) {
        this.http = builder
                .setConnectTimeout(Duration.ofSeconds(3))
                .setReadTimeout(Duration.ofSeconds(8))
                .build();
    }

    public BankSubmitResult submit(String payoutId, String bankAccount, long amount) {
        try {
            Map<String, Object> body = Map.of(
                    "payout_id", payoutId,
                    "bank_account", bankAccount,
                    "amount", amount
            );
            ResponseEntity<Map> resp = http.postForEntity(bankUrl + "/bank/payouts", body, Map.class);
            Map<?, ?> map = resp.getBody();
            String result = (String) map.get("result");
            if ("ACCEPTED".equals(result)) {
                return new BankSubmitResult(BankSubmitResult.Outcome.ACCEPTED, (String) map.get("bank_ref"));
            } else {
                return new BankSubmitResult(BankSubmitResult.Outcome.REJECTED, null);
            }
        } catch (Exception e) {
            return new BankSubmitResult(BankSubmitResult.Outcome.UNKNOWN, null);
        }
    }

    public String queryStatus(String payoutId) {
        try {
            ResponseEntity<Map> resp = http.getForEntity(bankUrl + "/bank/payouts/" + payoutId, Map.class);
            return (String) resp.getBody().get("status");
        } catch (Exception e) {
            return "UNKNOWN";
        }
    }

    public record BankSubmitResult(Outcome outcome, String bankRef) {
        public enum Outcome { ACCEPTED, REJECTED, UNKNOWN }
    }
}
