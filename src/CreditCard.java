import java.util.Date;

public class CreditCard extends Card {

    private double creditLimit;
    private double usedCredit;
    private double minimumPayment;
    private Date paymentDueDate;
    private double interestRate;

    //constructor
    public CreditCard(String cardId, String cardNumber, String cardHolderName,
                      String CVV, Date expiryDate, BankAccount linkedAccount,
                      double creditLimit, double interestRate, Date paymentDueDate) {
        super(cardId, cardNumber, cardHolderName, CVV, expiryDate, CardType.CREDIT, linkedAccount);
        this.creditLimit = creditLimit;
        this.usedCredit = 0.0;
        this.interestRate = interestRate;
        this.paymentDueDate = paymentDueDate;
        this.minimumPayment = 0.0;
    }

    @Override
    public String getCardType() {
        return "Card de Credit";
    }

    public double getAvailableCredit() {
        return creditLimit - usedCredit;
    }

    //debiteaza creditul si recalculeaza plata minima
    public void charge(double amount) {
        if (amount > getAvailableCredit())
            throw new IllegalStateException("Credit insuficient disponibil.");
        usedCredit += amount;
        minimumPayment = usedCredit * 0.05;
    }

    //reduce creditul folosit si recalculeaza plata minima
    public void makePayment(double amount) {
        if (amount <= 0) throw new IllegalArgumentException("Suma platii trebuie sa fie pozitiva.");
        usedCredit = Math.max(0, usedCredit - amount);
        minimumPayment = usedCredit * 0.05;
        System.out.println("Plata de " + amount + " procesata. Credit ramas: " + usedCredit);
    }

    public double getCreditLimit() { return creditLimit; }
    public void setCreditLimit(double creditLimit) { this.creditLimit = creditLimit; }
    public double getUsedCredit() { return usedCredit; }
    public double getMinimumPayment() { return minimumPayment; }
    public Date getPaymentDueDate() { return paymentDueDate; }
    public void setPaymentDueDate(Date paymentDueDate) { this.paymentDueDate = paymentDueDate; }
    public double getInterestRate() { return interestRate; }
    public void setInterestRate(double interestRate) { this.interestRate = interestRate; }

    @Override
    public String toString() {
        return super.toString() + " | LimitaCredit: " + creditLimit
                + " | CreditFolosit: " + usedCredit
                + " | Disponibil: " + getAvailableCredit();
    }
}