import java.util.Date;

public class DebitCard extends Card {

    //limite zilnice pentru retrageri si plati
    private double dailyWithdrawalLimit;
    private double dailyPaymentLimit;

    //constructor
    public DebitCard(String cardId, String cardNumber, String cardHolderName,
                     String CVV, Date expiryDate, BankAccount linkedAccount,
                     double dailyWithdrawalLimit, double dailyPaymentLimit) {
        super(cardId, cardNumber, cardHolderName, CVV, expiryDate, CardType.DEBIT, linkedAccount);
        this.dailyWithdrawalLimit = dailyWithdrawalLimit;
        this.dailyPaymentLimit = dailyPaymentLimit;
    }

    @Override
    public String getCardType() {
        return "Card de Debit";
    }

    public double getDailyWithdrawalLimit() { return dailyWithdrawalLimit; }
    public void setDailyWithdrawalLimit(double limit) { this.dailyWithdrawalLimit = limit; }
    public double getDailyPaymentLimit() { return dailyPaymentLimit; }
    public void setDailyPaymentLimit(double limit) { this.dailyPaymentLimit = limit; }

    @Override
    public String toString() {
        return super.toString() + " | LimitaRetragere: " + dailyWithdrawalLimit
                + " | LimitaPlata: " + dailyPaymentLimit;
    }
}