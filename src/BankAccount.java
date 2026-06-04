import java.util.ArrayList;
import java.util.Date;
import java.util.List;

//clasa abstracta de baza pentru toate tipurile de conturi bancare
public abstract class BankAccount {

    //starile posibile ale unui cont
    public enum AccountStatus {
        ACTIVE, FROZEN, CLOSED
    }

    //tipurile de conturi disponibile
    public enum AccountType {
        CURRENT, SAVINGS, FIXED_DEPOSIT
    }

    private String accountId;
    private String IBAN;
    protected double balance;
    private String currency;
    private Date createdAt;
    private Date closedAt;
    private AccountStatus status;
    private AccountType type;
    private User owner;
    private List<Transaction> transactions;
    private List<Card> cards;

    //constructor - initializeaza contul cu sold 0, status activ si listele goale
    public BankAccount(String accountId, String IBAN, String currency, AccountType type, User owner) {
        this.accountId = accountId;
        this.IBAN = IBAN;
        this.balance = 0.0;
        this.currency = currency;
        this.createdAt = new Date();
        this.status = AccountStatus.ACTIVE;
        this.type = type;
        this.owner = owner;
        this.transactions = new ArrayList<>();
        this.cards = new ArrayList<>();
    }

    //implementate diferit in functie de tipul de cont
    public abstract String getAccountType();
    public abstract double calculateInterest();

    //valideaza suma si statusul contului inainte de depunere
    public void deposit(double amount) {
        if (amount <= 0) throw new IllegalArgumentException("Suma depusa trebuie sa fie pozitiva.");
        if (status != AccountStatus.ACTIVE) throw new IllegalStateException("Contul nu este activ.");
        this.balance += amount;
    }

    //valideaza suma, statusul si soldul disponibil inainte de retragere
    public void withdraw(double amount) {
        if (amount <= 0) throw new IllegalArgumentException("Suma retrasa trebuie sa fie pozitiva.");
        if (status != AccountStatus.ACTIVE) throw new IllegalStateException("Contul nu este activ.");
        if (amount > balance) throw new IllegalStateException("Fonduri insuficiente.");
        this.balance -= amount;
    }
    //getteri si setteri
    public String getAccountId() { return accountId; }

    public String getIBAN() { return IBAN; }

    public double getBalance() { return balance; }

    public String getCurrency() { return currency; }

    public Date getCreatedAt() { return createdAt; }

    public Date getClosedAt() { return closedAt; }
    public void setClosedAt(Date closedAt) { this.closedAt = closedAt; }

    public AccountStatus getStatus() { return status; }
    public void setStatus(AccountStatus status) { this.status = status; }

    public AccountType getType() { return type; }

    public User getOwner() { return owner; }

    public List<Transaction> getTransactions() { return transactions; }
    public void addTransaction(Transaction transaction) { this.transactions.add(transaction); }

    public List<Card> getCards() { return cards; }
    public void addCard(Card card) { this.cards.add(card); }

    @Override
    public String toString() {
        return "BankAccount{" +
                "IBAN='" + IBAN + '\'' +
                ", Tip=" + getAccountType() +
                ", Sold=" + balance + " " + currency +
                ", Status=" + status +
                ", Proprietar='" + owner.getFullName() + '\'' +
                '}';
    }
}