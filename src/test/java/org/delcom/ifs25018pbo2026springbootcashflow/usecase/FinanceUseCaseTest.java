package org.delcom.ifs25018pbo2026springbootcashflow.usecase;

import org.delcom.ifs25018pbo2026springbootcashflow.adapter.repository.TransactionRepository;
import org.delcom.ifs25018pbo2026springbootcashflow.domain.entity.SortOption;
import org.delcom.ifs25018pbo2026springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25018pbo2026springbootcashflow.domain.exception.EntityNotFoundException;
import org.delcom.ifs25018pbo2026springbootcashflow.domain.exception.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FinanceUseCaseTest {

    private FinanceUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new FinanceUseCase(new TransactionRepository());
    }

    @Test
    void balanceFollowsIncomeAndExpense() throws Exception {
        assertEquals(new BigDecimal("0"), useCase.getBalance());
        useCase.addIncome("Gaji", "5000000");
        useCase.addExpense("Makan", "25000.50");
        assertEquals(new BigDecimal("5000000.00"), useCase.getTotalIncome());
        assertEquals(new BigDecimal("25000.50"), useCase.getTotalExpense());
        assertEquals(new BigDecimal("4974999.50"), useCase.getBalance());
    }

    @Test
    void balanceStaysConsistentAfterDelete() throws Exception {
        useCase.addIncome("Gaji", "100");
        Transaction e = useCase.addExpense("Makan", "30");
        assertEquals(new BigDecimal("70.00"), useCase.getBalance());
        useCase.removeTransaction(e.getId());
        assertEquals(new BigDecimal("100.00"), useCase.getBalance());
    }

    @Test
    void validation() {
        assertThrows(ValidationException.class, () -> useCase.addIncome("  ", "10"));
        assertThrows(ValidationException.class, () -> useCase.addExpense(null, "10"));
        assertThrows(ValidationException.class, () -> useCase.addIncome("x", null));
        assertThrows(ValidationException.class, () -> useCase.addIncome("x", "  "));
        assertThrows(ValidationException.class, () -> useCase.addIncome("x", "abc"));
        assertThrows(ValidationException.class, () -> useCase.addIncome("x", "-5"));
        assertThrows(ValidationException.class, () -> useCase.addExpense("x", "0"));
        assertThrows(ValidationException.class, () -> useCase.addExpense("x", "0.001"));
        assertTrue(useCase.getAllTransactions().isEmpty());
    }

    @Test
    void descriptionIsTrimmed() throws Exception {
        assertEquals("Gaji", useCase.addIncome("  Gaji ", "1").getDescription());
    }

    @Test
    void removeUnknownIdThrows() {
        EntityNotFoundException ex =
                assertThrows(EntityNotFoundException.class, () -> useCase.removeTransaction(42));
        assertEquals(42, ex.getId());
    }

    @Test
    void searchIsCaseInsensitiveAndDoesNotMutate() throws Exception {
        useCase.addIncome("Gaji Bulanan", "100");
        useCase.addExpense("Makan siang", "10");
        assertEquals(1, useCase.searchTransactions("GAJI").size());
        assertEquals(0, useCase.searchTransactions("zzz").size());
        assertEquals(2, useCase.getAllTransactions().size());
    }

    @Test
    void sortUsesSortOption() throws Exception {
        Transaction a = useCase.addExpense("a", "300");
        Transaction b = useCase.addIncome("b", "100");
        Transaction c = useCase.addExpense("c", "200");
        assertEquals(List.of(b, c, a), useCase.sortTransactions(SortOption.AMOUNT_ASC));
        assertEquals(List.of(a, c, b), useCase.sortTransactions(SortOption.AMOUNT_DESC));
        assertEquals(b, useCase.sortTransactions(SortOption.INCOME_FIRST).get(0));
        assertEquals(b, useCase.sortTransactions(SortOption.EXPENSE_FIRST).get(2));
    }
}
