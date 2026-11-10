package org.delcom.ifs25018pbo2026springbootcashflow.framework.view;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.delcom.ifs25018pbo2026springbootcashflow.adapter.presenter.FinancePresenter;
import org.delcom.ifs25018pbo2026springbootcashflow.domain.entity.SortOption;
import org.delcom.ifs25018pbo2026springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25018pbo2026springbootcashflow.domain.exception.EntityNotFoundException;
import org.delcom.ifs25018pbo2026springbootcashflow.domain.exception.ValidationException;
import org.delcom.ifs25018pbo2026springbootcashflow.usecase.FinanceUseCase;

import java.util.Comparator;
import java.util.List;

/**
 * Tampilan web Vaadin aplikasi catatan keuangan.
 * Tambah pemasukan/pengeluaran lewat dialog, cari keterangan, urutkan lewat
 * dropdown {@link SortOption} atau header kolom, dan hapus dengan konfirmasi.
 */
@Route("")
@PageTitle("Cashflow")
public class FinanceView extends VerticalLayout {

    private final FinanceUseCase financeUseCase;
    private final FinancePresenter presenter;

    private final Grid<Transaction> grid = new Grid<>(Transaction.class, false);
    private final TextField searchField = new TextField();
    private final Select<SortOption> sortSelect = new Select<>();
    private final Paragraph statusLabel = new Paragraph();
    private final Span emptyState = new Span();
    private final Span totalCount = new Span("0");
    private final Span incomeTotal = new Span();
    private final Span expenseTotal = new Span();
    private final Span balanceValue = new Span();
    private final Span balanceLabel = new Span();

    public FinanceView(FinanceUseCase financeUseCase, FinancePresenter presenter) {
        this.financeUseCase = financeUseCase;
        this.presenter = presenter;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        H1 title = new H1("Cashflow — Catatan Keuangan");
        configureGrid();
        configureToolbar();
        add(title, buildSummaryCards(), buildToolbar(), statusLabel, grid);
        refreshAll();
    }

    // ---------- Ringkasan ----------

    private HorizontalLayout buildSummaryCards() {
        HorizontalLayout summary = new HorizontalLayout(
                createSummaryCard("Jumlah Transaksi", totalCount, "#DBEAFE", "#1D4ED8", "#1E40AF"),
                createSummaryCard("Total Pemasukan", incomeTotal, "#DCFCE7", "#16A34A", "#166534"),
                createSummaryCard("Total Pengeluaran", expenseTotal, "#FEE2E2", "#DC2626", "#991B1B"),
                createSummaryCard(balanceLabel, balanceValue, "#FEF3C7", "#D97706", "#92400E")
        );
        summary.setWidthFull();
        summary.setSpacing(true);
        summary.getStyle().set("align-items", "stretch");
        return summary;
    }

    private Div createSummaryCard(String label, Span value, String background, String accent, String valueColor) {
        return createSummaryCard(new Span(label), value, background, accent, valueColor);
    }

    private Div createSummaryCard(Span caption, Span value, String background, String accent, String valueColor) {
        caption.getStyle()
                .set("display", "block")
                .set("font-size", "0.875rem")
                .set("font-weight", "500")
                .set("color", accent);
        value.getStyle()
                .set("display", "block")
                .set("margin-top", "0.35rem")
                .set("font-size", "1.5rem")
                .set("font-weight", "700")
                .set("line-height", "1.1")
                .set("color", valueColor);
        Div card = new Div(caption, value);
        card.getStyle()
                .set("flex", "1")
                .set("min-width", "0")
                .set("background", background)
                .set("border", "1px solid " + accent)
                .set("border-left", "6px solid " + accent)
                .set("border-radius", "12px")
                .set("padding", "1rem 1.1rem")
                .set("box-sizing", "border-box");
        return card;
    }

    private void updateSummary() {
        List<Transaction> all = financeUseCase.getAllTransactions();
        totalCount.setText(String.valueOf(all.size()));
        incomeTotal.setText(presenter.formatMoney(financeUseCase.getTotalIncome()));
        expenseTotal.setText(presenter.formatMoney(financeUseCase.getTotalExpense()));
        // Format "Saldo: Rp x" dipecah menjadi label + nilai agar tampil sebagai kartu.
        String balanceText = presenter.formatBalance(financeUseCase.getBalance());
        int split = balanceText.indexOf(':');
        balanceLabel.setText(balanceText.substring(0, split));
        balanceValue.setText(balanceText.substring(split + 1).trim());
    }

    // ---------- Grid ----------

    private void configureGrid() {
        grid.addColumn(Transaction::getId)
                .setHeader("ID").setKey("id")
                .setAutoWidth(true).setFlexGrow(0)
                .setSortable(true).setComparator(Transaction::getId);
        grid.addColumn(Transaction::getDescription)
                .setHeader("Keterangan").setKey("description")
                .setFlexGrow(1)
                .setSortable(true)
                .setComparator(Comparator.comparing(Transaction::getDescription, String.CASE_INSENSITIVE_ORDER));
        grid.addColumn(transaction -> presenter.formatMoney(transaction.getAmount()))
                .setHeader("Jumlah").setKey("amount")
                .setAutoWidth(true)
                .setSortable(true)
                .setComparator(SortOption.AMOUNT_ASC.comparator());
        grid.addComponentColumn(this::createTypeBadge)
                .setHeader("Tipe").setKey("type")
                .setAutoWidth(true).setFlexGrow(0)
                .setSortable(true)
                .setComparator(SortOption.INCOME_FIRST.comparator());
        grid.addColumn(presenter::formatCreatedAt)
                .setHeader("Waktu").setKey("createdAt")
                .setAutoWidth(true)
                .setSortable(true)
                .setComparator(Comparator.comparing(Transaction::getCreatedAt));
        grid.addComponentColumn(this::createActionButtons)
                .setHeader("Tindakan").setKey("tindakan")
                .setAutoWidth(true).setFlexGrow(0)
                .setSortable(false);
        grid.setEmptyStateComponent(emptyState);
        grid.setSizeFull();
    }

    private Span createTypeBadge(Transaction transaction) {
        Span badge = new Span(presenter.formatType(transaction.getType()));
        boolean income = transaction.isIncome();
        badge.getStyle()
                .set("padding", "0.15rem 0.6rem")
                .set("border-radius", "999px")
                .set("font-size", "0.8rem")
                .set("font-weight", "600")
                .set("color", income ? "#166534" : "#991B1B")
                .set("background", income ? "#DCFCE7" : "#FEE2E2");
        return badge;
    }

    private HorizontalLayout createActionButtons(Transaction transaction) {
        Button deleteButton = new Button("Hapus", VaadinIcon.TRASH.create(), event -> openDeleteDialog(transaction));
        deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY_INLINE);
        return new HorizontalLayout(deleteButton);
    }

    // ---------- Toolbar ----------

    private void configureToolbar() {
        searchField.setPlaceholder("Cari keterangan...");
        searchField.setPrefixComponent(VaadinIcon.SEARCH.create());
        searchField.setClearButtonVisible(true);

        sortSelect.setPlaceholder("Urutkan...");
        sortSelect.setItems(SortOption.values());
        // Select memanggil generator dengan null untuk item "kosong"; tangani agar tidak NPE.
        sortSelect.setItemLabelGenerator(option -> option == null ? "" : presenter.formatSortOption(option));
        sortSelect.setEmptySelectionAllowed(true);
        sortSelect.addValueChangeListener(event -> {
            if (event.getValue() != null) {
                sortTransactions(event.getValue());
            }
        });
    }

    private HorizontalLayout buildToolbar() {
        Button incomeButton = new Button("Tambah Pemasukan", VaadinIcon.PLUS.create(), event -> openAddDialog(true));
        incomeButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS);
        Button expenseButton = new Button("Tambah Pengeluaran", VaadinIcon.MINUS.create(), event -> openAddDialog(false));
        expenseButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);
        Button searchButton = new Button("Cari", VaadinIcon.SEARCH.create(), event -> searchTransactions());
        Button refreshButton = new Button("Muat Ulang", VaadinIcon.REFRESH.create(), event -> refreshAll());

        HorizontalLayout toolbar = new HorizontalLayout(
                searchField, searchButton, sortSelect, refreshButton, incomeButton, expenseButton);
        toolbar.setWidthFull();
        toolbar.setFlexGrow(1, searchField);
        return toolbar;
    }

    // ---------- Dialog ----------

    private void openAddDialog(boolean income) {
        TextField descriptionField = new TextField("Keterangan");
        descriptionField.setWidthFull();
        TextField amountField = new TextField("Jumlah (Rp)");
        amountField.setPlaceholder("contoh: 1000000");
        amountField.setWidthFull();
        VerticalLayout form = new VerticalLayout(descriptionField, amountField);
        form.setPadding(false);

        Dialog dialog = new Dialog();
        Button saveButton = new Button("Simpan", VaadinIcon.CHECK.create(), event -> {
            try {
                Transaction created = income
                        ? financeUseCase.addIncome(descriptionField.getValue(), amountField.getValue())
                        : financeUseCase.addExpense(descriptionField.getValue(), amountField.getValue());
                showSuccess(presenter.showAddSuccess(created));
                dialog.close();
                refreshAll();
            } catch (ValidationException ex) {
                showError(presenter.showValidationError(ex.getMessage()));
            }
        });
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        Button cancelButton = new Button("Batal", VaadinIcon.CLOSE.create(), event -> dialog.close());
        buildDialog(dialog, income ? "Menambah Pemasukan" : "Menambah Pengeluaran",
                income ? DialogTone.INCOME : DialogTone.DANGER, form, cancelButton, saveButton);
        dialog.open();
    }

    private void openDeleteDialog(Transaction transaction) {
        Paragraph message = new Paragraph("Yakin ingin menghapus transaksi \"" + transaction.getDescription()
                + "\" (ID " + transaction.getId() + ")?");
        message.getStyle().set("margin", "0");

        Dialog dialog = new Dialog();
        Button confirmButton = new Button("Hapus", VaadinIcon.TRASH.create(), event -> {
            deleteTransaction(transaction);
            dialog.close();
        });
        confirmButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);
        Button cancelButton = new Button("Batal", VaadinIcon.CLOSE.create(), event -> dialog.close());
        buildDialog(dialog, "Menghapus Transaksi", DialogTone.DANGER, message, cancelButton, confirmButton);
        dialog.open();
    }

    private enum DialogTone {
        INCOME("#16A34A"),
        DANGER("#DC2626");

        private final String color;

        DialogTone(String color) {
            this.color = color;
        }
    }

    /** Merakit dialog dengan header berwarna, body, dan footer abu-abu. */
    private void buildDialog(Dialog dialog, String title, DialogTone tone,
                             Component bodyContent, Button... footerButtons) {
        dialog.setWidth("500px");
        dialog.setMaxWidth("95vw");
        dialog.setResizable(false);

        Span headerTitle = new Span(title);
        headerTitle.getStyle()
                .set("color", "#FFFFFF")
                .set("font-size", "1.125rem")
                .set("font-weight", "600");
        Div header = new Div(headerTitle);
        header.getStyle()
                .set("background", tone.color)
                .set("padding", "1rem 1.5rem")
                .set("box-sizing", "border-box")
                .set("width", "100%");

        Div body = new Div(bodyContent);
        body.getStyle()
                .set("background", "#FFFFFF")
                .set("padding", "1.25rem 1.5rem")
                .set("box-sizing", "border-box")
                .set("width", "100%");

        Div footer = new Div();
        footer.getStyle()
                .set("display", "flex")
                .set("justify-content", "flex-end")
                .set("gap", "0.75rem")
                .set("background", "#F3F4F6")
                .set("border-top", "1px solid #E5E7EB")
                .set("padding", "0.85rem 1.5rem 1.1rem")
                .set("box-sizing", "border-box")
                .set("width", "100%");
        footer.add(footerButtons);

        Div shell = new Div(header, body, footer);
        shell.getStyle()
                .set("display", "flex")
                .set("flex-direction", "column")
                .set("width", "100%")
                .set("overflow", "hidden")
                .set("border-radius", "12px")
                .set("background", "#FFFFFF");
        dialog.removeAll();
        dialog.add(shell);
        dialog.addOpenedChangeListener(event -> {
            if (event.isOpened()) {
                dialog.getElement().executeJs("""
                        const overlay = this.$.overlay;
                        if (!overlay || !overlay.shadowRoot) { return; }
                        const content = overlay.shadowRoot.querySelector('[part="content"]');
                        if (content) { content.style.padding = '0'; content.style.overflow = 'visible'; }
                        const part = overlay.shadowRoot.querySelector('[part="overlay"]');
                        if (part) { part.style.padding = '0'; part.style.overflow = 'hidden'; }
                        """);
            }
        });
    }

    // ---------- Aksi ----------

    private void deleteTransaction(Transaction transaction) {
        try {
            financeUseCase.removeTransaction(transaction.getId());
            showSuccess(presenter.showRemoveSuccess());
            refreshAll();
        } catch (EntityNotFoundException ex) {
            showError(presenter.showRemoveFailed(ex.getId()));
            refreshAll();
        }
    }

    private void searchTransactions() {
        String keyword = searchField.getValue();
        if (keyword.isBlank()) {
            refreshAll();
            return;
        }
        List<Transaction> results = financeUseCase.searchTransactions(keyword);
        emptyState.setText(presenter.notFoundMessage());
        refreshGrid(results, "Hasil Pencarian: \"" + keyword + "\"");
    }

    private void sortTransactions(SortOption option) {
        emptyState.setText(presenter.emptyMessage());
        refreshGrid(financeUseCase.sortTransactions(option),
                "Daftar Transaksi (" + presenter.formatSortOption(option) + ")");
    }

    private void refreshAll() {
        emptyState.setText(presenter.emptyMessage());
        sortSelect.clear();
        refreshGrid(financeUseCase.getAllTransactions(), "Daftar Transaksi");
        searchField.clear();
    }

    private void refreshGrid(List<Transaction> transactions, String title) {
        grid.setItems(transactions);
        updateSummary();
        if (transactions.isEmpty()) {
            statusLabel.setText(title + " — " + emptyState.getText().replaceFirst("^- ", ""));
        } else {
            statusLabel.setText(title);
        }
    }

    private void showSuccess(String message) {
        Notification notification = Notification.show(message, 3000, Notification.Position.BOTTOM_START);
        notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    }

    private void showError(String message) {
        Notification notification = Notification.show(message, 4000, Notification.Position.BOTTOM_START);
        notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
    }
}
