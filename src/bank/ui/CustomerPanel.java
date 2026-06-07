package bank.ui;

import bank.model.Customer;
import bank.service.BankService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;
import java.awt.*;
import java.util.List;

/**
 * CustomerPanel – create, view, update, delete customers.
 */
public class CustomerPanel extends JPanel {

    private final BankService service;

    private JTable          table;
    private DefaultTableModel tableModel;
    private JTextField      tfSearch;

    // Form fields
    private JTextField tfName, tfEmail, tfPhone, tfAddress;
    private JButton    btnAdd, btnUpdate, btnDelete, btnClear;

    private int selectedCustomerId = -1;

    public CustomerPanel(BankService service) {
        this.service = service;
        setBackground(MainFrame.COL_BG);
        setLayout(new BorderLayout(15, 15));
        setBorder(new EmptyBorder(25, 25, 25, 25));
        buildUI();
    }

    private void buildUI() {
        // ── Title row ─────────────────────────────────────────────
        JLabel title = new JLabel("Customer Management");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(MainFrame.COL_TEXT);
        add(title, BorderLayout.NORTH);

        // ── Left: form ────────────────────────────────────────────
        JPanel formPanel = buildFormPanel();
        add(formPanel, BorderLayout.WEST);

        // ── Centre: search + table ────────────────────────────────
        JPanel tablePanel = buildTablePanel();
        add(tablePanel, BorderLayout.CENTER);
    }

    private JPanel buildFormPanel() {
        JPanel p = new JPanel();
        p.setBackground(MainFrame.COL_WHITE);
        p.setLayout(new GridBagLayout());
        p.setPreferredSize(new Dimension(280, 0));
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 225, 235)),
                new EmptyBorder(20, 20, 20, 20)));

        GridBagConstraints g = new GridBagConstraints();
        g.fill    = GridBagConstraints.HORIZONTAL;
        g.insets  = new Insets(6, 0, 6, 0);
        g.weightx = 1.0;
        g.gridx   = 0;

        JLabel lForm = new JLabel("Add / Edit Customer");
        lForm.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lForm.setForeground(MainFrame.COL_PRIMARY);
        g.gridy = 0; p.add(lForm, g);

        g.gridy = 1; p.add(label("Full Name"), g);
        tfName = field(); g.gridy = 2; p.add(tfName, g);

        g.gridy = 3; p.add(label("Email"), g);
        tfEmail = field(); g.gridy = 4; p.add(tfEmail, g);

        g.gridy = 5; p.add(label("Phone"), g);
        tfPhone = field(); g.gridy = 6; p.add(tfPhone, g);

        g.gridy = 7; p.add(label("Address"), g);
        tfAddress = field(); g.gridy = 8; p.add(tfAddress, g);

        // Buttons
        btnAdd    = styledButton("➕ Add Customer",    MainFrame.COL_ACCENT);
        btnUpdate = styledButton("✏️ Update",           new Color(52, 152, 219));
        btnDelete = styledButton("🗑️ Delete",           new Color(231, 76, 60));
        btnClear  = styledButton("✖ Clear",             new Color(150, 150, 150));
        btnUpdate.setEnabled(false);
        btnDelete.setEnabled(false);

        JPanel btnRow1 = new JPanel(new GridLayout(1, 2, 8, 0));
        btnRow1.setOpaque(false);
        btnRow1.add(btnAdd); btnRow1.add(btnUpdate);
        g.gridy = 9; g.insets = new Insets(14, 0, 6, 0);
        p.add(btnRow1, g);

        JPanel btnRow2 = new JPanel(new GridLayout(1, 2, 8, 0));
        btnRow2.setOpaque(false);
        btnRow2.add(btnDelete); btnRow2.add(btnClear);
        g.gridy = 10; g.insets = new Insets(0, 0, 0, 0);
        p.add(btnRow2, g);

        // Fill remaining space
        g.gridy = 11; g.weighty = 1.0;
        p.add(new JPanel() {{ setOpaque(false); }}, g);

        // ── Listeners ─────────────────────────────────────────────
        btnAdd.addActionListener(e -> addCustomer());
        btnUpdate.addActionListener(e -> updateCustomer());
        btnDelete.addActionListener(e -> deleteCustomer());
        btnClear.addActionListener(e -> clearForm());

        return p;
    }

    private JPanel buildTablePanel() {
        JPanel p = new JPanel(new BorderLayout(0, 10));
        p.setOpaque(false);

        // Search bar
        JPanel searchRow = new JPanel(new BorderLayout(8, 0));
        searchRow.setOpaque(false);
        tfSearch = new JTextField();
        tfSearch.setFont(MainFrame.FONT_BODY);
        tfSearch.putClientProperty("JTextField.placeholderText", "Search by name or email...");
        JButton btnSearch = styledButton("🔍 Search", MainFrame.COL_PRIMARY);
        JButton btnRefresh = styledButton("↺ All", new Color(100, 120, 150));
        searchRow.add(tfSearch, BorderLayout.CENTER);
        JPanel btnSearch2 = new JPanel(new GridLayout(1, 2, 5, 0));
        btnSearch2.setOpaque(false);
        btnSearch2.add(btnSearch); btnSearch2.add(btnRefresh);
        searchRow.add(btnSearch2, BorderLayout.EAST);
        p.add(searchRow, BorderLayout.NORTH);

        btnSearch.addActionListener(e -> searchCustomers());
        btnRefresh.addActionListener(e -> refresh());

        // Table
        String[] cols = {"ID", "Name", "Email", "Phone", "Address"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        styleTable(table);
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) populateFormFromTable();
        });

        p.add(new JScrollPane(table), BorderLayout.CENTER);
        return p;
    }

    // ── Actions ───────────────────────────────────────────────────
    private void addCustomer() {
        if (!validateForm()) return;
        Customer c = new Customer(tfName.getText().trim(), tfEmail.getText().trim(),
                tfPhone.getText().trim(), tfAddress.getText().trim());
        if (service.addCustomer(c)) {
            JOptionPane.showMessageDialog(this, "Customer added successfully!", "Success",
                    JOptionPane.INFORMATION_MESSAGE);
            clearForm(); refresh();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to add customer. Email may already exist.",
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateCustomer() {
        if (selectedCustomerId < 0 || !validateForm()) return;
        Customer c = new Customer(tfName.getText().trim(), tfEmail.getText().trim(),
                tfPhone.getText().trim(), tfAddress.getText().trim());
        c.setCustomerId(selectedCustomerId);
        if (service.updateCustomer(c)) {
            JOptionPane.showMessageDialog(this, "Customer updated!", "Success",
                    JOptionPane.INFORMATION_MESSAGE);
            clearForm(); refresh();
        }
    }

    private void deleteCustomer() {
        if (selectedCustomerId < 0) return;
        int confirm = JOptionPane.showConfirmDialog(this,
                "Delete this customer and all their accounts?", "Confirm Delete",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            service.deleteCustomer(selectedCustomerId);
            clearForm(); refresh();
        }
    }

    private void searchCustomers() {
        String kw = tfSearch.getText().trim();
        if (kw.isEmpty()) { refresh(); return; }
        populateTable(service.searchCustomers(kw));
    }

    private void populateFormFromTable() {
        int row = table.getSelectedRow();
        if (row < 0) return;
        selectedCustomerId = (int) tableModel.getValueAt(row, 0);
        tfName.setText((String) tableModel.getValueAt(row, 1));
        tfEmail.setText((String) tableModel.getValueAt(row, 2));
        tfPhone.setText((String) tableModel.getValueAt(row, 3));
        tfAddress.setText((String) tableModel.getValueAt(row, 4));
        btnUpdate.setEnabled(true);
        btnDelete.setEnabled(true);
        btnAdd.setEnabled(false);
    }

    private void clearForm() {
        selectedCustomerId = -1;
        tfName.setText(""); tfEmail.setText("");
        tfPhone.setText(""); tfAddress.setText("");
        btnAdd.setEnabled(true);
        btnUpdate.setEnabled(false);
        btnDelete.setEnabled(false);
        table.clearSelection();
    }

    public void refresh() {
        populateTable(service.getAllCustomers());
    }

    private void populateTable(List<Customer> list) {
        tableModel.setRowCount(0);
        for (Customer c : list)
            tableModel.addRow(new Object[]{
                    c.getCustomerId(), c.getFullName(), c.getEmail(),
                    c.getPhone(), c.getAddress()});
    }

    private boolean validateForm() {
        if (tfName.getText().trim().isEmpty() || tfEmail.getText().trim().isEmpty()
                || tfPhone.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Name, Email, and Phone are required.",
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        return true;
    }

    // ── UI helpers ────────────────────────────────────────────────
    private JLabel label(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 12));
        l.setForeground(new Color(80, 90, 110));
        return l;
    }

    private JTextField field() {
        JTextField tf = new JTextField();
        tf.setFont(MainFrame.FONT_BODY);
        tf.setPreferredSize(new Dimension(0, 32));
        return tf;
    }

    private JButton styledButton(String text, Color bg) {
        JButton b = new JButton(text);
        b.setFont(new Font("Segoe UI", Font.BOLD, 12));
        b.setBackground(bg);
        b.setForeground(Color.WHITE);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setPreferredSize(new Dimension(0, 34));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    private void styleTable(JTable t) {
        t.setFont(MainFrame.FONT_BODY);
        t.setRowHeight(28);
        t.setGridColor(new Color(230, 235, 245));
        t.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        t.getTableHeader().setBackground(MainFrame.COL_PRIMARY);
        t.getTableHeader().setForeground(Color.WHITE);
        t.setSelectionBackground(new Color(200, 220, 255));
        t.setShowVerticalLines(false);
        // Hide ID column (still accessible via model)
        t.getColumnModel().getColumn(0).setMaxWidth(50);
    }
}
