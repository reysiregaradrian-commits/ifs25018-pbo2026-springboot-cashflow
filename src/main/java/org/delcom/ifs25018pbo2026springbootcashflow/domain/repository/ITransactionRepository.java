package org.delcom.ifs25018pbo2026springbootcashflow.domain.repository;

import org.delcom.ifs25018pbo2026springbootcashflow.domain.entity.Transaction;

import java.math.BigDecimal;

/**
 * Port spesifik Transaction yang memperluas generic {@link IRepository}.
 * Menambahkan method pembuatan transaksi baru dengan penomoran ID otomatis.
 */
public interface ITransactionRepository extends IRepository<Transaction, Integer> {

    /** Menyimpan transaksi pemasukan baru. */
    Transaction saveIncome(String description, BigDecimal amount);

    /** Menyimpan transaksi pengeluaran baru. */
    Transaction saveExpense(String description, BigDecimal amount);
}
