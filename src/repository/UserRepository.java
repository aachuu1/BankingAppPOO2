package repository;

import db.DatabaseConnection;

import java.sql.*;
import java.util.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

// Import domeniu - in compilare trebuie in acelasi classpath
// (clasele User, BankAccount etc. sunt in pachetul implicit)

/**
 * Repository singleton pentru entitatea User.
 * Expune operatii CRUD complete.
 */
public class UserRepository implements Repository<UserRepository.UserDTO, String> {

    private static UserRepository instance;
    private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd");

    private UserRepository() {}

    public static UserRepository getInstance() {
        if (instance == null) {
            instance = new UserRepository();
        }
        return instance;
    }

    // DTO simplu pentru a nu depinde de User (care are relatii complexe)
    public static class UserDTO {
        public String customerId;
        public String firstName;
        public String lastName;
        public String cnp;
        public String email;
        public String phoneNumber;
        public Date dateOfBirth;
        public String status;

        public UserDTO(String customerId, String firstName, String lastName, String cnp,
                       String email, String phoneNumber, Date dateOfBirth, String status) {
            this.customerId = customerId;
            this.firstName = firstName;
            this.lastName = lastName;
            this.cnp = cnp;
            this.email = email;
            this.phoneNumber = phoneNumber;
            this.dateOfBirth = dateOfBirth;
            this.status = status;
        }

        @Override
        public String toString() {
            return "UserDTO{id='" + customerId + "', name='" + firstName + " " + lastName
                    + "', cnp='" + cnp + "', status='" + status + "'}";
        }
    }

    @Override
    public void save(UserDTO user) throws SQLException {
        String sql = """
            INSERT INTO users (customer_id, first_name, last_name, cnp, email, phone_number, date_of_birth, status)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """;
        try (PreparedStatement ps = DatabaseConnection.getInstance().getConnection().prepareStatement(sql)) {
            ps.setString(1, user.customerId);
            ps.setString(2, user.firstName);
            ps.setString(3, user.lastName);
            ps.setString(4, user.cnp);
            ps.setString(5, user.email);
            ps.setString(6, user.phoneNumber);
            ps.setString(7, user.dateOfBirth != null ? SDF.format(user.dateOfBirth) : null);
            ps.setString(8, user.status);
            ps.executeUpdate();
            System.out.println("[DB] User salvat: " + user.customerId);
        }
    }

    @Override
    public Optional<UserDTO> findById(String id) throws SQLException {
        String sql = "SELECT * FROM users WHERE customer_id = ?";
        try (PreparedStatement ps = DatabaseConnection.getInstance().getConnection().prepareStatement(sql)) {
            ps.setString(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return Optional.of(mapRow(rs));
            }
        }
        return Optional.empty();
    }

    @Override
    public List<UserDTO> findAll() throws SQLException {
        String sql = "SELECT * FROM users ORDER BY last_name, first_name";
        List<UserDTO> result = new ArrayList<>();
        try (Statement stmt = DatabaseConnection.getInstance().getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                result.add(mapRow(rs));
            }
        }
        return result;
    }

    @Override
    public void update(UserDTO user) throws SQLException {
        String sql = """
            UPDATE users SET first_name=?, last_name=?, email=?, phone_number=?, status=?
            WHERE customer_id=?
            """;
        try (PreparedStatement ps = DatabaseConnection.getInstance().getConnection().prepareStatement(sql)) {
            ps.setString(1, user.firstName);
            ps.setString(2, user.lastName);
            ps.setString(3, user.email);
            ps.setString(4, user.phoneNumber);
            ps.setString(5, user.status);
            ps.setString(6, user.customerId);
            int rows = ps.executeUpdate();
            System.out.println("[DB] User actualizat: " + user.customerId + " (" + rows + " randuri)");
        }
    }

    @Override
    public void delete(String id) throws SQLException {
        String sql = "DELETE FROM users WHERE customer_id = ?";
        try (PreparedStatement ps = DatabaseConnection.getInstance().getConnection().prepareStatement(sql)) {
            ps.setString(1, id);
            int rows = ps.executeUpdate();
            System.out.println("[DB] User sters: " + id + " (" + rows + " randuri)");
        }
    }

    public Optional<UserDTO> findByCNP(String cnp) throws SQLException {
        String sql = "SELECT * FROM users WHERE cnp = ?";
        try (PreparedStatement ps = DatabaseConnection.getInstance().getConnection().prepareStatement(sql)) {
            ps.setString(1, cnp);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return Optional.of(mapRow(rs));
        }
        return Optional.empty();
    }

    private UserDTO mapRow(ResultSet rs) throws SQLException {
        Date dob = null;
        String dobStr = rs.getString("date_of_birth");
        if (dobStr != null) {
            try { dob = SDF.parse(dobStr); } catch (ParseException ignored) {}
        }
        return new UserDTO(
            rs.getString("customer_id"),
            rs.getString("first_name"),
            rs.getString("last_name"),
            rs.getString("cnp"),
            rs.getString("email"),
            rs.getString("phone_number"),
            dob,
            rs.getString("status")
        );
    }
}
