import javax.swing.*;
import javax.swing.border.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.*;
import java.nio.file.*;

/**
 * SetupWizard – run this ONCE to configure and build JavaBank.
 *
 * It will:
 *   1. Ask for MySQL host, username, password
 *   2. Ask you to browse for mysql-connector-java.jar
 *   3. Patch DBConnection.java with your credentials
 *   4. Copy the JAR into lib/
 *   5. Compile all Java sources
 *   6. Create  RunBank.bat  (Windows) and  RunBank.sh  (Linux/Mac)
 */
public class SetupWizard extends JFrame {

    // ── Colours ───────────────────────────────────────────────────
    private static final Color BLUE   = new Color(0, 82, 147);
    private static final Color GREEN  = new Color(0, 168, 120);
    private static final Color BG     = new Color(245, 247, 252);
    private static final Color WHITE  = Color.WHITE;
    private static final Font  BOLD   = new Font("Segoe UI", Font.BOLD, 13);
    private static final Font  PLAIN  = new Font("Segoe UI", Font.PLAIN, 13);

    // ── Form fields ───────────────────────────────────────────────
    private JTextField tfHost, tfPort, tfUser, tfJar;
    private JPasswordField pfPassword;
    private JTextArea  taLog;
    private JButton    btnBrowse, btnSetup, btnLaunch;
    private JProgressBar progress;

    // Project root = parent of setup/ folder
    private final File projectRoot;

    public SetupWizard() {
        // Determine project root (setup/ is inside it)
        projectRoot = new File(SetupWizard.class.getProtectionDomain()
                .getCodeSource().getLocation().getPath())
                .getParentFile()      // setup/
                .getParentFile();     // BankManagementSystem/

        buildUI();
    }

    // ─────────────────────────────────────────────────────────────
    private void buildUI() {
        setTitle("JavaBank – Setup Wizard");
        setSize(620, 680);
        setResizable(false);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(BG);
        setLayout(new BorderLayout(0, 0));

        add(buildHeader(),  BorderLayout.NORTH);
        add(buildCenter(),  BorderLayout.CENTER);
        add(buildFooter(),  BorderLayout.SOUTH);
    }

    // ── Header ────────────────────────────────────────────────────
    private JPanel buildHeader() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(BLUE);
        p.setBorder(new EmptyBorder(18, 24, 18, 24));

        JLabel t = new JLabel("🏦  JavaBank Setup Wizard");
        t.setFont(new Font("Segoe UI", Font.BOLD, 20));
        t.setForeground(WHITE);
        p.add(t, BorderLayout.WEST);

        JLabel sub = new JLabel("One-time configuration");
        sub.setFont(PLAIN);
        sub.setForeground(new Color(180, 210, 255));
        p.add(sub, BorderLayout.EAST);
        return p;
    }

    // ── Centre form ───────────────────────────────────────────────
    private JPanel buildCenter() {
        JPanel outer = new JPanel(new BorderLayout(0, 14));
        outer.setOpaque(false);
        outer.setBorder(new EmptyBorder(20, 24, 0, 24));

        // ── DB credentials card ───────────────────────────────────
        JPanel dbCard = card("Step 1 – MySQL Database Settings");
        dbCard.setLayout(new GridLayout(4, 2, 10, 10));

        dbCard.add(lbl("Host (usually localhost)"));
        tfHost = field("localhost");        dbCard.add(tfHost);

        dbCard.add(lbl("Port (usually 3306)"));
        tfPort = field("3306");             dbCard.add(tfPort);

        dbCard.add(lbl("Username"));
        tfUser = field("root");             dbCard.add(tfUser);

        dbCard.add(lbl("Password"));
        pfPassword = new JPasswordField();
        pfPassword.setFont(PLAIN);          dbCard.add(pfPassword);

        outer.add(dbCard, BorderLayout.NORTH);

        // ── JAR card ──────────────────────────────────────────────
        JPanel jarCard = card("Step 2 – MySQL JDBC Driver (.jar)");
        jarCard.setLayout(new BorderLayout(10, 8));

        JLabel hint = new JLabel("<html><font color='#555555'>Download from: <b>https://dev.mysql.com/downloads/connector/j/</b><br>" +
                "Select <b>Platform Independent</b> → extract the .jar → browse below.</font></html>");
        hint.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        jarCard.add(hint, BorderLayout.NORTH);

        JPanel jarRow = new JPanel(new BorderLayout(8, 0));
        jarRow.setOpaque(false);
        tfJar = field("Click Browse to select mysql-connector-j-x.x.x.jar");
        tfJar.setEditable(false);
        tfJar.setBackground(new Color(240, 244, 255));
        btnBrowse = btn("📂 Browse", BLUE);
        btnBrowse.setPreferredSize(new Dimension(110, 34));
        jarRow.add(tfJar,     BorderLayout.CENTER);
        jarRow.add(btnBrowse, BorderLayout.EAST);
        jarCard.add(jarRow, BorderLayout.CENTER);

        btnBrowse.addActionListener(e -> browseJar());

        // ── Log area ──────────────────────────────────────────────
        JPanel logCard = card("Build Log");
        logCard.setLayout(new BorderLayout());
        taLog = new JTextArea(8, 0);
        taLog.setFont(new Font("Consolas", Font.PLAIN, 12));
        taLog.setEditable(false);
        taLog.setBackground(new Color(20, 30, 45));
        taLog.setForeground(new Color(0, 230, 100));
        taLog.setBorder(new EmptyBorder(8, 10, 8, 10));
        logCard.add(new JScrollPane(taLog), BorderLayout.CENTER);

        progress = new JProgressBar(0, 100);
        progress.setStringPainted(true);
        progress.setString("Ready");
        progress.setFont(PLAIN);
        logCard.add(progress, BorderLayout.SOUTH);

        // Stack middle panels
        JPanel mid = new JPanel(new GridLayout(3, 1, 0, 14));
        mid.setOpaque(false);
        mid.add(jarCard);
        mid.add(logCard);

        JPanel centerWrap = new JPanel(new BorderLayout(0, 14));
        centerWrap.setOpaque(false);
        centerWrap.add(dbCard,  BorderLayout.NORTH);
        centerWrap.add(mid,     BorderLayout.CENTER);

        outer.add(centerWrap, BorderLayout.CENTER);
        return outer;
    }

    // ── Footer buttons ────────────────────────────────────────────
    private JPanel buildFooter() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT, 16, 14));
        p.setBackground(new Color(230, 235, 245));
        p.setBorder(new MatteBorder(1, 0, 0, 0, new Color(200, 210, 225)));

        btnLaunch = btn("▶ Launch App", GREEN);
        btnLaunch.setEnabled(false);
        btnLaunch.setToolTipText("Click Setup first");

        btnSetup = btn("⚙ Run Setup & Build", BLUE);
        btnSetup.setPreferredSize(new Dimension(180, 38));
        btnLaunch.setPreferredSize(new Dimension(150, 38));

        p.add(btnLaunch);
        p.add(btnSetup);

        btnSetup.addActionListener(e -> runSetup());
        btnLaunch.addActionListener(e -> launchApp());
        return p;
    }

    // ─────────────────────────────────────────────────────────────
    //  ACTIONS
    // ─────────────────────────────────────────────────────────────

    private void browseJar() {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Select mysql-connector-java.jar");
        fc.setFileFilter(new FileNameExtensionFilter("JAR files (*.jar)", "jar"));
        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
            tfJar.setText(fc.getSelectedFile().getAbsolutePath());
    }

    private void runSetup() {
        // Basic validation
        if (new String(pfPassword.getPassword()).isEmpty()) {
            warn("Please enter your MySQL password."); return;
        }
        if (tfJar.getText().startsWith("Click Browse")) {
            warn("Please browse and select the MySQL JDBC .jar file."); return;
        }
        File jarFile = new File(tfJar.getText());
        if (!jarFile.exists()) {
            warn("JAR file not found at: " + tfJar.getText()); return;
        }

        // Disable UI during build
        btnSetup.setEnabled(false);
        btnBrowse.setEnabled(false);
        taLog.setText("");

        // Run in background thread so GUI stays responsive
        new Thread(() -> {
            try {
                step1_patchDBConnection();
                step2_copyJar(jarFile);
                step3_compile();
                step4_createLaunchers();
                SwingUtilities.invokeLater(() -> {
                    progress.setValue(100);
                    progress.setString("✅ Setup Complete!");
                    btnLaunch.setEnabled(true);
                    JOptionPane.showMessageDialog(this,
                            "✅ Setup completed successfully!\n\nClick 'Launch App' to start JavaBank,\nor run RunBank.bat anytime in future.",
                            "Success", JOptionPane.INFORMATION_MESSAGE);
                });
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> {
                    log("❌ ERROR: " + ex.getMessage());
                    progress.setString("Failed – see log");
                    btnSetup.setEnabled(true);
                    btnBrowse.setEnabled(true);
                    warn("Setup failed:\n" + ex.getMessage());
                });
            }
        }).start();
    }

    // ── Step 1: patch DBConnection.java ──────────────────────────
    private void step1_patchDBConnection() throws Exception {
        setProgress(10, "Configuring database connection...");
        log("→ Patching DBConnection.java with your credentials...");

        String host     = tfHost.getText().trim();
        String port     = tfPort.getText().trim();
        String user     = tfUser.getText().trim();
        String password = new String(pfPassword.getPassword());

        File dbFile = new File(projectRoot,
                "src/bank/util/DBConnection.java");
        if (!dbFile.exists()) throw new Exception("Cannot find DBConnection.java at: " + dbFile);

        String content = Files.readString(dbFile.toPath());

        // Replace the three config lines
        content = content.replaceAll(
                "private static final String DB_URL.*?;",
                "private static final String DB_URL      = \"jdbc:mysql://" + host + ":" + port
                        + "/bank_db?useSSL=false&serverTimezone=UTC\";");
        content = content.replaceAll(
                "private static final String DB_USER.*?;",
                "private static final String DB_USER     = \"" + user + "\";");
        content = content.replaceAll(
                "private static final String DB_PASSWORD.*?;",
                "private static final String DB_PASSWORD = \"" + password + "\";");

        Files.writeString(dbFile.toPath(), content);
        log("   ✓ Credentials saved.");
    }

    // ── Step 2: copy JAR to lib/ ──────────────────────────────────
    private void step2_copyJar(File jarFile) throws Exception {
        setProgress(30, "Copying JDBC driver...");
        log("→ Copying " + jarFile.getName() + " to lib/...");

        File libDir = new File(projectRoot, "lib");
        libDir.mkdirs();

        // Remove any old connector jars first
        File[] old = libDir.listFiles(f -> f.getName().contains("mysql-connector"));
        if (old != null) for (File f : old) f.delete();

        Files.copy(jarFile.toPath(),
                new File(libDir, jarFile.getName()).toPath(),
                StandardCopyOption.REPLACE_EXISTING);
        log("   ✓ JAR copied to lib/" + jarFile.getName());
    }

    // ── Step 3: compile ───────────────────────────────────────────
    private void step3_compile() throws Exception {
        setProgress(55, "Compiling Java sources (this takes ~10 sec)...");
        log("→ Compiling all Java source files...");

        File outDir = new File(projectRoot, "out");
        outDir.mkdirs();

        // Collect all .java files
        StringBuilder sources = new StringBuilder();
        collectJava(new File(projectRoot, "src"), sources);

        String cp = new File(projectRoot, "lib").getAbsolutePath()
                + File.separator + "*";

        String[] cmd;
        if (isWindows()) {
            cmd = new String[]{"cmd", "/c",
                    "javac -cp \"" + cp + "\" -d \"" + outDir.getAbsolutePath()
                            + "\" " + sources.toString().trim()};
        } else {
            cmd = new String[]{"bash", "-c",
                    "javac -cp \"" + cp + "\" -d \"" + outDir.getAbsolutePath()
                            + "\" " + sources.toString().trim()};
        }

        Process proc = Runtime.getRuntime().exec(cmd);
        String stderr = new String(proc.getErrorStream().readAllBytes());
        int exit = proc.waitFor();

        if (exit != 0) throw new Exception("Compilation failed:\n" + stderr);
        log("   ✓ Compiled successfully.");
        setProgress(80, "Compilation done.");
    }

    // ── Step 4: create launchers ──────────────────────────────────
    private void step4_createLaunchers() throws Exception {
        setProgress(90, "Creating launch scripts...");
        log("→ Creating RunBank.bat (Windows) and RunBank.sh (Linux/Mac)...");

        String cp_win = "out;lib/*";
        String cp_unix = "out:lib/*";

        // Windows .bat
        String bat = "@echo off\r\n" +
                "cd /d \"%~dp0\"\r\n" +
                "java -cp \"" + cp_win + "\" bank.Main\r\n" +
                "if %ERRORLEVEL% NEQ 0 pause\r\n";
        Files.writeString(new File(projectRoot, "RunBank.bat").toPath(), bat);

        // Linux/Mac .sh
        String sh = "#!/bin/bash\n" +
                "cd \"$(dirname \"$0\")\"\n" +
                "java -cp \"" + cp_unix + "\" bank.Main\n";
        File shFile = new File(projectRoot, "RunBank.sh");
        Files.writeString(shFile.toPath(), sh);
        shFile.setExecutable(true);

        log("   ✓ RunBank.bat created  →  double-click to launch anytime!");
        log("   ✓ RunBank.sh  created  →  ./RunBank.sh on Linux/Mac");
        log("");
        log("════════════════════════════════════════");
        log("  🎉 SETUP COMPLETE! App is ready.");
        log("════════════════════════════════════════");
    }

    // ── Launch the actual bank app ────────────────────────────────
    private void launchApp() {
        try {
            String cp = isWindows()
                    ? new File(projectRoot, "out").getAbsolutePath() + ";"
                      + new File(projectRoot, "lib").getAbsolutePath() + "/*"
                    : new File(projectRoot, "out").getAbsolutePath() + ":"
                      + new File(projectRoot, "lib").getAbsolutePath() + "/*";

            String[] cmd = {"java", "-cp", cp, "bank.Main"};
            new ProcessBuilder(cmd)
                    .directory(projectRoot)
                    .inheritIO()
                    .start();

            log("▶ JavaBank launched!");
        } catch (Exception ex) {
            warn("Could not launch: " + ex.getMessage());
        }
    }

    // ─────────────────────────────────────────────────────────────
    //  Helpers
    // ─────────────────────────────────────────────────────────────

    private void collectJava(File dir, StringBuilder sb) {
        for (File f : dir.listFiles()) {
            if (f.isDirectory())            collectJava(f, sb);
            else if (f.getName().endsWith(".java"))
                sb.append("\"").append(f.getAbsolutePath()).append("\" ");
        }
    }

    private void log(String msg) {
        SwingUtilities.invokeLater(() -> {
            taLog.append(msg + "\n");
            taLog.setCaretPosition(taLog.getDocument().getLength());
        });
    }

    private void setProgress(int val, String label) {
        SwingUtilities.invokeLater(() -> {
            progress.setValue(val);
            progress.setString(label);
        });
    }

    private void warn(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Setup", JOptionPane.WARNING_MESSAGE);
    }

    private boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().contains("win");
    }

    // ── UI factory methods ────────────────────────────────────────
    private JPanel card(String title) {
        JPanel p = new JPanel();
        p.setBackground(WHITE);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(215, 225, 240)),
                new EmptyBorder(14, 16, 14, 16)));
        p.setLayout(new BorderLayout(8, 8));

        JLabel h = new JLabel(title);
        h.setFont(new Font("Segoe UI", Font.BOLD, 13));
        h.setForeground(BLUE);
        h.setBorder(new EmptyBorder(0, 0, 8, 0));
        p.add(h, BorderLayout.NORTH);
        return p;
    }

    private JLabel lbl(String t) {
        JLabel l = new JLabel(t);
        l.setFont(BOLD);
        l.setForeground(new Color(60, 70, 90));
        return l;
    }

    private JTextField field(String text) {
        JTextField tf = new JTextField(text);
        tf.setFont(PLAIN);
        tf.setPreferredSize(new Dimension(0, 32));
        return tf;
    }

    private JButton btn(String text, Color bg) {
        JButton b = new JButton(text);
        b.setFont(BOLD);
        b.setBackground(bg);
        b.setForeground(WHITE);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    // ─────────────────────────────────────────────────────────────
    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
        catch (Exception ignored) {}
        SwingUtilities.invokeLater(() -> new SetupWizard().setVisible(true));
    }
}
