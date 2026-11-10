package org.delcom.ifs25018pbo2026springbootcashflow.adapter.presenter;

import org.delcom.ifs25018pbo2026springbootcashflow.domain.entity.SortOption;
import org.delcom.ifs25018pbo2026springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25018pbo2026springbootcashflow.domain.entity.TransactionType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FinancePresenterTest {

    private final FinancePresenter p = new FinancePresenter();

    private final Transaction income = new Transaction(1, "Gaji", new BigDecimal("1000000"),
            TransactionType.INCOME, LocalDateTime.of(2026, 11, 10, 8, 5));
    private final Transaction expense = new Transaction(2, "Makan", new BigDecimal("25000.5"),
            TransactionType.EXPENSE, LocalDateTime.of(2026, 11, 10, 9, 0));

    @Test
    void moneyFormat() {
        assertEquals("Rp 1,000,000.00", p.formatMoney(new BigDecimal("1000000")));
        assertEquals("Rp 0.00", p.formatMoney(BigDecimal.ZERO));
        assertEquals("Rp 4,975,000.00", p.formatMoney(new BigDecimal("4975000")));
        assertEquals("Saldo: Rp 4,975,000.00", p.formatBalance(new BigDecimal("4975000")));
    }

    @Test
    void rowFormat() {
        assertEquals("[1] Gaji | Rp 1,000,000.00 | [Pemasukan]", p.format(income));
        assertEquals("[2] Makan | Rp 25,000.50 | [Pengeluaran]", p.format(expense));
        assertEquals("10/11/2026 08:05", p.formatCreatedAt(income));
    }

    @Test
    void sortLabels() {
        assertEquals("Jumlah terkecil", p.formatSortOption(SortOption.AMOUNT_ASC));
        assertEquals("Jumlah terbesar", p.formatSortOption(SortOption.AMOUNT_DESC));
        assertEquals("Pemasukan dulu", p.formatSortOption(SortOption.INCOME_FIRST));
        assertEquals("Pengeluaran dulu", p.formatSortOption(SortOption.EXPENSE_FIRST));
    }

    @Test
    void lists() {
        assertEquals("Daftar Transaksi:\n- Data transaksi belum tersedia!", p.showTransactions(List.of()));
        assertEquals("Daftar Transaksi:\n[1] Gaji | Rp 1,000,000.00 | [Pemasukan]",
                p.showTransactions(List.of(income)));
        assertEquals("Hasil Pencarian: \"x\"\n- Transaksi tidak ditemukan!", p.showSearchResults(List.of(), "x"));
        assertEquals("Daftar Transaksi (Terurut):\n- Data transaksi belum tersedia!",
                p.showSortedTransactions(List.of()));
    }

    @Test
    void messages() {
        assertEquals("Berhasil menambah pemasukan: [1] Gaji | Rp 1,000,000.00 | [Pemasukan]", p.showAddSuccess(income));
        assertEquals("Berhasil menambah pengeluaran: [2] Makan | Rp 25,000.50 | [Pengeluaran]", p.showAddSuccess(expense));
        assertEquals("Berhasil menghapus transaksi.", p.showRemoveSuccess());
        assertEquals("[!] Gagal menghapus transaksi dengan ID: 5.", p.showRemoveFailed(5));
        assertEquals("[!] ID tidak valid!", p.showInvalidId());
        assertEquals("[!] Pilihan tidak dimengerti.", p.showInvalidChoice());
        assertEquals("[!] oops", p.showValidationError("oops"));
    }
}
