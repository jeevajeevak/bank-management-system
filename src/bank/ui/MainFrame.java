package bank.ui;

import bank.service.BankService;
import bank.util.DBConnection;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * MainFrame – the root application window.
 *
 * Layout:
 *   ┌──────────────────────────────────────────┐
 *   │  Header (Bank Name + Logo)               │
 *   ├────────────┬─────────────────────────────┤
 *   │ Sidebar    │  Content Panel (CardLayout) │
 *   │ (nav menu) │                             │
 *   └────────────┴─────────────────────────────┘
 */
public class MainFrame extends JFrame {

    // Shared service instance (passed to all panels)
    private final BankService service = new BankService();

    // Colour palette
    public static final Color COL_PRIMARY    = new Color(0, 82, 147);   // Deep bank blue
    public static final Color COL_ACCENT     = new Color(0, 168, 120);  // Green
    public static final Color COL_SIDEBAR_BG = new Color(20, 40, 70);
    public static final Color COL_BG         = new Color(245, 247, 252);
    public static final Color COL_WHITE      = Color.WHITE;
    public static final Color COL_TEXT       = new Color(30, 30, 50);
    public static final Font  FONT_TITLE     = new Font("Segoe UI", Font.BOLD,  22);
    public static final Font  FONT_NAV       = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font  FONT_BODY      = new Font("Segoe UI", Font.PLAIN, 13);

    // CardLayout to swap between pages
    private final CardLayout   cardLayout  = new CardLayout();
    private final JPanel       contentArea = new JPanel(cardLayout);

    // Individual panels
    private DashboardPanel    dashboardPanel;
    private CustomerPanel     customerPanel;
    private AccountPanel      accountPanel;
    private TransactionPanel  transactionPanel;
    private LoanPanel         loanPanel;

    public MainFrame() {
        initUI();
    }

    private void initUI() {
        setTitle("JavaBank – Management System");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(1200, 750);
        setMinimumSize(new Dimension(900, 600));
        setLocationRelativeTo(null);

        // Graceful shutdown on close
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override public void windowClosing(java.awt.event.WindowEvent e) {
                DBConnection.closeConnection();
                System.exit(0);
            }
        });

        setLayout(new BorderLayout());

        add(buildHeader(),  BorderLayout.NORTH);
        add(buildSidebar(), BorderLayout.WEST);
        add(buildContent(), BorderLayout.CENTER);

        // Show dashboard first
        showPanel("dashboard");
    }

    // ── Header bar ────────────────────────────────────────────────
    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(COL_PRIMARY);
        header.setPreferredSize(new Dimension(0, 60));
        header.setBorder(new EmptyBorder(0, 20, 0, 20));

        JLabel logo = new JLabel("🏦  JavaBank Management System");
        logo.setFont(FONT_TITLE);
        logo.setForeground(COL_WHITE);
        header.add(logo, BorderLayout.WEST);

        JLabel version = new JLabel("v1.0  |  Bangalore Branch");
        version.setFont(FONT_BODY);
        version.setForeground(new Color(180, 210, 255));
        header.add(version, BorderLayout.EAST);

        return header;
    }

    // ── Left sidebar ──────────────────────────────────────────────
    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setBackground(COL_SIDEBAR_BG);
        sidebar.setPreferredSize(new Dimension(200, 0));
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBorder(new EmptyBorder(20, 0, 20, 0));

        String[][] items = {
            { "📊", "Dashboard",    "dashboard"    },
            { "👤", "Customers",    "customers"    },
            { "💳", "Accounts",     "accounts"     },
            { "💸", "Transactions", "transactions" },
            { "🏠", "Loans",        "loans"        },
        };

        for (String[] item : items) {
            sidebar.add(navButton(item[0] + "  " + item[1], item[2]));
            sidebar.add(Box.createRigidArea(new Dimension(0, 4)));
        }

        sidebar.add(Box.createVerticalGlue());
        return sidebar;
    }

    private JButton navButton(String label, String panelName) {
        JButton btn = new JButton(label);
        btn.setFont(FONT_NAV);
        btn.setForeground(new Color(200, 220, 255));
        btn.setBackground(COL_SIDEBAR_BG);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setMaximumSize(new Dimension(200, 44));
        btn.setPreferredSize(new Dimension(200, 44));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setBorder(new EmptyBorder(0, 20, 0, 0));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) {
                btn.setBackground(new Color(40, 70, 110));
            }
            public void mouseExited(java.awt.event.MouseEvent e) {
                btn.setBackground(COL_SIDEBAR_BG);
            }
        });

        btn.addActionListener(e -> showPanel(panelName));
        return btn;
    }

    // ── Content area (swappable panels) ──────────────────────────
    private JPanel buildContent() {
        contentArea.setBackground(COL_BG);

        dashboardPanel   = new DashboardPanel(service);
        customerPanel    = new CustomerPanel(service);
        accountPanel     = new AccountPanel(service);
        transactionPanel = new TransactionPanel(service);
        loanPanel        = new LoanPanel(service);

        contentArea.add(dashboardPanel,   "dashboard");
        contentArea.add(customerPanel,    "customers");
        contentArea.add(accountPanel,     "accounts");
        contentArea.add(transactionPanel, "transactions");
        contentArea.add(loanPanel,        "loans");

        return contentArea;
    }

    public void showPanel(String name) {
        cardLayout.show(contentArea, name);
        // Refresh data whenever a panel becomes visible
        switch (name) {
            case "dashboard":    dashboardPanel.refresh();    break;
            case "customers":    customerPanel.refresh();     break;
            case "accounts":     accountPanel.refresh();      break;
            case "transactions": transactionPanel.refresh();  break;
            case "loans":        loanPanel.refresh();         break;
        }
    }
}
