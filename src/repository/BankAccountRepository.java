package repository;

import db.DatabaseConnection;

import java.sql.*;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Repository singleton pentru entitatea BankAccount (curent si economii).
 */
public class BankAccountRepository implements Repository<BankAccountRepository.BankAccountDTO, String> {

    private static BankAccountRepository instance;
    private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private BankAccountRepository() {}

    public static BankAccountRepository getInstance() {
        if (instance == null) {
            instance = new BankAccountRepository();
        }
        return instance;
    }

    public static class BankAccountDTO {
        public String accountId;
        public String iban;
        public String currency;
        public String accountType;   // "CURRENT" sau "SAVINGS"
        public String status;
        public double balance;
        public String ownerId;
        public Date createdAt;
        // Savings specific
        public Double interestRate;
        public Integer lockPeriodMonths;
        public Double minimumBalance;
        // Current specific
        public Double overdraftLimit;
        public Double overdraftUsed;
        public Double monthlyFee;

        public BankAccountDTO(String accountId, String iban, String currency, String accountType,
                              String status, double balance, String ownerId, Date createdAt) {
            this.accountId = accountId;
            this.iban = iban;
            this.currency = currency;
            this.accountType = accountType;
            this.status = status;
            this.balance = balance;
            this.ownerId = ownerId;
            this.createdAt = createdAt;
        }

        @Override
        public String toString() {
            return "BankAccountDTO{iban='" + iban + "', type='" + accountType
                    + "', balance=" + balance + " " + currency + ", status='" + status + "'}";
        }
    }

    @Override
    public void save(BankAccountDTO acc) throws SQLException {
        String sql = """
            INSERT INTO bank_accounts (account_id, iban, currency, account_type, status, balance,
                owner_id, created_at, interest_rate, lock_period_months, minimum_balance,
                overdraft_limit, overdraft_used, monthly_fee)
            VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)
            """;
        try (PreparedStatement ps = DatabaseConnection.getInstance().getConnection().prepareStatement(sql)) {
            ps.setString(1, acc.accountId);
            ps.setString(2, acc.iban);
            ps.setString(3, acc.currency);
            ps.setString(4, acc.accountType);
            ps.setString(5, acc.status);
            ps.setDouble(6, acc.balance);
            ps.setString(7, acc.ownerId);
            ps.setString(8, acc.createdAt != null ? SDF.format(acc.createdAt) : null);
            setNullableDouble(ps, 9, acc.interestRate);
            setNullableInt(ps, 10, acc.lockPeriodMonths);
            setNullableDouble(ps, 11, acc.minimumBalance);
            setNullableDouble(ps, 12, acc.overdraftLimit);
            setNullableDouble(ps, 13, acc.overdraftUsed);
            setNullableDouble(ps, 14, acc.monthlyFee);
            ps.executeUpdate();
            System.out.println("[DB] Cont salvat: " + acc.iban);
        }
    }

    @Override
    public Optional<BankAccountDTO> findById(String id) throws SQLException {
        String sql = "SELECT * FROM bank_accounts WHERE account_id = ?";
        try (PreparedStatement ps = DatabaseConnection.getInstance().getConnection().prepareStatement(sql)) {
            ps.setString(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return Optional.of(mapRow(rs));
        }
        return Optional.empty();
    }

    public Optional<BankAccountDTO> findByIBAN(String iban) throws SQLException {
        String sql = "SELECT * FROM bank_accounts WHERE iban = ?";
        try (PreparedStatement ps = DatabaseConnection.getInstance().getConnection().prepareStatement(sql)) {
            ps.setString(1, iban);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return Optional.of(mapRow(rs));
        }
        return Optional.empty();
    }

    public List<BankAccountDTO> findByOwner(String ownerId) throws SQLException {
        String sql = "SELECT * FROM bank_accounts WHERE owner_id = ? ORDER BY iban";
        List<BankAccountDTO> result = new ArrayList<>();
        try (PreparedStatement ps = DatabaseConnection.getInstance().getConnection().prepareStatement(sql)) {
            ps.setString(1, ownerId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) result.add(mapRow(rs));
        }
        return result;
    }

    @Override
    public List<BankAccountDTO> findAll() throws SQLException {
        String sql = "SELECT * FROM bank_accounts ORDER BY iban";
        List<BankAccountDTO> result = new ArrayList<>();
        try (Statement stmt = DatabaseConnection.getInstance().getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) result.add(mapRow(rs));
        }
        return result;
    }

    @Override
    public void update(BankAccountDTO acc) throws SQLException {
        String sql = """
            UPDATE bank_accounts
            SET balance=?, status=?, overdraft_used=?, interest_rate=?, minimum_balance=?
            WHERE account_id=?
            """;
        try (PreparedStatement ps = DatabaseConnection.getInstance().getConnection().prepareStatement(sql)) {
            ps.setDouble(1, acc.balance);
            ps.setString(2, acc.status);
            setNullableDouble(ps, 3, acc.overdraftUsed);
            setNullableDouble(ps, 4, acc.interestRate);
            setNullableDouble(ps, 5, acc.minimumBalance);
            ps.setString(6, acc.accountId);
            int rows = ps.executeUpdate();
            System.out.println("[DB] Cont actualizat: " + acc.accountId + " (" + rows + " randuri)");
        }
    }

    /** Actualizeaza doar soldul unui cont dupa IBAN - util dupa fiecare tranzactie. */
    public void updateBalance(String iban, double newBalance) throws SQLException {
        String sql = "UPDATE bank_accounts SET balance=? WHERE iban=?";
        try (PreparedStatement ps = DatabaseConnection.getInstance().getConnection().prepareStatement(sql)) {
            ps.setDouble(1, newBalance);
            ps.setString(2, iban);
            ps.executeUpdate();
        }
    }

    @Override
    public void delete(String id) throws SQLException {
        String sql = "DELETE FROM bank_accounts WHERE account_id = ?";
        try (PreparedStatement ps = DatabaseConnection.getInstance().getConnection().prepareStatement(sql)) {
            ps.setString(1, id);
            int rows = ps.executeUpdate();
            System.out.println("[DB] Cont sters: " + id + " (" + rows + " randuri)");
        }
    }

    private BankAccountDTO mapRow(ResultSet rs) throws SQLException {
        Date createdAt = null;
        String caStr = rs.getString("created_at");
        if (caStr != null) {
            try { createdAt = SDF.parse(caStr); } catch (Exception ignored) {}
        }
        BankAccountDTO dto = new BankAccountDTO(
            rs.getString("account_id"),
            rs.getString("iban"),
            rs.getString("currency"),
            rs.getString("account_type"),
            rs.getString("status"),
            rs.getDouble("balance"),
            rs.getString("owner_id"),
            createdAt
        );
        double ir = rs.getDouble("interest_rate"); dto.interestRate = rs.wasNull() ? null : ir;
        int lp = rs.getInt("lock_period_months"); dto.lockPeriodMonths = rs.wasNull() ? null : lp;
        double mb = rs.getDouble("minimum_balance"); dto.minimumBalance = rs.wasNull() ? null : mb;
        double ol = rs.getDouble("overdraft_limit"); dto.overdraftLimit = rs.wasNull() ? null : ol;
        double ou = rs.getDouble("overdraft_used"); dto.overdraftUsed = rs.wasNull() ? null : ou;
        double mf = rs.getDouble("monthly_fee"); dto.monthlyFee = rs.wasNull() ? null : mf;
        return dto;
    }

    private void setNullableDouble(PreparedStatement ps, int idx, Double val) throws SQLException {
        if (val == null) ps.setNull(idx, Types.REAL);
        else ps.setDouble(idx, val);
    }

    private void setNullableInt(PreparedStatement ps, int idx, Integer val) throws SQLException {
        if (val == null) ps.setNull(idx, Types.INTEGER);
        else ps.setInt(idx, val);
    }
}
