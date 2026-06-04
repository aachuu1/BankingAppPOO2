import java.util.Date;

//clasa abstracta de baza pentru toate tipurile de carduri
public abstract class Card {

    public enum CardStatus {
        ACTIVE, BLOCKED, EXPIRED
    }

    public enum CardType {
        DEBIT, CREDIT
    }

    private String cardId;
    private String cardNumber;
    private String cardHolderName;
    private String CVV;
    private Date expiryDate;
    private Date issueDate;
    private CardStatus status;
    private CardType type;
    private BankAccount linkedAccount;

    //constructor - cardul e activ din momentul emiterii
    public Card(String cardId, String cardNumber, String cardHolderName,
                String CVV, Date expiryDate, CardType type, BankAccount linkedAccount) {
        this.cardId = cardId;
        this.cardNumber = cardNumber;
        this.cardHolderName = cardHolderName;
        this.CVV = CVV;
        this.expiryDate = expiryDate;
        this.issueDate = new Date();
        this.status = CardStatus.ACTIVE;
        this.type = type;
        this.linkedAccount = linkedAccount;
    }

    public abstract String getCardType();

    public void block() {
        this.status = CardStatus.BLOCKED;
        System.out.println("Cardul " + maskedCardNumber() + " a fost blocat.");
    }

    //nu permite deblocarea unui card expirat
    public void unblock() {
        if (isExpired()) throw new IllegalStateException("Cardul este expirat si nu poate fi deblocat.");
        this.status = CardStatus.ACTIVE;
        System.out.println("Cardul " + maskedCardNumber() + " a fost deblocat.");
    }

    public boolean isExpired() {
        return new Date().after(expiryDate);
    }

    //afiseaza doar ultimele 4 cifre
    public String maskedCardNumber() {
        return "**** **** **** " + cardNumber.substring(cardNumber.length() - 4);
    }

    public String getCardId() { return cardId; }
    public String getCardNumber() { return cardNumber; }
    public String getCardHolderName() { return cardHolderName; }
    public String getCVV() { return CVV; }
    public Date getExpiryDate() { return expiryDate; }
    public void setExpiryDate(Date expiryDate) { this.expiryDate = expiryDate; }
    public Date getIssueDate() { return issueDate; }
    public CardStatus getStatus() { return status; }
    public void setStatus(CardStatus status) { this.status = status; }
    public CardType getType() { return type; }
    public BankAccount getLinkedAccount() { return linkedAccount; }

    @Override
    public String toString() {
        return "Card{" +
                "Tip=" + getCardType() +
                ", Numar='" + maskedCardNumber() + '\'' +
                ", Titular='" + cardHolderName + '\'' +
                ", Status=" + status +
                ", Expira=" + expiryDate +
                '}';
    }
}