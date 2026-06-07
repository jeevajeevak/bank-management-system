package bank.dao;

import bank.model.Loan;
import bank.model.Loan.LoanStatus;
import bank.model.Loan.LoanType;
import bank.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * LoanDAO – all database operations for the loans table.
 */
public class LoanDAO {

    // ── CREATE ────────────────────────────────────────────────────
    public boolean addLoan(Loan l) {
        String sql = "INSERT INTO loans (customer_id, loan_type, principal, interest_rate, " +
                     "duration_months, emi, amount_paid, status) VALUES (?,?,?,?,?,?,?,?)";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql,
                Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, l.getCustomerId());
            ps.setString(2, l.getLoanType().name());
            ps.setDouble(3, l.getPrincipal());
            ps.setDouble(4, l.getInterestRate());
            ps.setInt(5, l.getDurationMonths());
            ps.setDouble(6, l.getEmi());
            ps.setDouble(7, l.getAmountPaid());
            ps.setString(8, l.getStatus().name());
            ps.executeUpdate();

            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) l.setLoanId(keys.getInt(1));
            return true;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    // ── READ ALL ──────────────────────────────────────────────────
    public List<Loan> getAllLoans() {
        List<Loan> list = new ArrayList<>();
        String sql = "SELECT l.*, c.full_name FROM loans l " +
                     "JOIN customers c ON l.customer_id = c.customer_id ORDER BY l.loan_id";
        try (Statement st = DBConnection.getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    // ── READ by customer ──────────────────────────────────────────
    public List<Loan> getLoansByCustomer(int customerId) {
        List<Loan> list = new ArrayList<>();
        String sql = "SELECT l.*, c.full_name FROM loans l " +
                     "JOIN customers c ON l.customer_id = c.customer_id WHERE l.customer_id = ?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, customerId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    // ── UPDATE STATUS ─────────────────────────────────────────────
    public boolean updateStatus(int loanId, LoanStatus status) {
        String sql = "UPDATE loans SET status = ?, start_date = CASE WHEN ? = 'ACTIVE' THEN CURDATE() ELSE start_date END WHERE loan_id = ?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, status.name());
            ps.setString(2, status.name());
            ps.setInt(3, loanId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    // ── MAKE EMI PAYMENT ──────────────────────────────────────────
    public boolean makePayment(int loanId, double amount) {
        String sql = "UPDATE loans SET amount_paid = amount_paid + ? WHERE loan_id = ?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setDouble(1, amount);
            ps.setInt(2, loanId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    // ── READ ONE ──────────────────────────────────────────────────
    public Loan getLoanById(int id) {
        String sql = "SELECT l.*, c.full_name FROM loans l " +
                     "JOIN customers c ON l.customer_id = c.customer_id WHERE l.loan_id = ?";
        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    // ── Helper ────────────────────────────────────────────────────
    private Loan mapRow(ResultSet rs) throws SQLException {
        Loan l = new Loan();
        l.setLoanId(rs.getInt("loan_id"));
        l.setCustomerId(rs.getInt("customer_id"));
        l.setCustomerName(rs.getString("full_name"));
        l.setLoanType(LoanType.valueOf(rs.getString("loan_type")));
        l.setPrincipal(rs.getDouble("principal"));
        l.setInterestRate(rs.getDouble("interest_rate"));
        l.setDurationMonths(rs.getInt("duration_months"));
        l.setEmi(rs.getDouble("emi"));
        l.setAmountPaid(rs.getDouble("amount_paid"));
        l.setStatus(LoanStatus.valueOf(rs.getString("status")));
        java.sql.Date sd = rs.getDate("start_date");
        if (sd != null) l.setStartDate(sd.toLocalDate());
        return l;
    }
}
