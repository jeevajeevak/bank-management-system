package bank.ui;

import bank.model.Account;
import bank.model.Transaction;
import bank.service.BankService;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * TransactionPanel – deposit, withdrawal, and fund transfer.
 */
public class TransactionPanel extends JPanel {

    private final BankService service;

    // Deposit/Withdraw controls
    private JComboBox<String> cbAccount;
    private JTextField        tfAmount, tfDescription;
    private JRadioButton      rbDeposit, rbWithdraw;

    // Transfer controls
    private JComboBox<String> cbFromAccount;
    private JTextField        tfToAccountNum, tfTransferAmount;

    // History table
    private JTable            table;
    private DefaultTableModel tableModel;
    private JComboBox<String> cbHistoryAccount;

    public TransactionPanel(BankService service) {
        this.service = service;
        setBackground(MainFrame.COL_BG);
        setLayout(new BorderLayout(15, 15));
        setBorder(new EmptyBorder(25, 25, 25, 25));
        buildUI();
    }

    private void buildUI() {
        JLabel title = new JLabel("Transactions");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(MainFrame.COL_TEXT);
        add(title, BorderLayout.NORTH);

        JPanel leftPanel = new JPanel(new GridLayout(2, 1, 0, 15));
        leftPanel.setOpaque(false);
        leftPanel.setPreferredSize(new Dimension(300, 0));
        leftPanel.add(buildDepositWithdrawPanel());
        leftPanel.add(buildTransferPanel());
        add(leftPanel, BorderLayout.WEST);

        add(buildHistoryPanel(), BorderLayout.CENTER);
    }

    // ── Deposit / Withdraw ────────────────────────────────────────
    private JPanel buildDepositWithdrawPanel() {
        JPanel p = card("Deposit / Withdrawal");
        GridBagConstraints g = gbc();

        g.gridy = 1; p.add(lbl("Account"), g);
        cbAccount = combo(); g.gridy = 2; p.add(cbAccount, g);

        g.gridy = 3; p.add(lbl("Amount (₹)"), g);
        tfAmount = field(""); g.gridy = 4; p.add(tfAmount, g);

        g.gridy = 5; p.add(lbl("Description"), g);
        tfDescription = field(""); g.gridy = 6; p.add(tfDescription, g);

        // Radio buttons
        rbDeposit  = new JRadioButton("Deposit",    true);
        rbWithdraw = new JRadioButton("Withdrawal", false);
        ButtonGroup bg = new ButtonGroup();
        bg.add(rbDeposit); bg.add(rbWithdraw);
        styleRadio(rbDeposit); styleRadio(rbWithdraw);
        JPanel radioRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        radioRow.setOpaque(false);
        radioRow.add(rbDeposit); radioRow.add(rbWithdraw);
        g.gridy = 7; p.add(radioRow, g);

        JButton btnExec = btn("▶ Execute", MainFrame.COL_ACCENT);
        g.gridy = 8; g.insets = new Insets(14, 0, 0, 0); p.add(btnExec, g);
        btnExec.addActionListener(e -> executeTransaction());

        g.gridy = 9; g.weighty = 1; p.add(Box.createVerticalGlue(), g);
        return p;
    }

    // ── Transfer ──────────────────────────────────────────────────
    private JPanel buildTransferPanel() {
        JPanel p = card("Fund Transfer");
        GridBagConstraints g = gbc();

        g.gridy = 1; p.add(lbl("From Account"), g);
        cbFromAccount = combo(); g.gridy = 2; p.add(cbFromAccount, g);

        g.gridy = 3; p.add(lbl("To Account Number"), g);
        tfToAccountNum = field("ACC000000001"); g.gridy = 4; p.add(tfToAccountNum, g);

        g.gridy = 5; p.add(lbl("Amount (₹)"), g);
        tfTransferAmount = field(""); g.gridy = 6; p.add(tfTransferAmount, g);

        JButton btnTransfer = btn("↔ Transfer Funds", new Color(52, 152, 219));
        g.gridy = 7; g.insets = new Insets(14, 0, 0, 0); p.add(btnTransfer, g);
        btnTransfer.addActionListener(e -> executeTransfer());

        g.gridy = 8; g.weighty = 1; p.add(Box.createVerticalGlue(), g);
        return p;
    }

    // ── History ───────────────────────────────────────────────────
    private JPanel buildHistoryPanel() {
        JPanel p = new JPanel(new BorderLayout(0, 10));
        p.setOpaque(false);

        JPanel topRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        topRow.setOpaque(false);
        topRow.add(new JLabel("View History for Account:"));
        cbHistoryAccount = combo();
        cbHistoryAccount.setPreferredSize(new Dimension(200, 30));
        topRow.add(cbHistoryAccount);
        JButton btnLoad = btn("Load", MainFrame.COL_PRIMARY);
        btnLoad.addActionListener(e -> loadHistory());
        topRow.add(btnLoad);
        p.add(topRow, BorderLayout.NORTH);

        String[] cols = {"ID", "Type", "Amount (₹)", "Description", "Date"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        styleTable(table);
        p.add(new JScrollPane(table), BorderLayout.CENTER);
        return p;
    }

    // ── Logic ─────────────────────────────────────────────────────
    private void executeTransaction() {
        Object sel = cbAccount.getSelectedItem();
        if (sel == null) { msg("Select an account."); return; }
        int accId = Integer.parseInt(sel.toString().split(" \\|")[0].trim());

        double amount;
        try { amount = Double.parseDouble(tfAmount.getText().trim()); }
        catch (NumberFormatException e) { msg("Enter a valid amount."); return; }

        String desc = tfDescription.getText().trim();
        String error = rbDeposit.isSelected()
                ? service.deposit(accId, amount, desc)
                : service.withdraw(accId, amount, desc);

        if (error == null) {
            JOptionPane.showMessageDialog(this,
                    (rbDeposit.isSelected() ? "Deposit" : "Withdrawal") + " successful!",
                    "Success", JOptionPane.INFORMATION_MESSAGE);
            tfAmount.setText(""); tfDescription.setText("");
            refresh();
        } else {
            msg(error);
        }
    }

    private void executeTransfer() {
        Object sel = cbFromAccount.getSelectedItem();
        if (sel == null) { msg("Select source account."); return; }
        int fromId = Integer.parseInt(sel.toString().split(" \\|")[0].trim());
        String toNum = tfToAccountNum.getText().trim();

        double amount;
        try { amount = Double.parseDouble(tfTransferAmount.getText().trim()); }
        catch (NumberFormatException e) { msg("Enter a valid amount."); return; }

        String error = service.transfer(fromId, toNum, amount);
        if (error == null) {
            JOptionPane.showMessageDialog(this, "Transfer successful!", "Success",
                    JOptionPane.INFORMATION_MESSAGE);
            tfTransferAmount.setText(""); refresh();
        } else {
            msg(error);
        }
    }

    private void loadHistory() {
        Object sel = cbHistoryAccount.getSelectedItem();
        if (sel == null) return;
        int accId = Integer.parseInt(sel.toString().split(" \\|")[0].trim());
        List<Transaction> list = service.getTransactionHistory(accId);
        tableModel.setRowCount(0);
        for (Transaction t : list)
            tableModel.addRow(new Object[]{
                    t.getTransactionId(), t.getType(),
                    String.format("%,.2f", t.getAmount()),
                    t.getDescription(),
                    t.getTransactionDate() != null ? t.getTransactionDate().toString() : "-"});
    }

    public void refresh() {
        cbAccount.removeAllItems();
        cbFromAccount.removeAllItems();
        cbHistoryAccount.removeAllItems();
        for (Account a : service.getAllAccounts()) {
            String entry = a.getAccountId() + " | " + a.getAccountNumber() + " | " + a.getCustomerName();
            cbAccount.addItem(entry);
            cbFromAccount.addItem(entry);
            cbHistoryAccount.addItem(entry);
        }
    }

    // ── UI Helpers ────────────────────────────────────────────────
    private JPanel card(String heading) {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(MainFrame.COL_WHITE);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 225, 235)),
                new EmptyBorder(18, 18, 18, 18)));
        GridBagConstraints g = gbc();
        JLabel h = new JLabel(heading);
        h.setFont(new Font("Segoe UI", Font.BOLD, 15));
        h.setForeground(MainFrame.COL_PRIMARY);
        g.gridy = 0; p.add(h, g);
        return p;
    }

    private GridBagConstraints gbc() {
        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(5, 0, 5, 0);
        g.weightx = 1.0; g.gridx = 0;
        return g;
    }

    private JLabel     lbl(String t)   { JLabel l = new JLabel(t); l.setFont(new Font("Segoe UI", Font.BOLD, 12)); l.setForeground(new Color(80, 90, 110)); return l; }
    private JTextField field(String d) { JTextField tf = new JTextField(d); tf.setFont(MainFrame.FONT_BODY); tf.setPreferredSize(new Dimension(0, 30)); return tf; }
    private JComboBox<String> combo()  { JComboBox<String> cb = new JComboBox<>(); cb.setFont(MainFrame.FONT_BODY); return cb; }

    private JButton btn(String t, Color bg) {
        JButton b = new JButton(t);
        b.setFont(new Font("Segoe UI", Font.BOLD, 12));
        b.setBackground(bg); b.setForeground(Color.WHITE);
        b.setBorderPainted(false); b.setFocusPainted(false);
        b.setPreferredSize(new Dimension(0, 34));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    private void styleRadio(JRadioButton r) {
        r.setFont(MainFrame.FONT_BODY);
        r.setOpaque(false);
        r.setBorder(new EmptyBorder(0, 0, 0, 15));
    }

    private void styleTable(JTable t) {
        t.setFont(MainFrame.FONT_BODY); t.setRowHeight(28);
        t.setGridColor(new Color(230, 235, 245));
        t.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        t.getTableHeader().setBackground(MainFrame.COL_PRIMARY);
        t.getTableHeader().setForeground(Color.WHITE);
        t.setSelectionBackground(new Color(200, 220, 255));
        t.setShowVerticalLines(false);
    }

    private void msg(String m) {
        JOptionPane.showMessageDialog(this, m, "Error", JOptionPane.ERROR_MESSAGE);
    }
}
