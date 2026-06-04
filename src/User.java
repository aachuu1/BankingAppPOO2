import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class User {

    public enum CustomerStatus {
        ACTIVE, SUSPENDED, CLOSED
    }

    private String customerId;
    private String firstName;
    private String lastName;
    private String CNP;
    private String email;
    private String phoneNumber;
    private Date dateOfBirth;
    private CustomerStatus status;
    //listele sunt initializate goale si populate pe parcurs
    private List<BankAccount> accounts;
    private List<Card> cards;
    private List<Loan> loans;

    //constructor - userul e activ din momentul inregistrarii
    public User(String customerId, String firstName, String lastName, String CNP,
                String email, String phoneNumber, Date dateOfBirth) {
        this.customerId = customerId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.CNP = CNP;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.dateOfBirth = dateOfBirth;
        this.status = CustomerStatus.ACTIVE;
        this.accounts = new ArrayList<>();
        this.cards = new ArrayList<>();
        this.loans = new ArrayList<>();
    }

    public String getCustomerId() { return customerId; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getFullName() { return firstName + " " + lastName; }
    public String getCNP() { return CNP; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public Date getDateOfBirth() { return dateOfBirth; }
    public CustomerStatus getStatus() { return status; }
    public void setStatus(CustomerStatus status) { this.status = status; }
    public List<BankAccount> getAccounts() { return accounts; }
    public void addAccount(BankAccount account) { this.accounts.add(account); }
    public void removeAccount(BankAccount account) { this.accounts.remove(account); }
    public List<Card> getCards() { return cards; }
    public void addCard(Card card) { this.cards.add(card); }
    public List<Loan> getLoans() { return loans; }
    public void addLoan(Loan loan) { this.loans.add(loan); }

    @Override
    public String toString() {
        return "User{" +
                "ID='" + customerId + '\'' +
                ", Nume='" + getFullName() + '\'' +
                ", CNP='" + CNP + '\'' +
                ", Email='" + email + '\'' +
                ", Status=" + status +
                ", Conturi=" + accounts.size() +
                '}';
    }
}