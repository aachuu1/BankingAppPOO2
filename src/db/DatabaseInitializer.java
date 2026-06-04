package db;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Initializeaza schema bazei de date la pornirea aplicatiei.
 * Se apeleaza o singura data, la startup.
 */
public class DatabaseInitializer {

    public static void initialize() throws SQLException {
        Connection conn = DatabaseConnection.getInstance().getConnection();
        try (Statement stmt = conn.createStatement()) {

            // Tabela Users (clienti)
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS users (
                    customer_id   TEXT PRIMARY KEY,
                    first_name    TEXT NOT NULL,
                    last_name     TEXT NOT NULL,
                    cnp           TEXT NOT NULL UNIQUE,
                    email         TEXT NOT NULL,
                    phone_number  TEXT,
                    date_of_birth TEXT,
                    status        TEXT NOT NULL DEFAULT 'ACTIVE'
                )
            """);

            // Tabela BankAccounts
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS bank_accounts (
                    account_id   TEXT PRIMARY KEY,
                    iban         TEXT NOT NULL UNIQUE,
                    currency     TEXT NOT NULL,
                    account_type TEXT NOT NULL,
                    status       TEXT NOT NULL DEFAULT 'ACTIVE',
                    balance      REAL NOT NULL DEFAULT 0.0,
                    owner_id     TEXT NOT NULL,
                    created_at   TEXT,
                    -- campuri specifice SavingsAccount
                    interest_rate     REAL,
                    lock_period_months INTEGER,
                    minimum_balance   REAL,
                    -- campuri specifice CurrentAccount
                    overdraft_limit   REAL,
                    overdraft_used    REAL,
                    monthly_fee       REAL,
                    FOREIGN KEY (owner_id) REFERENCES users(customer_id)
                )
            """);

            // Tabela Cards
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS cards (
                    card_id           TEXT PRIMARY KEY,
                    card_number       TEXT NOT NULL UNIQUE,
                    card_holder_name  TEXT NOT NULL,
                    cvv               TEXT NOT NULL,
                    expiry_date       TEXT,
                    issue_date        TEXT,
                    status            TEXT NOT NULL DEFAULT 'ACTIVE',
                    card_type         TEXT NOT NULL,
                    linked_account_id TEXT NOT NULL,
                    owner_id          TEXT NOT NULL,
                    -- credit card specific
                    credit_limit      REAL,
                    used_credit       REAL,
                    interest_rate     REAL,
                    payment_due_date  TEXT,
                    -- debit card specific
                    daily_withdrawal_limit REAL,
                    daily_payment_limit    REAL,
                    FOREIGN KEY (linked_account_id) REFERENCES bank_accounts(account_id),
                    FOREIGN KEY (owner_id) REFERENCES users(customer_id)
                )
            """);

            // Tabela Loans
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS loans (
                    loan_id          TEXT PRIMARY KEY,
                    borrower_id      TEXT NOT NULL,
                    principal_amount REAL NOT NULL,
                    remaining_balance REAL NOT NULL,
                    interest_rate    REAL NOT NULL,
                    term_months      INTEGER NOT NULL,
                    monthly_payment  REAL NOT NULL,
                    start_date       TEXT,
                    end_date         TEXT,
                    status           TEXT NOT NULL DEFAULT 'ACTIVE',
                    loan_type        TEXT NOT NULL,
                    FOREIGN KEY (borrower_id) REFERENCES users(customer_id)
                )
            """);

            // Tabela Transactions
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS transactions (
                    transaction_id     TEXT PRIMARY KEY,
                    amount             REAL NOT NULL,
                    currency           TEXT NOT NULL,
                    transaction_type   TEXT NOT NULL,
                    status             TEXT NOT NULL,
                    description        TEXT,
                    reference_number   TEXT,
                    timestamp          TEXT,
                    -- relatii optionale in functie de tip
                    source_account_id  TEXT,
                    target_account_id  TEXT,
                    card_id            TEXT,
                    merchant_name      TEXT,
                    deposit_source     TEXT,
                    location           TEXT
                )
            """);

            System.out.println("[DB] Schema bazei de date initializata cu succes.");
        }
    }
}
