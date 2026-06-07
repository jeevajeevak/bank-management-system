package bank.dao;

import bank.model.Transaction;
import bank.model.Transaction.TransactionType;
import bank.util.DBConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * TransactionDAO – all database operations for the transactions table.
 */
public class TransactionDAO {

    // ── CREATE ────────────────────────────────────────────────────
    public boolean addTransaction(Transaction t) {
        String sql = "INSERT INTO transactions (account_id, type, amount, description) VALUES (?,?,?,?)";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql,
                Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, t.getAccountId());
            ps.setString(2, t.getType().name());
            ps.setDouble(3, t.getAmount());
            ps.setString(4, t.getDescription());
            ps.executeUpdate();

            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) t.setTransactionId(keys.getInt(1));
            return true;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    // ── READ by account ───────────────────────────────────────────
    public List<Transaction> getTransactionsByAccount(int accountId) {
        List<Transaction> list = new ArrayList<>();
        String sql = "SELECT * FROM transactions WHERE account_id = ? ORDER BY transaction_date DESC";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, accountId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    // ── READ recent (all accounts) ────────────────────────────────
    public List<Transaction> getRecentTransactions(int limit) {
        List<Transaction> list = new ArrayList<>();
        String sql = "SELECT * FROM transactions ORDER BY transaction_date DESC LIMIT ?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, limit);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    // ── Helper ────────────────────────────────────────────────────
    private Transaction mapRow(ResultSet rs) throws SQLException {
        Transaction t = new Transaction();
        t.setTransactionId(rs.getInt("transaction_id"));
        t.setAccountId(rs.getInt("account_id"));
        t.setType(TransactionType.valueOf(rs.getString("type")));
        t.setAmount(rs.getDouble("amount"));
        t.setDescription(rs.getString("description"));
        Timestamp ts = rs.getTimestamp("transaction_date");
        if (ts != null) t.setTransactionDate(ts.toLocalDateTime());
        return t;
    }
}
