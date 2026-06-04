import java.util.Date;

//clasa abstracta de baza pentru toate tipurile de tranzactii
public abstract class Transaction {

    public enum TransactionStatus {
        PENDING, COMPLETED, FAILED, REVERSED
    }

    public enum TransactionType {
        TRANSFER, DEPOSIT, WITHDRAWAL, CARD_PAYMENT
    }

    private String transactionId;
    private double amount;
    private String currency;
    private Date timestamp;
    private TransactionStatus status;
    private TransactionType type;
    private String description;
    private String referenceNumber;

    //constructor - orice tranzactie porneste cu status PENDING
    public Transaction(String transactionId, double amount, String currency,
                       TransactionType type, String description) {
        this.transactionId = transactionId;
        this.amount = amount;
        this.currency = currency;
        this.timestamp = new Date();
        this.status = TransactionStatus.PENDING;
        this.type = type;
        this.description = description;
        this.referenceNumber = "REF-" + transactionId;
    }

    //implementate diferit in functie de tipul de tranzactie
    public abstract void execute();
    public abstract void rollback();

    public String getTransactionId() { return transactionId; }
    public double getAmount() { return amount; }
    public String getCurrency() { return currency; }
    public Date getTimestamp() { return timestamp; }
    public TransactionStatus getStatus() { return status; }
    public void setStatus(TransactionStatus status) { this.status = status; }
    public TransactionType getType() { return type; }
    public String getDescription() { return description; }
    public String getReferenceNumber() { return referenceNumber; }

    @Override
    public String toString() {
        return "Transaction{" +
                "ID='" + transactionId + '\'' +
                ", Tip=" + type +
                ", Suma=" + amount + " " + currency +
                ", Status=" + status +
                ", Data=" + timestamp +
                ", Descriere='" + description + '\'' +
                '}';
    }
}