package ua.kpi.comsys.dop;

import java.math.BigDecimal;

public record Deposit(String accountId, BigDecimal amount) implements Transaction {

    public Deposit {
        if (accountId == null || accountId.isBlank()) {
            throw new IllegalArgumentException("accountId must not be null or blank");
        }
        if (amount == null || amount.signum() <= 0) {
            throw new IllegalArgumentException("amount must be greater than 0");
        }
    }
}
