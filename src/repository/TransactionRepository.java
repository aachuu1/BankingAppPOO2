package repository;

import db.DatabaseConnection;

import java.sql.*;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Repository singleton pentru entitatea Transaction.
 */
public class TransactionRepository implements Repository<TransactionRepository.TransactionDTO, String> {

    private static TransactionRepository instance;
    private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private TransactionRepository() {}

    public static TransactionRepository getInstance() {
        if (instance == null) {
            instance = new TransactionRepository();
        }
        return instance;
    }

    public static class TransactionDTO {
        public String transactionId;
        public double amount;
        public String currency;
        public String transactionType;
        public String status;
        public String description;
        public String referenceNumber;
        public Date timestamp;
        // optional relatii
        public String sourceAccountId;
        public String targetAccountId;
        public String cardId;
        public String merchantName;
        public String depositSource;
        public String location;

        public TransactionDTO(String transactionId, double amount, String currency,
                              String transactionType, String status, String description,
                              String referenceNumber, Date timestamp) {
            this.transactionId = transactionId; this.amount = amount;
            this.currency = currency; this.transactionType = transactionType;
            this.status = status; this.description = description;
            this.referenceNumber = referenceNumber; this.timestamp = timestamp;
        }

        @Override
        public String toString() {
            return String.format("TransactionDTO{id='%s', type=%s, amount=%.2f %s, status=%s}",
                    transactionId, transactionType, amount, currency, status);
        }
    }

    @Override
    public void save(TransactionDTO tx) throws SQLException {
        String sql = """
            INSERT INTO transactions (transaction_id, amount, currency, transaction_type, status,
                description, reference_number, timestamp, source_account_id, target_account_id,
                card_id, merchant_name, deposit_source, location)
            VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)
            """;
        try (PreparedStatement ps = DatabaseConnection.getInstance().getConnection().prepareStatement(sql)) {
            ps.setString(1, tx.transactionId);
            ps.setDouble(2, tx.amount);
            ps.setString(3, tx.currency);
            ps.setString(4, tx.transactionType);
            ps.setString(5, tx.status);
            ps.setString(6, tx.description);
            ps.setString(7, tx.referenceNumber);
            ps.setString(8, tx.timestamp != null ? SDF.format(tx.timestamp) : SDF.format(new Date()));
            setNS(ps, 9,  tx.sourceAccountId);
            setNS(ps, 10, tx.targetAccountId);
            setNS(ps, 11, tx.cardId);
            setNS(ps, 12, tx.merchantName);
            setNS(ps, 13, tx.depositSource);
            setNS(ps, 14, tx.location);
            ps.executeUpdate();
            System.out.println("[DB] Tranzactie salvata: " + tx.transactionId);
        }
    }

    @Override
    public Optional<TransactionDTO> findById(String id) throws SQLException {
        String sql = "SELECT * FROM transactions WHERE transaction_id = ?";
        try (PreparedStatement ps = DatabaseConnection.getInstance().getConnection().prepareStatement(sql)) {
            ps.setString(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return Optional.of(mapRow(rs));
        }
        return Optional.empty();
    }

    public List<TransactionDTO> findByAccount(String accountId) throws SQLException {
        String sql = """
            SELECT * FROM transactions
            WHERE source_account_id = ? OR target_account_id = ?
            ORDER BY timestamp DESC
            """;
        List<TransactionDTO> result = new ArrayList<>();
        try (PreparedStatement ps = DatabaseConnection.getInstance().getConnection().prepareStatement(sql)) {
            ps.setString(1, accountId);
            ps.setString(2, accountId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) result.add(mapRow(rs));
        }
        return result;
    }

    public List<TransactionDTO> findByDateRange(Date start, Date end) throws SQLException {
        String sql = "SELECT * FROM transactions WHERE timestamp BETWEEN ? AND ? ORDER BY timestamp DESC";
        List<TransactionDTO> result = new ArrayList<>();
        try (PreparedStatement ps = DatabaseConnection.getInstance().getConnection().prepareStatement(sql)) {
            ps.setString(1, SDF.format(start));
            ps.setString(2, SDF.format(end));
            ResultSet rs = ps.executeQuery();
            while (rs.next()) result.add(mapRow(rs));
        }
        return result;
    }

    @Override
    public List<TransactionDTO> findAll() throws SQLException {
        String sql = "SELECT * FROM transactions ORDER BY timestamp DESC";
        List<TransactionDTO> result = new ArrayList<>();
        try (Statement stmt = DatabaseConnection.getInstance().getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) result.add(mapRow(rs));
        }
        return result;
    }

    @Override
    public void update(TransactionDTO tx) throws SQLException {
        String sql = "UPDATE transactions SET status=? WHERE transaction_id=?";
        try (PreparedStatement ps = DatabaseConnection.getInstance().getConnection().prepareStatement(sql)) {
            ps.setString(1, tx.status);
            ps.setString(2, tx.transactionId);
            ps.executeUpdate();
            System.out.println("[DB] Tranzactie actualizata: " + tx.transactionId);
        }
    }

    @Override
    public void delete(String id) throws SQLException {
        String sql = "DELETE FROM transactions WHERE transaction_id = ?";
        try (PreparedStatement ps = DatabaseConnection.getInstance().getConnection().prepareStatement(sql)) {
            ps.setString(1, id);
            int rows = ps.executeUpdate();
            System.out.println("[DB] Tranzactie stearsa: " + id + " (" + rows + " randuri)");
        }
    }

    private TransactionDTO mapRow(ResultSet rs) throws SQLException {
        TransactionDTO dto = new TransactionDTO(
            rs.getString("transaction_id"), rs.getDouble("amount"),
            rs.getString("currency"), rs.getString("transaction_type"),
            rs.getString("status"), rs.getString("description"),
            rs.getString("reference_number"), parseDate(rs.getString("timestamp"))
        );
        dto.sourceAccountId = rs.getString("source_account_id");
        dto.targetAccountId = rs.getString("target_account_id");
        dto.cardId = rs.getString("card_id");
        dto.merchantName = rs.getString("merchant_name");
        dto.depositSource = rs.getString("deposit_source");
        dto.location = rs.getString("location");
        return dto;
    }

    private void setNS(PreparedStatement ps, int idx, String val) throws SQLException {
        if (val == null) ps.setNull(idx, Types.VARCHAR);
        else ps.setString(idx, val);
    }

    private Date parseDate(String s) {
        if (s == null) return null;
        try { return SDF.parse(s); } catch (Exception e) { return null; }
    }
}
