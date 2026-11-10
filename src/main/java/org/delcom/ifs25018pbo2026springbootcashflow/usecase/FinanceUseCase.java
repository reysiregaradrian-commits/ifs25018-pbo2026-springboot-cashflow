package org.delcom.ifs25018pbo2026springbootcashflow.usecase;

import org.delcom.ifs25018pbo2026springbootcashflow.domain.entity.SortOption;
import org.delcom.ifs25018pbo2026springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25018pbo2026springbootcashflow.domain.exception.EntityNotFoundException;
import org.delcom.ifs25018pbo2026springbootcashflow.domain.exception.ValidationException;
import org.delcom.ifs25018pbo2026springbootcashflow.domain.repository.ITransactionRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Use case yang menangani logika bisnis pencatatan keuangan.
 * Tidak melakukan I/O — hanya memproses data dan melempar checked exception
 * domain jika operasi tidak valid atau data tidak ditemukan.
 */
public class FinanceUseCase {

    /** Port repository yang di-inject dari luar (Dependency Inversion). */
    private final ITransactionRepository transactionRepository;

    public FinanceUseCase(ITransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    /** Mengambil semua transaksi. */
    public List<Transaction> getAllTransactions() {
        return transactionRepository.findAll();
    }

    /** Menambah pemasukan setelah validasi keterangan dan jumlah. */
    public Transaction addIncome(String description, String amountText) throws ValidationException {
        String cleanDescription = validateDescription(description);
        BigDecimal amount = parseAmount(amountText);
        return transactionRepository.saveIncome(cleanDescription, amount);
    }

    /** Menambah pengeluaran setelah validasi keterangan dan jumlah. */
    public Transaction addExpense(String description, String amountText) throws ValidationException {
        String cleanDescription = validateDescription(description);
        BigDecimal amount = parseAmount(amountText);
        return transactionRepository.saveExpense(cleanDescription, amount);
    }

    /** Menghapus transaksi berdasarkan ID; melempar exception jika tidak ditemukan. */
    public void removeTransaction(int id) throws EntityNotFoundException {
        if (!transactionRepository.deleteById(id)) {
            throw new EntityNotFoundException(id);
        }
    }

    /** Mencari transaksi yang keterangannya mengandung kata kunci (case-insensitive). */
    public List<Transaction> searchTransactions(String keyword) {
        String lowerKeyword = keyword.toLowerCase();
        return transactionRepository.findBy(
                transaction -> transaction.getDescription().toLowerCase().contains(lowerKeyword));
    }

    /** Mengurutkan transaksi sesuai kriteria {@link SortOption}. */
    public List<Transaction> sortTransactions(SortOption option) {
        return transactionRepository.findAll().stream()
                .sorted(option.comparator())
                .toList();
    }

    /** Total seluruh pemasukan. */
    public BigDecimal getTotalIncome() {
        return sumOf(true);
    }

    /** Total seluruh pengeluaran. */
    public BigDecimal getTotalExpense() {
        return sumOf(false);
    }

    /** Saldo saat ini = total pemasukan − total pengeluaran. */
    public BigDecimal getBalance() {
        return getTotalIncome().subtract(getTotalExpense());
    }

    private BigDecimal sumOf(boolean income) {
        return transactionRepository.findBy(transaction -> transaction.isIncome() == income).stream()
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private String validateDescription(String description) throws ValidationException {
        if (description == null || description.trim().isEmpty()) {
            throw new ValidationException("Keterangan transaksi tidak boleh kosong");
        }
        return description.trim();
    }

    private BigDecimal parseAmount(String amountText) throws ValidationException {
        if (amountText == null || amountText.trim().isEmpty()) {
            throw new ValidationException("Jumlah tidak boleh kosong");
        }
        BigDecimal amount;
        try {
            amount = new BigDecimal(amountText.trim());
        } catch (NumberFormatException ex) {
            throw new ValidationException("Jumlah harus berupa angka");
        }
        amount = amount.setScale(2, RoundingMode.HALF_UP);
        if (amount.signum() <= 0) {
            throw new ValidationException("Jumlah harus lebih besar dari 0");
        }
        return amount;
    }
}
