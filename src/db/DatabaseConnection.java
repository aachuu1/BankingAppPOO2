package db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Singleton pentru conexiunea la baza de date SQLite.
 * Folosim SQLite deoarece nu necesita server extern si e ideal pentru demo/proiect.
 */
public class DatabaseConnection {

    private static DatabaseConnection instance;
    private Connection connection;

    // Fisierul bazei de date va fi creat local
    private static final String DB_URL = "jdbc:sqlite:banking.db";

    private DatabaseConnection() throws SQLException {
        try {
            Class.forName("org.sqlite.JDBC");
            this.connection = DriverManager.getConnection(DB_URL);
            this.connection.setAutoCommit(true);
            System.out.println("[DB] Conexiune la baza de date stabilita.");
        } catch (ClassNotFoundException e) {
            throw new SQLException("Driver SQLite nu a fost gasit: " + e.getMessage());
        }
    }

    public static DatabaseConnection getInstance() throws SQLException {
        if (instance == null || instance.connection.isClosed()) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    public Connection getConnection() {
        return connection;
    }

    public void closeConnection() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
            System.out.println("[DB] Conexiune inchisa.");
        }
    }
}
