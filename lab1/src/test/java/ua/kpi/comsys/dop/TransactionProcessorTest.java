package ua.kpi.comsys.dop;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class TransactionProcessorTest {

    private final TransactionProcessor processor = new TransactionProcessor();

    @Test
    void processDeposit() {
        String result = processor.process(new Deposit("ACC-1", new BigDecimal("100.50")));
        assertEquals("""
                Transaction Type: DEPOSIT
                Account: ACC-1
                Amount: USD 100.50
                """, result);
    }

    @Test
    void processWithdrawal() {
        String result = processor.process(new Withdrawal("ACC-1", new BigDecimal("25"), "Food"));
        assertEquals("""
                Transaction Type: WITHDRAWAL
                Account: ACC-1
                Category: Food
                Amount: USD 25
                """, result);
    }

    @Test
    void processTransfer() {
        String result = processor.process(new Transfer("ACC-1", "ACC-2", new BigDecimal("10.00")));
        assertEquals("""
                Transaction Type: TRANSFER
                From: ACC-1
                To: ACC-2
                Amount: USD 10.00
                """, result);
    }

    @Test
    void processNullThrows() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> processor.process(null));
        assertEquals("Transaction cannot be null", e.getMessage());
    }

    @Test
    void depositValidation() {
        BigDecimal ok = BigDecimal.ONE;
        assertThrows(IllegalArgumentException.class, () -> new Deposit(null, ok));
        assertThrows(IllegalArgumentException.class, () -> new Deposit(" ", ok));
        assertThrows(IllegalArgumentException.class, () -> new Deposit("A", null));
        assertThrows(IllegalArgumentException.class, () -> new Deposit("A", BigDecimal.ZERO));
        assertThrows(IllegalArgumentException.class, () -> new Deposit("A", new BigDecimal("-1")));
    }

    @Test
    void withdrawalValidation() {
        BigDecimal ok = BigDecimal.ONE;
        assertThrows(IllegalArgumentException.class, () -> new Withdrawal(null, ok, "c"));
        assertThrows(IllegalArgumentException.class, () -> new Withdrawal("", ok, "c"));
        assertThrows(IllegalArgumentException.class, () -> new Withdrawal("A", null, "c"));
        assertThrows(IllegalArgumentException.class, () -> new Withdrawal("A", BigDecimal.ZERO, "c"));
        assertThrows(IllegalArgumentException.class, () -> new Withdrawal("A", new BigDecimal("-5"), "c"));
        assertThrows(IllegalArgumentException.class, () -> new Withdrawal("A", ok, null));
        assertThrows(IllegalArgumentException.class, () -> new Withdrawal("A", ok, "  "));
    }

    @Test
    void transferValidation() {
        BigDecimal ok = BigDecimal.ONE;
        assertThrows(IllegalArgumentException.class, () -> new Transfer(null, "B", ok));
        assertThrows(IllegalArgumentException.class, () -> new Transfer(" ", "B", ok));
        assertThrows(IllegalArgumentException.class, () -> new Transfer("A", null, ok));
        assertThrows(IllegalArgumentException.class, () -> new Transfer("A", "", ok));
        assertThrows(IllegalArgumentException.class, () -> new Transfer("A", "B", null));
        assertThrows(IllegalArgumentException.class, () -> new Transfer("A", "B", BigDecimal.ZERO));
        assertThrows(IllegalArgumentException.class, () -> new Transfer("A", "B", new BigDecimal("-0.01")));
    }
}
