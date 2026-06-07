package bank.ui;

import bank.model.*;
import bank.service.BankService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

/**
 * DashboardPanel – the home page.
 * Shows: total customers, accounts, deposits, recent transactions.
 */
public class DashboardPanel extends JPanel {

    private final BankService service;

    private JLabel lblCustomers, lblAccounts, lblBalance, lblLoans;
    private JTextArea txRecent;

    public DashboardPanel(BankService service) {
        this.service = service;
        setBackground(MainFrame.COL_BG);
        setLayout(new BorderLayout(20, 20));
        setBorder(new EmptyBorder(30, 30, 30, 30));
        buildUI();
    }

    private void buildUI() {
        // ── Title ─────────────────────────────────────────────────
        JLabel title = new JLabel("Dashboard Overview");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(MainFrame.COL_TEXT);
        add(title, BorderLayout.NORTH);

        // ── Stat cards (top row) ──────────────────────────────────
        JPanel cards = new JPanel(new GridLayout(1, 4, 15, 0));
        cards.setOpaque(false);

        lblCustomers = statCard(cards, "Total Customers", "0",  new Color(52, 152, 219));
        lblAccounts  = statCard(cards, "Active Accounts",  "0",  new Color(46, 204, 113));
        lblBalance   = statCard(cards, "Total Deposits",   "₹0", new Color(155, 89, 182));
        lblLoans     = statCard(cards, "Active Loans",     "0",  new Color(230, 126, 34));

        add(cards, BorderLayout.CENTER);

        // ── Recent transactions ────────────────────────────────────
        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setOpaque(false);

        JLabel recentTitle = new JLabel("Recent Transactions (last 10)");
        recentTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        recentTitle.setBorder(new EmptyBorder(0, 0, 8, 0));
        bottom.add(recentTitle, BorderLayout.NORTH);

        txRecent = new JTextArea(12, 0);
        txRecent.setFont(new Font("Consolas", Font.PLAIN, 12));
        txRecent.setEditable(false);
        txRecent.setBackground(MainFrame.COL_WHITE);
        txRecent.setBorder(new EmptyBorder(10, 10, 10, 10));
        bottom.add(new JScrollPane(txRecent), BorderLayout.CENTER);

        add(bottom, BorderLayout.SOUTH);
    }

    /** Creates one coloured stat card and returns the value label for later update. */
    private JLabel statCard(JPanel parent, String title, String value, Color color) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(color);
        card.setBorder(new EmptyBorder(20, 20, 20, 20));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(color.darker(), 1),
                new EmptyBorder(20, 20, 20, 20)));

        JLabel lTitle = new JLabel(title);
        lTitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lTitle.setForeground(new Color(255, 255, 255, 200));
        card.add(lTitle, BorderLayout.NORTH);

        JLabel lValue = new JLabel(value);
        lValue.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lValue.setForeground(Color.WHITE);
        card.add(lValue, BorderLayout.CENTER);

        // Rounded feel with drop shadow
        card.putClientProperty("html.disable", Boolean.FALSE);
        parent.add(card);
        return lValue;
    }

    /** Called every time the dashboard tab is selected. */
    public void refresh() {
        List<Customer> customers = service.getAllCustomers();
        List<Account>  accounts  = service.getAllAccounts();
        List<Loan>     loans     = service.getAllLoans();

        lblCustomers.setText(String.valueOf(customers.size()));

        long activeAccounts = accounts.stream()
                .filter(a -> a.getStatus() == Account.AccountStatus.ACTIVE).count();
        lblAccounts.setText(String.valueOf(activeAccounts));

        double totalBalance = accounts.stream().mapToDouble(Account::getBalance).sum();
        lblBalance.setText(String.format("₹%,.0f", totalBalance));

        long activeLoans = loans.stream()
                .filter(l -> l.getStatus() == Loan.LoanStatus.ACTIVE).count();
        lblLoans.setText(String.valueOf(activeLoans));

        // Recent transactions
        List<Transaction> recent = service.getRecentTransactions(10);
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%-6s  %-10s  %-15s  %-12s  %s%n",
                "ID", "Account", "Type", "Amount", "Description"));
        sb.append("─".repeat(70)).append("\n");
        for (Transaction t : recent) {
            sb.append(String.format("%-6d  %-10d  %-15s  ₹%,10.2f  %s%n",
                    t.getTransactionId(), t.getAccountId(),
                    t.getType(), t.getAmount(), t.getDescription()));
        }
        if (recent.isEmpty()) sb.append("  No transactions yet.");
        txRecent.setText(sb.toString());
        txRecent.setCaretPosition(0);
    }
}
