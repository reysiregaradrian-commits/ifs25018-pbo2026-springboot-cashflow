package org.delcom.ifs25018pbo2026springbootcashflow.adapter.presenter;

import org.delcom.ifs25018pbo2026springbootcashflow.domain.entity.SortOption;
import org.delcom.ifs25018pbo2026springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25018pbo2026springbootcashflow.domain.entity.TransactionType;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Presenter yang memformat data dari use case menjadi teks siap tampil.
 * Format tampilan bukan tanggung jawab entity.
 */
public class FinancePresenter {

    private static final DateTimeFormatter DATE_TIME_FORMAT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    /** Format uang: pemisah ribuan koma dan 2 desimal, mis. {@code Rp 1,000,000.00}. */
    public String formatMoney(BigDecimal amount) {
        DecimalFormat format = new DecimalFormat("#,##0.00", DecimalFormatSymbols.getInstance(Locale.US));
        return "Rp " + format.format(amount);
    }

    /** Label tipe transaksi. */
    public String formatType(TransactionType type) {
        return type == TransactionType.INCOME ? "Pemasukan" : "Pengeluaran";
    }

    /** Memformat waktu transaksi. */
    public String formatCreatedAt(Transaction transaction) {
        return transaction.getCreatedAt().format(DATE_TIME_FORMAT);
    }

    /** Satu baris transaksi: {@code [id] keterangan | Rp x | [Tipe]}. */
    public String format(Transaction transaction) {
        return String.format("[%d] %s | %s | [%s]",
                transaction.getId(),
                transaction.getDescription(),
                formatMoney(transaction.getAmount()),
                formatType(transaction.getType()));
    }

    /** Memformat saldo: {@code Saldo: Rp x}. */
    public String formatBalance(BigDecimal balance) {
        return "Saldo: " + formatMoney(balance);
    }

    /** Label opsi pengurutan. */
    public String formatSortOption(SortOption option) {
        return switch (option) {
            case AMOUNT_ASC -> "Jumlah terkecil";
            case AMOUNT_DESC -> "Jumlah terbesar";
            case INCOME_FIRST -> "Pemasukan dulu";
            case EXPENSE_FIRST -> "Pengeluaran dulu";
        };
    }

    /** Memformat daftar transaksi menjadi teks multi-baris. */
    public String formatList(List<Transaction> transactions, String header, String emptyMessage) {
        StringBuilder builder = new StringBuilder(header);
        if (transactions.isEmpty()) {
            return builder.append('\n').append(emptyMessage).toString();
        }
        String body = transactions.stream()
                .map(this::format)
                .collect(Collectors.joining("\n"));
        return builder.append('\n').append(body).toString();
    }

    public String showTransactions(List<Transaction> transactions) {
        return formatList(transactions, "Daftar Transaksi:", emptyMessage());
    }

    public String showSearchResults(List<Transaction> transactions, String keyword) {
        return formatList(transactions, "Hasil Pencarian: \"" + keyword + "\"", notFoundMessage());
    }

    public String showSortedTransactions(List<Transaction> transactions) {
        return formatList(transactions, "Daftar Transaksi (Terurut):", emptyMessage());
    }

    public String emptyMessage() {
        return "- Data transaksi belum tersedia!";
    }

    public String notFoundMessage() {
        return "- Transaksi tidak ditemukan!";
    }

    public String showAddSuccess(Transaction transaction) {
        String kind = transaction.isIncome() ? "pemasukan" : "pengeluaran";
        return "Berhasil menambah " + kind + ": " + format(transaction);
    }

    public String showRemoveSuccess() {
        return "Berhasil menghapus transaksi.";
    }

    public String showRemoveFailed(int id) {
        return String.format("[!] Gagal menghapus transaksi dengan ID: %d.", id);
    }

    public String showInvalidId() {
        return "[!] ID tidak valid!";
    }

    public String showInvalidChoice() {
        return "[!] Pilihan tidak dimengerti.";
    }

    public String showValidationError(String message) {
        return "[!] " + message;
    }
}
