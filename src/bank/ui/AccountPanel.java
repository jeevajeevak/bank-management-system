package bank.ui;

import bank.model.Account;
import bank.model.Account.AccountType;
import bank.model.Customer;
import bank.service.BankService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * AccountPanel – open, view, freeze/activate, close/delete accounts.
 */
public class AccountPanel extends JPanel {

    private final BankService service;

    private JTable            table;
    private DefaultTableModel tableModel;

    // Form
    private JComboBox<String> cbCustomer, cbType;
    private JTextField        tfInitialBalance;
    private JButton           btnOpen, btnFreeze, btnActivate, btnDelete;

    private int selectedAccountId = -1;

    public AccountPanel(BankService service) {
        this.service = service;
        setBackground(MainFrame.COL_BG);
        setLayout(new BorderLayout(15, 15));
        setBorder(new EmptyBorder(25, 25, 25, 25));
        buildUI();
    }

    private void buildUI() {
        JLabel title = new JLabel("Account Management");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(MainFrame.COL_TEXT);
        add(title, BorderLayout.NORTH);
        add(buildForm(),  BorderLayout.WEST);
        add(buildTable(), BorderLayout.CENTER);
    }

    private JPanel buildForm() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(MainFrame.COL_WHITE);
        p.setPreferredSize(new Dimension(270, 0));
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 225, 235)),
                new EmptyBorder(20, 20, 20, 20)));

        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(6, 0, 6, 0);
        g.weightx = 1.0; g.gridx = 0;

        JLabel lForm = new JLabel("Open New Account");
        lForm.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lForm.setForeground(MainFrame.COL_PRIMARY);
        g.gridy = 0; p.add(lForm, g);

        g.gridy = 1; p.add(lbl("Select Customer"), g);
        cbCustomer = new JComboBox<>();
        cbCustomer.setFont(MainFrame.FONT_BODY);
        g.gridy = 2; p.add(cbCustomer, g);

        g.gridy = 3; p.add(lbl("Account Type"), g);
        cbType = new JComboBox<>(new String[]{"SAVINGS", "CURRENT", "FIXED_DEPOSIT"});
        cbType.setFont(MainFrame.FONT_BODY);
        g.gridy = 4; p.add(cbType, g);

        g.gridy = 5; p.add(lbl("Initial Deposit (₹)"), g);
        tfInitialBalance = new JTextField("0.00");
        tfInitialBalance.setFont(MainFrame.FONT_BODY);
        g.gridy = 6; p.add(tfInitialBalance, g);

        btnOpen     = btn("➕ Open Account",    MainFrame.COL_ACCENT);
        btnFreeze   = btn("❄️ Freeze Account",   new Color(52, 152, 219));
        btnActivate = btn("✅ Activate Account", new Color(39, 174, 96));
        btnDelete   = btn("🗑️ Delete Account",   new Color(231, 76, 60));
        btnFreeze.setEnabled(false);
        btnActivate.setEnabled(false);
        btnDelete.setEnabled(false);

        g.gridy = 7; g.insets = new Insets(14, 0, 6, 0); p.add(btnOpen, g);
        g.gridy = 8; g.insets = new Insets(0, 0, 6, 0);  p.add(btnFreeze, g);
        g.gridy = 9;  p.add(btnActivate, g);
        g.gridy = 10; p.add(btnDelete, g);
        g.gridy = 11; g.weighty = 1.0; p.add(Box.createVerticalGlue(), g);

        btnOpen.addActionListener(e -> openAccount());
        btnFreeze.addActionListener(e -> { service.freezeAccount(selectedAccountId); refresh(); });
        btnActivate.addActionListener(e -> { service.activateAccount(selectedAccountId); refresh(); });
        btnDelete.addActionListener(e -> deleteAccount());

        return p;
    }

    private JPanel buildTable() {
        JPanel p = new JPanel(new BorderLayout(0, 10));
        p.setOpaque(false);

        JButton btnRefresh = btn("↺ Refresh", new Color(100, 120, 150));
        btnRefresh.addActionListener(e -> refresh());
        JPanel top = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        top.setOpaque(false);
        top.add(btnRefresh);
        p.add(top, BorderLayout.NORTH);

        String[] cols = {"ID", "Account Number", "Customer", "Type", "Balance (₹)", "Status"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        styleTable(table);
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) onTableSelect();
        });
        p.add(new JScrollPane(table), BorderLayout.CENTER);
        return p;
    }

    private void openAccount() {
        Object sel = cbCustomer.getSelectedItem();
        if (sel == null) { JOptionPane.showMessageDialog(this, "Select a customer."); return; }
        int custId = Integer.parseInt(sel.toString().split(" - ")[0]);

        double balance;
        try { balance = Double.parseDouble(tfInitialBalance.getText().trim()); }
        catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid balance amount."); return;
        }

        Account a = new Account();
        a.setCustomerId(custId);
        a.setAccountType(AccountType.valueOf((String) cbType.getSelectedItem()));
        a.setBalance(balance);

        if (service.openAccount(a)) {
            JOptionPane.showMessageDialog(this,
                    "Account opened!\nAccount Number: " + a.getAccountNumber(), "Success",
                    JOptionPane.INFORMATION_MESSAGE);
            refresh();
        }
    }

    private void deleteAccount() {
        if (selectedAccountId < 0) return;
        int c = JOptionPane.showConfirmDialog(this,
                "Delete account " + selectedAccountId + "?", "Confirm",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (c == JOptionPane.YES_OPTION) { service.deleteAccount(selectedAccountId); refresh(); }
    }

    private void onTableSelect() {
        int row = table.getSelectedRow();
        if (row < 0) return;
        selectedAccountId = (int) tableModel.getValueAt(row, 0);
        btnFreeze.setEnabled(true);
        btnActivate.setEnabled(true);
        btnDelete.setEnabled(true);
    }

    public void refresh() {
        // Reload customer dropdown
        cbCustomer.removeAllItems();
        for (Customer c : service.getAllCustomers())
            cbCustomer.addItem(c.getCustomerId() + " - " + c.getFullName());

        // Reload table
        tableModel.setRowCount(0);
        for (Account a : service.getAllAccounts())
            tableModel.addRow(new Object[]{
                    a.getAccountId(), a.getAccountNumber(), a.getCustomerName(),
                    a.getAccountType(), String.format("%,.2f", a.getBalance()), a.getStatus()});
        selectedAccountId = -1;
        btnFreeze.setEnabled(false); btnActivate.setEnabled(false); btnDelete.setEnabled(false);
    }

    private JLabel lbl(String t) {
        JLabel l = new JLabel(t);
        l.setFont(new Font("Segoe UI", Font.BOLD, 12));
        l.setForeground(new Color(80, 90, 110));
        return l;
    }

    private JButton btn(String t, Color bg) {
        JButton b = new JButton(t);
        b.setFont(new Font("Segoe UI", Font.BOLD, 12));
        b.setBackground(bg); b.setForeground(Color.WHITE);
        b.setBorderPainted(false); b.setFocusPainted(false);
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
        t.getColumnModel().getColumn(0).setMaxWidth(50);
    }
}
