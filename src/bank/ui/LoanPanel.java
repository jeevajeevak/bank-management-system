package bank.ui;

import bank.model.Customer;
import bank.model.Loan;
import bank.model.Loan.LoanType;
import bank.service.BankService;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/**
 * LoanPanel – apply for loans, approve/reject, view all loans, pay EMI.
 */
public class LoanPanel extends JPanel {

    private final BankService service;

    private JComboBox<String> cbCustomer, cbLoanType;
    private JTextField        tfPrincipal, tfRate, tfDuration, tfEmiPreview;
    private JTable            table;
    private DefaultTableModel tableModel;
    private JButton           btnApprove, btnReject, btnPayEMI;

    private int selectedLoanId = -1;

    public LoanPanel(BankService service) {
        this.service = service;
        setBackground(MainFrame.COL_BG);
        setLayout(new BorderLayout(15, 15));
        setBorder(new EmptyBorder(25, 25, 25, 25));
        buildUI();
    }

    private void buildUI() {
        JLabel title = new JLabel("Loan Management");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(MainFrame.COL_TEXT);
        add(title, BorderLayout.NORTH);
        add(buildForm(),  BorderLayout.WEST);
        add(buildTable(), BorderLayout.CENTER);
    }

    private JPanel buildForm() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(MainFrame.COL_WHITE);
        p.setPreferredSize(new Dimension(280, 0));
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 225, 235)),
                new EmptyBorder(20, 20, 20, 20)));

        GridBagConstraints g = gbc();

        JLabel h = new JLabel("Apply for Loan");
        h.setFont(new Font("Segoe UI", Font.BOLD, 15));
        h.setForeground(MainFrame.COL_PRIMARY);
        g.gridy = 0; p.add(h, g);

        g.gridy = 1; p.add(lbl("Customer"), g);
        cbCustomer = new JComboBox<>(); cbCustomer.setFont(MainFrame.FONT_BODY);
        g.gridy = 2; p.add(cbCustomer, g);

        g.gridy = 3; p.add(lbl("Loan Type"), g);
        cbLoanType = new JComboBox<>(new String[]{"HOME","CAR","PERSONAL","EDUCATION"});
        cbLoanType.setFont(MainFrame.FONT_BODY);
        g.gridy = 4; p.add(cbLoanType, g);

        g.gridy = 5; p.add(lbl("Principal Amount (₹)"), g);
        tfPrincipal = field(""); g.gridy = 6; p.add(tfPrincipal, g);

        g.gridy = 7; p.add(lbl("Annual Interest Rate (%)"), g);
        tfRate = field("8.5"); g.gridy = 8; p.add(tfRate, g);

        g.gridy = 9; p.add(lbl("Duration (months)"), g);
        tfDuration = field("60"); g.gridy = 10; p.add(tfDuration, g);

        g.gridy = 11; p.add(lbl("Monthly EMI (₹) — auto"), g);
        tfEmiPreview = field("");
        tfEmiPreview.setEditable(false);
        tfEmiPreview.setBackground(new Color(240, 248, 255));
        g.gridy = 12; p.add(tfEmiPreview, g);

        // Live EMI preview on input
        java.awt.event.KeyAdapter calc = new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent e) { updateEMIPreview(); }
        };
        tfPrincipal.addKeyListener(calc);
        tfRate.addKeyListener(calc);
        tfDuration.addKeyListener(calc);

        JButton btnApply = btn("📋 Apply for Loan", MainFrame.COL_ACCENT);
        g.gridy = 13; g.insets = new Insets(14, 0, 8, 0); p.add(btnApply, g);
        btnApply.addActionListener(e -> applyLoan());

        // Actions on selected loan
        JLabel lActions = new JLabel("Selected Loan Actions");
        lActions.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lActions.setForeground(MainFrame.COL_TEXT);
        g.gridy = 14; g.insets = new Insets(10, 0, 6, 0); p.add(lActions, g);

        btnApprove = btn("✅ Approve",  new Color(39, 174, 96));
        btnReject  = btn("❌ Reject",   new Color(231, 76, 60));
        btnPayEMI  = btn("💳 Pay EMI",  new Color(52, 152, 219));
        btnApprove.setEnabled(false); btnReject.setEnabled(false); btnPayEMI.setEnabled(false);

        g.gridy = 15; g.insets = new Insets(4, 0, 4, 0); p.add(btnApprove, g);
        g.gridy = 16; p.add(btnReject, g);
        g.gridy = 17; p.add(btnPayEMI, g);

        g.gridy = 18; g.weighty = 1; p.add(Box.createVerticalGlue(), g);

        btnApprove.addActionListener(e -> { service.approveLoan(selectedLoanId); refresh(); });
        btnReject.addActionListener(e  -> { service.rejectLoan(selectedLoanId);  refresh(); });
        btnPayEMI.addActionListener(e  -> payEMI());

        return p;
    }

    private JPanel buildTable() {
        JPanel p = new JPanel(new BorderLayout(0, 10));
        p.setOpaque(false);

        JButton btnRefresh = btn("↺ Refresh", new Color(100, 120, 150));
        btnRefresh.addActionListener(e -> refresh());
        JPanel top = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        top.setOpaque(false); top.add(btnRefresh);
        p.add(top, BorderLayout.NORTH);

        String[] cols = {"ID","Customer","Type","Principal (₹)","Rate","Months","EMI (₹)","Paid (₹)","Status"};
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

    // ── Logic ─────────────────────────────────────────────────────
    private void applyLoan() {
        Object sel = cbCustomer.getSelectedItem();
        if (sel == null) { msg("Select a customer."); return; }
        int custId = Integer.parseInt(sel.toString().split(" - ")[0]);

        try {
            double principal = Double.parseDouble(tfPrincipal.getText().trim());
            double rate      = Double.parseDouble(tfRate.getText().trim());
            int    months    = Integer.parseInt(tfDuration.getText().trim());

            Loan l = new Loan(custId, LoanType.valueOf((String) cbLoanType.getSelectedItem()),
                    principal, rate, months);
            if (service.applyForLoan(l)) {
                JOptionPane.showMessageDialog(this,
                        "Loan application submitted!\nEMI: ₹" + String.format("%,.2f", l.getEmi()),
                        "Success", JOptionPane.INFORMATION_MESSAGE);
                tfPrincipal.setText(""); tfEmiPreview.setText(""); refresh();
            }
        } catch (NumberFormatException ex) { msg("Enter valid numbers for Principal, Rate, and Duration."); }
    }

    private void payEMI() {
        if (selectedLoanId < 0) return;
        String error = service.payEMI(selectedLoanId);
        if (error == null) {
            JOptionPane.showMessageDialog(this, "EMI payment recorded!", "Success",
                    JOptionPane.INFORMATION_MESSAGE);
            refresh();
        } else { msg(error); }
    }

    private void updateEMIPreview() {
        try {
            double p = Double.parseDouble(tfPrincipal.getText().trim());
            double r = Double.parseDouble(tfRate.getText().trim());
            int    n = Integer.parseInt(tfDuration.getText().trim());
            double emi = Loan.calculateEMI(p, r, n);
            tfEmiPreview.setText(String.format("₹%,.2f", emi));
        } catch (NumberFormatException ex) { tfEmiPreview.setText(""); }
    }

    private void onTableSelect() {
        int row = table.getSelectedRow();
        if (row < 0) return;
        selectedLoanId = (int) tableModel.getValueAt(row, 0);
        btnApprove.setEnabled(true); btnReject.setEnabled(true); btnPayEMI.setEnabled(true);
    }

    public void refresh() {
        cbCustomer.removeAllItems();
        for (Customer c : service.getAllCustomers())
            cbCustomer.addItem(c.getCustomerId() + " - " + c.getFullName());

        tableModel.setRowCount(0);
        for (Loan l : service.getAllLoans())
            tableModel.addRow(new Object[]{
                    l.getLoanId(), l.getCustomerName(), l.getLoanType(),
                    String.format("%,.2f", l.getPrincipal()),
                    l.getInterestRate() + "%",
                    l.getDurationMonths(),
                    String.format("%,.2f", l.getEmi()),
                    String.format("%,.2f", l.getAmountPaid()),
                    l.getStatus()});
        selectedLoanId = -1;
        btnApprove.setEnabled(false); btnReject.setEnabled(false); btnPayEMI.setEnabled(false);
    }

    // ── UI Helpers ────────────────────────────────────────────────
    private GridBagConstraints gbc() {
        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(5, 0, 5, 0);
        g.weightx = 1.0; g.gridx = 0;
        return g;
    }

    private JLabel     lbl(String t)   { JLabel l = new JLabel(t); l.setFont(new Font("Segoe UI", Font.BOLD, 12)); l.setForeground(new Color(80, 90, 110)); return l; }
    private JTextField field(String d) { JTextField tf = new JTextField(d); tf.setFont(MainFrame.FONT_BODY); tf.setPreferredSize(new Dimension(0, 30)); return tf; }

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
        t.setFont(MainFrame.FONT_BODY); t.setRowHeight(28);
        t.setGridColor(new Color(230, 235, 245));
        t.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        t.getTableHeader().setBackground(MainFrame.COL_PRIMARY);
        t.getTableHeader().setForeground(Color.WHITE);
        t.setSelectionBackground(new Color(200, 220, 255));
        t.setShowVerticalLines(false);
        t.getColumnModel().getColumn(0).setMaxWidth(40);
    }

    private void msg(String m) {
        JOptionPane.showMessageDialog(this, m, "Error", JOptionPane.ERROR_MESSAGE);
    }
}
