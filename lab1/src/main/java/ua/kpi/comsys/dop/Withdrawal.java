package ua.kpi.comsys.dop;

import java.math.BigDecimal;

public record Withdrawal(String accountId, BigDecimal amount, String category) implements Transaction {

    public Withdrawal {
        if (accountId == null || accountId.isBlank()) {
            throw new IllegalArgumentException("accountId must not be null or blank");
        }
        if (amount == null || amount.signum() <= 0) {
            throw new IllegalArgumentException("amount must be greater than 0");
        }
        if (category == null || category.isBlank()) {
            throw new IllegalArgumentException("category must not be null or blank");
        }
    }
}
