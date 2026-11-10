package org.delcom.ifs25018pbo2026springbootcashflow.adapter.repository;

import org.delcom.ifs25018pbo2026springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25018pbo2026springbootcashflow.domain.entity.TransactionType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TransactionRepositoryTest {

    @Test
    void autoIdAndTypes() {
        TransactionRepository repo = new TransactionRepository();
        Transaction in = repo.saveIncome("Gaji", new BigDecimal("10.00"));
        Transaction out = repo.saveExpense("Makan", new BigDecimal("5.00"));
        assertEquals(1, in.getId());
        assertEquals(2, out.getId());
        assertEquals(TransactionType.INCOME, in.getType());
        assertEquals(TransactionType.EXPENSE, out.getType());
    }

    @Test
    void crudAndDefensiveCopy() {
        TransactionRepository repo = new TransactionRepository();
        Transaction in = repo.saveIncome("Gaji", new BigDecimal("10.00"));
        repo.saveExpense("Makan", new BigDecimal("5.00"));

        List<Transaction> copy = repo.findAll();
        copy.clear();
        assertEquals(2, repo.findAll().size());

        assertTrue(repo.findById(1).isPresent());
        assertTrue(repo.findById(99).isEmpty());
        assertEquals(1, repo.findBy(Transaction::isIncome).size());

        in.changeDescription("Bonus");
        repo.update(in);
        assertEquals("Bonus", repo.findById(1).orElseThrow().getDescription());

        assertTrue(repo.deleteById(1));
        assertFalse(repo.deleteById(1));
        assertEquals(1, repo.findAll().size());
    }
}
