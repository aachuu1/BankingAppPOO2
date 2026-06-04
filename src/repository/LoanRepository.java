package repository;

import db.DatabaseConnection;

import java.sql.*;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Repository singleton pentru entitatea Loan.
 */
public class LoanRepository implements Repository<LoanRepository.LoanDTO, String> {

    private static LoanRepository instance;
    private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private LoanRepository() {}

    public static LoanRepository getInstance() {
        if (instance == null) {
            instance = new LoanRepository();
        }
        return instance;
    }

    public static class LoanDTO {
        public String loanId;
        public String borrowerId;
        public double principalAmount;
        public double remainingBalance;
        public double interestRate;
        public int termMonths;
        public double monthlyPayment;
        public Date startDate;
        public Date endDate;
        public String status;
        public String loanType;

        public LoanDTO(String loanId, String borrowerId, double principalAmount,
                       double remainingBalance, double interestRate, int termMonths,
                       double monthlyPayment, Date startDate, Date endDate,
                       String status, String loanType) {
            this.loanId = loanId; this.borrowerId = borrowerId;
            this.principalAmount = principalAmount; this.remainingBalance = remainingBalance;
            this.interestRate = interestRate; this.termMonths = termMonths;
            this.monthlyPayment = monthlyPayment; this.startDate = startDate;
            this.endDate = endDate; this.status = status; this.loanType = loanType;
        }

        @Override
        public String toString() {
            return String.format("LoanDTO{id='%s', type=%s, principal=%.2f, remaining=%.2f, status=%s}",
                    loanId, loanType, principalAmount, remainingBalance, status);
        }
    }

    @Override
    public void save(LoanDTO loan) throws SQLException {
        String sql = """
            INSERT INTO loans (loan_id, borrower_id, principal_amount, remaining_balance,
                interest_rate, term_months, monthly_payment, start_date, end_date, status, loan_type)
            VALUES (?,?,?,?,?,?,?,?,?,?,?)
            """;
        try (PreparedStatement ps = DatabaseConnection.getInstance().getConnection().prepareStatement(sql)) {
            ps.setString(1, loan.loanId);
            ps.setString(2, loan.borrowerId);
            ps.setDouble(3, loan.principalAmount);
            ps.setDouble(4, loan.remainingBalance);
            ps.setDouble(5, loan.interestRate);
            ps.setInt(6, loan.termMonths);
            ps.setDouble(7, loan.monthlyPayment);
            ps.setString(8, loan.startDate != null ? SDF.format(loan.startDate) : null);
            ps.setString(9, loan.endDate != null ? SDF.format(loan.endDate) : null);
            ps.setString(10, loan.status);
            ps.setString(11, loan.loanType);
            ps.executeUpdate();
            System.out.println("[DB] Imprumut salvat: " + loan.loanId);
        }
    }

    @Override
    public Optional<LoanDTO> findById(String id) throws SQLException {
        String sql = "SELECT * FROM loans WHERE loan_id = ?";
        try (PreparedStatement ps = DatabaseConnection.getInstance().getConnection().prepareStatement(sql)) {
            ps.setString(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return Optional.of(mapRow(rs));
        }
        return Optional.empty();
    }

    public List<LoanDTO> findByBorrower(String borrowerId) throws SQLException {
        String sql = "SELECT * FROM loans WHERE borrower_id = ?";
        List<LoanDTO> result = new ArrayList<>();
        try (PreparedStatement ps = DatabaseConnection.getInstance().getConnection().prepareStatement(sql)) {
            ps.setString(1, borrowerId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) result.add(mapRow(rs));
        }
        return result;
    }

    public List<LoanDTO> findByStatus(String status) throws SQLException {
        String sql = "SELECT * FROM loans WHERE status = ?";
        List<LoanDTO> result = new ArrayList<>();
        try (PreparedStatement ps = DatabaseConnection.getInstance().getConnection().prepareStatement(sql)) {
            ps.setString(1, status);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) result.add(mapRow(rs));
        }
        return result;
    }

    @Override
    public List<LoanDTO> findAll() throws SQLException {
        String sql = "SELECT * FROM loans ORDER BY start_date DESC";
        List<LoanDTO> result = new ArrayList<>();
        try (Statement stmt = DatabaseConnection.getInstance().getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) result.add(mapRow(rs));
        }
        return result;
    }

    @Override
    public void update(LoanDTO loan) throws SQLException {
        String sql = "UPDATE loans SET remaining_balance=?, status=? WHERE loan_id=?";
        try (PreparedStatement ps = DatabaseConnection.getInstance().getConnection().prepareStatement(sql)) {
            ps.setDouble(1, loan.remainingBalance);
            ps.setString(2, loan.status);
            ps.setString(3, loan.loanId);
            int rows = ps.executeUpdate();
            System.out.println("[DB] Imprumut actualizat: " + loan.loanId + " (" + rows + " randuri)");
        }
    }

    @Override
    public void delete(String id) throws SQLException {
        String sql = "DELETE FROM loans WHERE loan_id = ?";
        try (PreparedStatement ps = DatabaseConnection.getInstance().getConnection().prepareStatement(sql)) {
            ps.setString(1, id);
            int rows = ps.executeUpdate();
            System.out.println("[DB] Imprumut sters: " + id + " (" + rows + " randuri)");
        }
    }

    private LoanDTO mapRow(ResultSet rs) throws SQLException {
        return new LoanDTO(
            rs.getString("loan_id"), rs.getString("borrower_id"),
            rs.getDouble("principal_amount"), rs.getDouble("remaining_balance"),
            rs.getDouble("interest_rate"), rs.getInt("term_months"),
            rs.getDouble("monthly_payment"),
            parseDate(rs.getString("start_date")), parseDate(rs.getString("end_date")),
            rs.getString("status"), rs.getString("loan_type")
        );
    }

    private Date parseDate(String s) {
        if (s == null) return null;
        try { return SDF.parse(s); } catch (Exception e) { return null; }
    }
}
