package org.delcom.ifs25018pbo2026springbootcashflow.domain.entity;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TransactionTest {

    private static Transaction tx(int id, String d, String a, TransactionType t) {
        return new Transaction(id, d, new BigDecimal(a), t);
    }

    @Test
    void gettersAndSignedAmount() {
        LocalDateTime now = LocalDateTime.of(2026, 1, 2, 3, 4);
        Transaction income = new Transaction(1, "Gaji", new BigDecimal("100.00"), TransactionType.INCOME, now);
        Transaction expense = tx(2, "Makan", "40.00", TransactionType.EXPENSE);

        assertEquals(1, income.getId());
        assertEquals("Gaji", income.getDescription());
        assertEquals(new BigDecimal("100.00"), income.getAmount());
        assertEquals(TransactionType.INCOME, income.getType());
        assertEquals(now, income.getCreatedAt());
        assertNotNull(expense.getCreatedAt());
        assertTrue(income.isIncome());
        assertFalse(expense.isIncome());
        assertEquals(new BigDecimal("100.00"), income.signedAmount());
        assertEquals(new BigDecimal("-40.00"), expense.signedAmount());
    }

    @Test
    void changeMethods() {
        Transaction t = tx(1, "a", "1.00", TransactionType.INCOME);
        t.changeDescription("b");
        t.changeAmount(new BigDecimal("2.00"));
        assertEquals("b", t.getDescription());
        assertEquals(new BigDecimal("2.00"), t.getAmount());
    }

    @Test
    void sortOptionsUseTheirComparators() {
        Transaction a = tx(1, "a", "300.00", TransactionType.EXPENSE);
        Transaction b = tx(2, "b", "100.00", TransactionType.INCOME);
        Transaction c = tx(3, "c", "200.00", TransactionType.EXPENSE);
        List<Transaction> all = List.of(a, b, c);

        assertEquals(List.of(b, c, a), all.stream().sorted(SortOption.AMOUNT_ASC.comparator()).toList());
        assertEquals(List.of(a, c, b), all.stream().sorted(SortOption.AMOUNT_DESC.comparator()).toList());
        assertEquals(b, all.stream().sorted(SortOption.INCOME_FIRST.comparator()).findFirst().orElseThrow());
        assertEquals(b, all.stream().sorted(SortOption.EXPENSE_FIRST.comparator()).toList().get(2));
        assertEquals(4, SortOption.values().length);
        assertEquals(SortOption.AMOUNT_ASC, SortOption.valueOf("AMOUNT_ASC"));
        assertEquals(TransactionType.EXPENSE, TransactionType.valueOf("EXPENSE"));
        assertNotNull(Comparator.class);
    }
}
