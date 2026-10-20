package ua.kpi.comsys.dop;

import java.math.BigDecimal;

public record Transfer(String fromAccount, String toAccount, BigDecimal amount) implements Transaction {

    public Transfer {
        if (fromAccount == null || fromAccount.isBlank()) {
            throw new IllegalArgumentException("fromAccount must not be null or blank");
        }
        if (toAccount == null || toAccount.isBlank()) {
            throw new IllegalArgumentException("toAccount must not be null or blank");
        }
        if (amount == null || amount.signum() <= 0) {
            throw new IllegalArgumentException("amount must be greater than 0");
        }
    }
}
