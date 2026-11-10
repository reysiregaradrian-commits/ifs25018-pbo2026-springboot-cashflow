package org.delcom.ifs25018pbo2026springbootcashflow.framework.view;

import com.github.mvysny.kaributesting.v10.MockVaadin;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridSortOrder;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.SortDirection;
import org.delcom.ifs25018pbo2026springbootcashflow.adapter.presenter.FinancePresenter;
import org.delcom.ifs25018pbo2026springbootcashflow.adapter.repository.TransactionRepository;
import org.delcom.ifs25018pbo2026springbootcashflow.domain.entity.SortOption;
import org.delcom.ifs25018pbo2026springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25018pbo2026springbootcashflow.domain.exception.EntityNotFoundException;
import org.delcom.ifs25018pbo2026springbootcashflow.usecase.FinanceUseCase;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.github.mvysny.kaributesting.v10.GridKt._getCellComponent;
import static com.github.mvysny.kaributesting.v10.LocatorJ._click;
import static com.github.mvysny.kaributesting.v10.LocatorJ._get;
import static com.github.mvysny.kaributesting.v10.LocatorJ._setValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;

class FinanceViewTest {

    private FinanceUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = spy(new FinanceUseCase(new TransactionRepository()));
        MockVaadin.setup();
        UI.getCurrent().add(new FinanceView(useCase, new FinancePresenter()));
    }

    @AfterEach
    void tearDown() {
        MockVaadin.tearDown();
    }

    @Test
    void addSearchSortAndDeleteFlow() throws Exception {
        assertTrue(_get(Paragraph.class).getText().contains("Data transaksi belum tersedia"));
        assertSummary("0", "Rp 0.00", "Rp 0.00", "Rp 0.00");

        addTransaction("Tambah Pemasukan", "Gaji", "5000000");
        addTransaction("Tambah Pengeluaran", "Makan siang", "25000");
        addTransaction("Tambah Pengeluaran", "Buku", "100000.5");
        assertEquals(3, useCase.getAllTransactions().size());
        assertEquals("Daftar Transaksi", _get(Paragraph.class).getText());
        assertSummary("3", "Rp 5,000,000.00", "Rp 125,000.50", "Rp 4,874,999.50");

        // Cari: tidak ditemukan, ditemukan (case-insensitive), kosong -> semua
        TextField searchField = _get(TextField.class, spec -> spec.withPlaceholder("Cari keterangan..."));
        _setValue(searchField, "tidakada");
        clickButton("Cari");
        assertTrue(_get(Paragraph.class).getText().contains("Transaksi tidak ditemukan"));

        _setValue(searchField, "GAJI");
        clickButton("Cari");
        assertEquals("Hasil Pencarian: \"GAJI\"", _get(Paragraph.class).getText());
        assertEquals(1, grid().getListDataView().getItemCount());

        _setValue(searchField, "  ");
        clickButton("Cari");
        assertEquals(3, grid().getListDataView().getItemCount());

        // Urut lewat dropdown SortOption, lalu reset lewat Muat Ulang
        @SuppressWarnings("unchecked")
        Select<SortOption> sortSelect = _get(Select.class);
        _setValue(sortSelect, SortOption.AMOUNT_ASC);
        assertEquals("Daftar Transaksi (Jumlah terkecil)", _get(Paragraph.class).getText());
        assertEquals("Makan siang", grid().getListDataView().getItems().toList().get(0).getDescription());
        _setValue(sortSelect, SortOption.AMOUNT_DESC);
        assertEquals("Gaji", grid().getListDataView().getItems().toList().get(0).getDescription());
        clickButton("Muat Ulang");
        assertEquals("Daftar Transaksi", _get(Paragraph.class).getText());

        // Urut lewat header kolom
        Grid<Transaction> grid = grid();
        for (String key : List.of("id", "description", "amount", "type", "createdAt")) {
            grid.sort(List.of(new GridSortOrder<>(grid.getColumnByKey(key), SortDirection.ASCENDING)));
            grid.sort(List.of(new GridSortOrder<>(grid.getColumnByKey(key), SortDirection.DESCENDING)));
        }

        // Badge tipe untuk pemasukan dan pengeluaran
        List<Transaction> items = grid.getListDataView().getItems().toList();
        for (int i = 0; i < items.size(); i++) {
            Span badge = (Span) _getCellComponent(grid, i, "type");
            assertEquals(items.get(i).isIncome() ? "Pemasukan" : "Pengeluaran", badge.getText());
        }

        // Hapus dengan konfirmasi
        int row = indexOf(grid, "Makan siang");
        deleteRow(grid, row);
        clickDialogButton(_get(Dialog.class), "Hapus");
        assertEquals(2, useCase.getAllTransactions().size());
        assertSummary("2", "Rp 5,000,000.00", "Rp 100,000.50", "Rp 4,899,999.50");
    }

    @Test
    void validationErrorsCancelAndDeleteCancel() throws Exception {
        clickButton("Tambah Pemasukan");
        Dialog dialog = _get(Dialog.class);
        _setValue(_get(dialog, TextField.class, spec -> spec.withLabel("Keterangan")), " ");
        _setValue(_get(dialog, TextField.class, spec -> spec.withLabel("Jumlah (Rp)")), "100");
        clickDialogButton(dialog, "Simpan");

        _setValue(_get(dialog, TextField.class, spec -> spec.withLabel("Keterangan")), "Gaji");
        _setValue(_get(dialog, TextField.class, spec -> spec.withLabel("Jumlah (Rp)")), "abc");
        clickDialogButton(dialog, "Simpan");

        _setValue(_get(dialog, TextField.class, spec -> spec.withLabel("Jumlah (Rp)")), "-5");
        clickDialogButton(dialog, "Simpan");
        assertTrue(useCase.getAllTransactions().isEmpty());
        clickDialogButton(dialog, "Batal");

        clickButton("Tambah Pengeluaran");
        Dialog expenseDialog = _get(Dialog.class);
        clickDialogButton(expenseDialog, "Simpan");
        clickDialogButton(expenseDialog, "Batal");
        assertTrue(useCase.getAllTransactions().isEmpty());

        useCase.addIncome("Ada", "10");
        clickButton("Muat Ulang");
        deleteRow(grid(), 0);
        clickDialogButton(_get(Dialog.class), "Batal");
        assertEquals(1, useCase.getAllTransactions().size());
    }

    @Test
    void deleteNotFoundShowsErrorAndRefreshes() throws Exception {
        useCase.addIncome("Temp", "10");
        clickButton("Muat Ulang");
        Transaction selected = useCase.getAllTransactions().get(0);
        doThrow(new EntityNotFoundException(selected.getId()))
                .when(useCase).removeTransaction(selected.getId());

        deleteRow(grid(), 0);
        clickDialogButton(_get(Dialog.class), "Hapus");

        assertEquals(1, useCase.getAllTransactions().size());
    }

    // ---------- helper ----------

    @SuppressWarnings("unchecked")
    private Grid<Transaction> grid() {
        return _get(Grid.class);
    }

    private void addTransaction(String buttonCaption, String description, String amount) {
        clickButton(buttonCaption);
        Dialog dialog = _get(Dialog.class);
        _setValue(_get(dialog, TextField.class, spec -> spec.withLabel("Keterangan")), description);
        _setValue(_get(dialog, TextField.class, spec -> spec.withLabel("Jumlah (Rp)")), amount);
        clickDialogButton(dialog, "Simpan");
    }

    private void deleteRow(Grid<Transaction> grid, int row) {
        HorizontalLayout actions = (HorizontalLayout) _getCellComponent(grid, row, "tindakan");
        _click(_get(actions, Button.class, spec -> spec.withCaption("Hapus")));
    }

    private int indexOf(Grid<Transaction> grid, String description) {
        List<Transaction> items = grid.getListDataView().getItems().toList();
        for (int i = 0; i < items.size(); i++) {
            if (description.equals(items.get(i).getDescription())) {
                return i;
            }
        }
        throw new AssertionError("Transaksi tidak ditemukan: " + description);
    }

    /** Memeriksa nilai pada 4 kartu ringkasan: jumlah, pemasukan, pengeluaran, saldo. */
    private void assertSummary(String count, String income, String expense, String balance) {
        FinanceView view = _get(FinanceView.class);
        HorizontalLayout summary = (HorizontalLayout) view.getChildren()
                .filter(HorizontalLayout.class::isInstance)
                .findFirst()
                .orElseThrow();
        List<String> values = summary.getChildren()
                .map(card -> ((Div) card).getChildren()
                        .skip(1)
                        .map(component -> ((Span) component).getText())
                        .findFirst()
                        .orElse(""))
                .toList();
        assertEquals(List.of(count, income, expense, balance), values);
    }

    private void clickButton(String caption) {
        _click(_get(Button.class, spec -> spec.withCaption(caption)));
    }

    private void clickDialogButton(Dialog dialog, String caption) {
        _click(_get(dialog, Button.class, spec -> spec.withCaption(caption)));
    }
}
