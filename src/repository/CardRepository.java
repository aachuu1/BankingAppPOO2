package repository;

import db.DatabaseConnection;

import java.sql.*;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Repository singleton pentru entitatea Card (debit si credit).
 */
public class CardRepository implements Repository<CardRepository.CardDTO, String> {

    private static CardRepository instance;
    private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private CardRepository() {}

    public static CardRepository getInstance() {
        if (instance == null) {
            instance = new CardRepository();
        }
        return instance;
    }

    public static class CardDTO {
        public String cardId;
        public String cardNumber;
        public String cardHolderName;
        public String cvv;
        public Date expiryDate;
        public Date issueDate;
        public String status;
        public String cardType;      // "DEBIT" sau "CREDIT"
        public String linkedAccountId;
        public String ownerId;
        // Credit specific
        public Double creditLimit;
        public Double usedCredit;
        public Double interestRate;
        public Date paymentDueDate;
        // Debit specific
        public Double dailyWithdrawalLimit;
        public Double dailyPaymentLimit;

        public CardDTO(String cardId, String cardNumber, String cardHolderName,
                       String cvv, Date expiryDate, Date issueDate, String status,
                       String cardType, String linkedAccountId, String ownerId) {
            this.cardId = cardId; this.cardNumber = cardNumber;
            this.cardHolderName = cardHolderName; this.cvv = cvv;
            this.expiryDate = expiryDate; this.issueDate = issueDate;
            this.status = status; this.cardType = cardType;
            this.linkedAccountId = linkedAccountId; this.ownerId = ownerId;
        }

        public String maskedNumber() {
            if (cardNumber == null || cardNumber.length() < 4) return "****";
            return "**** **** **** " + cardNumber.substring(cardNumber.length() - 4);
        }

        @Override
        public String toString() {
            return "CardDTO{" + maskedNumber() + ", type=" + cardType + ", status=" + status + "}";
        }
    }

    @Override
    public void save(CardDTO card) throws SQLException {
        String sql = """
            INSERT INTO cards (card_id, card_number, card_holder_name, cvv, expiry_date, issue_date,
                status, card_type, linked_account_id, owner_id,
                credit_limit, used_credit, interest_rate, payment_due_date,
                daily_withdrawal_limit, daily_payment_limit)
            VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)
            """;
        try (PreparedStatement ps = DatabaseConnection.getInstance().getConnection().prepareStatement(sql)) {
            ps.setString(1, card.cardId);
            ps.setString(2, card.cardNumber);
            ps.setString(3, card.cardHolderName);
            ps.setString(4, card.cvv);
            ps.setString(5, card.expiryDate != null ? SDF.format(card.expiryDate) : null);
            ps.setString(6, card.issueDate != null ? SDF.format(card.issueDate) : null);
            ps.setString(7, card.status);
            ps.setString(8, card.cardType);
            ps.setString(9, card.linkedAccountId);
            ps.setString(10, card.ownerId);
            setND(ps, 11, card.creditLimit);
            setND(ps, 12, card.usedCredit);
            setND(ps, 13, card.interestRate);
            ps.setString(14, card.paymentDueDate != null ? SDF.format(card.paymentDueDate) : null);
            setND(ps, 15, card.dailyWithdrawalLimit);
            setND(ps, 16, card.dailyPaymentLimit);
            ps.executeUpdate();
            System.out.println("[DB] Card salvat: " + card.maskedNumber());
        }
    }

    @Override
    public Optional<CardDTO> findById(String id) throws SQLException {
        String sql = "SELECT * FROM cards WHERE card_id = ?";
        try (PreparedStatement ps = DatabaseConnection.getInstance().getConnection().prepareStatement(sql)) {
            ps.setString(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return Optional.of(mapRow(rs));
        }
        return Optional.empty();
    }

    public List<CardDTO> findByOwner(String ownerId) throws SQLException {
        String sql = "SELECT * FROM cards WHERE owner_id = ?";
        List<CardDTO> result = new ArrayList<>();
        try (PreparedStatement ps = DatabaseConnection.getInstance().getConnection().prepareStatement(sql)) {
            ps.setString(1, ownerId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) result.add(mapRow(rs));
        }
        return result;
    }

    @Override
    public List<CardDTO> findAll() throws SQLException {
        String sql = "SELECT * FROM cards";
        List<CardDTO> result = new ArrayList<>();
        try (Statement stmt = DatabaseConnection.getInstance().getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) result.add(mapRow(rs));
        }
        return result;
    }

    @Override
    public void update(CardDTO card) throws SQLException {
        String sql = "UPDATE cards SET status=?, used_credit=? WHERE card_id=?";
        try (PreparedStatement ps = DatabaseConnection.getInstance().getConnection().prepareStatement(sql)) {
            ps.setString(1, card.status);
            setND(ps, 2, card.usedCredit);
            ps.setString(3, card.cardId);
            int rows = ps.executeUpdate();
            System.out.println("[DB] Card actualizat: " + card.cardId + " (" + rows + " randuri)");
        }
    }

    /** Actualizeaza statusul cardului (ex: BLOCKED). */
    public void updateStatus(String cardId, String newStatus) throws SQLException {
        String sql = "UPDATE cards SET status=? WHERE card_id=?";
        try (PreparedStatement ps = DatabaseConnection.getInstance().getConnection().prepareStatement(sql)) {
            ps.setString(1, newStatus);
            ps.setString(2, cardId);
            ps.executeUpdate();
        }
    }

    @Override
    public void delete(String id) throws SQLException {
        String sql = "DELETE FROM cards WHERE card_id = ?";
        try (PreparedStatement ps = DatabaseConnection.getInstance().getConnection().prepareStatement(sql)) {
            ps.setString(1, id);
            int rows = ps.executeUpdate();
            System.out.println("[DB] Card sters: " + id + " (" + rows + " randuri)");
        }
    }

    private CardDTO mapRow(ResultSet rs) throws SQLException {
        Date expiry = parseDate(rs.getString("expiry_date"));
        Date issue  = parseDate(rs.getString("issue_date"));
        CardDTO dto = new CardDTO(
            rs.getString("card_id"), rs.getString("card_number"),
            rs.getString("card_holder_name"), rs.getString("cvv"),
            expiry, issue, rs.getString("status"), rs.getString("card_type"),
            rs.getString("linked_account_id"), rs.getString("owner_id")
        );
        double cl = rs.getDouble("credit_limit"); dto.creditLimit = rs.wasNull() ? null : cl;
        double uc = rs.getDouble("used_credit"); dto.usedCredit = rs.wasNull() ? null : uc;
        double ir = rs.getDouble("interest_rate"); dto.interestRate = rs.wasNull() ? null : ir;
        dto.paymentDueDate = parseDate(rs.getString("payment_due_date"));
        double dwl = rs.getDouble("daily_withdrawal_limit"); dto.dailyWithdrawalLimit = rs.wasNull() ? null : dwl;
        double dpl = rs.getDouble("daily_payment_limit"); dto.dailyPaymentLimit = rs.wasNull() ? null : dpl;
        return dto;
    }

    private void setND(PreparedStatement ps, int idx, Double val) throws SQLException {
        if (val == null) ps.setNull(idx, Types.REAL);
        else ps.setDouble(idx, val);
    }

    private Date parseDate(String s) {
        if (s == null) return null;
        try { return SDF.parse(s); } catch (Exception e) { return null; }
    }
}
