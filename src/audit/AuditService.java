package audit;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Serviciu de audit singleton.
 * Scrie intr-un fisier CSV fiecare actiune executata in aplicatie.
 * Format: nume_actiune,timestamp
 */
public class AuditService {

    private static AuditService instance;
    private static final String AUDIT_FILE = "audit_log.csv";
    private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    // Actiunile definite in prima etapa
    public enum Action {
        REGISTER_CUSTOMER,
        OPEN_ACCOUNT,
        DEPOSIT,
        WITHDRAWAL,
        TRANSFER,
        ISSUE_CARD,
        BLOCK_CARD,
        UNBLOCK_CARD,
        CARD_PAYMENT,
        GRANT_LOAN,
        LOAN_PAYMENT,
        GENERATE_STATEMENT,
        APPLY_INTEREST,
        FIND_ACCOUNT_BY_IBAN,
        FIND_CUSTOMER_BY_ID,
        PRINT_ACCOUNTS_SORTED,
        PRINT_CUSTOMER_DETAILS,
        // Operatii CRUD baza de date
        DB_SAVE_USER,
        DB_UPDATE_USER,
        DB_DELETE_USER,
        DB_SAVE_ACCOUNT,
        DB_UPDATE_ACCOUNT,
        DB_DELETE_ACCOUNT,
        DB_SAVE_CARD,
        DB_UPDATE_CARD,
        DB_DELETE_CARD,
        DB_SAVE_LOAN,
        DB_UPDATE_LOAN,
        DB_DELETE_LOAN,
        DB_SAVE_TRANSACTION,
        DB_UPDATE_TRANSACTION,
        DB_DELETE_TRANSACTION
    }

    private BufferedWriter writer;

    private AuditService() {
        try {
            // append=true: nu sterge logul la repornire
            FileWriter fw = new FileWriter(AUDIT_FILE, true);
            this.writer = new BufferedWriter(fw);

            // Scrie header daca fisierul e gol/nou
            File f = new File(AUDIT_FILE);
            if (f.length() == 0) {
                writer.write("nume_actiune,timestamp");
                writer.newLine();
                writer.flush();
            }
            System.out.println("[AUDIT] Serviciu de audit initializat. Fisier: " + AUDIT_FILE);
        } catch (IOException e) {
            System.err.println("[AUDIT] EROARE la initializarea fisierului de audit: " + e.getMessage());
        }
    }

    public static AuditService getInstance() {
        if (instance == null) {
            instance = new AuditService();
        }
        return instance;
    }

    /**
     * Inregistreaza o actiune cu timestamp-ul curent.
     */
    public void log(Action action) {
        log(action.name());
    }

    /**
     * Inregistreaza o actiune cu nume personalizat si timestamp curent.
     */
    public void log(String actionName) {
        try {
            String timestamp = SDF.format(new Date());
            writer.write(actionName + "," + timestamp);
            writer.newLine();
            writer.flush();
        } catch (IOException e) {
            System.err.println("[AUDIT] EROARE la scrierea in log: " + e.getMessage());
        }
    }

    /**
     * Inregistreaza actiunea cu detalii suplimentare (adaugate la numele actiunii).
     */
    public void log(Action action, String details) {
        log(action.name() + "[" + details + "]");
    }

    /**
     * Inchide fisierul de audit. Apelati la finalul aplicatiei.
     */
    public void close() {
        try {
            if (writer != null) {
                writer.close();
                System.out.println("[AUDIT] Fisier de audit inchis.");
            }
        } catch (IOException e) {
            System.err.println("[AUDIT] EROARE la inchiderea fisierului: " + e.getMessage());
        }
    }
}
