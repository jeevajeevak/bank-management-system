package bank.dao;

import bank.model.Account;
import bank.model.Account.AccountStatus;
import bank.model.Account.AccountType;
import bank.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * AccountDAO – all database operations for the accounts table.
 */
public class AccountDAO {

    // ── CREATE ────────────────────────────────────────────────────
    public boolean addAccount(Account a) {
        String sql = "INSERT INTO accounts (account_number, customer_id, account_type, balance) VALUES (?,?,?,?)";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql,
                Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, a.getAccountNumber());
            ps.setInt(2, a.getCustomerId());
            ps.setString(3, a.getAccountType().name());
            ps.setDouble(4, a.getBalance());
            ps.executeUpdate();

            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) a.setAccountId(keys.getInt(1));
            return true;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    // ── READ ALL (with customer name join) ────────────────────────
    public List<Account> getAllAccounts() {
        List<Account> list = new ArrayList<>();
        String sql = "SELECT a.*, c.full_name FROM accounts a " +
                     "JOIN customers c ON a.customer_id = c.customer_id ORDER BY a.account_id";
        try (Statement st = DBConnection.getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    // ── READ ONE ──────────────────────────────────────────────────
    public Account getAccountById(int id) {
        String sql = "SELECT a.*, c.full_name FROM accounts a " +
                     "JOIN customers c ON a.customer_id = c.customer_id WHERE a.account_id = ?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public Account getAccountByNumber(String accNum) {
        String sql = "SELECT a.*, c.full_name FROM accounts a " +
                     "JOIN customers c ON a.customer_id = c.customer_id WHERE a.account_number = ?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, accNum);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    // Get all accounts for a specific customer
    public List<Account> getAccountsByCustomer(int customerId) {
        List<Account> list = new ArrayList<>();
        String sql = "SELECT a.*, c.full_name FROM accounts a " +
                     "JOIN customers c ON a.customer_id = c.customer_id WHERE a.customer_id = ?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, customerId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    // ── UPDATE BALANCE ────────────────────────────────────────────
    public boolean updateBalance(int accountId, double newBalance) {
        String sql = "UPDATE accounts SET balance = ? WHERE account_id = ?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setDouble(1, newBalance);
            ps.setInt(2, accountId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    // ── UPDATE STATUS ─────────────────────────────────────────────
    public boolean updateStatus(int accountId, AccountStatus status) {
        String sql = "UPDATE accounts SET status = ? WHERE account_id = ?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, status.name());
            ps.setInt(2, accountId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    // ── DELETE ────────────────────────────────────────────────────
    public boolean deleteAccount(int id) {
        String sql = "DELETE FROM accounts WHERE account_id = ?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    // ── Generate a new unique account number ──────────────────────
    public String generateAccountNumber() {
        String sql = "SELECT COUNT(*) FROM accounts";
        try (Statement st = DBConnection.getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) {
                int count = rs.getInt(1) + 1;
                return String.format("ACC%09d", count);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return "ACC000000001";
    }

    // ── Helper ────────────────────────────────────────────────────
    private Account mapRow(ResultSet rs) throws SQLException {
        Account a = new Account();
        a.setAccountId(rs.getInt("account_id"));
        a.setAccountNumber(rs.getString("account_number"));
        a.setCustomerId(rs.getInt("customer_id"));
        a.setCustomerName(rs.getString("full_name"));
        a.setAccountType(AccountType.valueOf(rs.getString("account_type")));
        a.setBalance(rs.getDouble("balance"));
        a.setStatus(AccountStatus.valueOf(rs.getString("status")));
        return a;
    }
}
