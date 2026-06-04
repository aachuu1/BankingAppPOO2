public class CardPayment extends Transaction {

    private Card usedCard;
    private String merchantName;
    private String merchantCategory;

    //constructor
    public CardPayment(String transactionId, double amount, String currency,
                       Card usedCard, String merchantName, String merchantCategory) {
        super(transactionId, amount, currency, TransactionType.CARD_PAYMENT,
                "Plata la: " + merchantName);
        this.usedCard = usedCard;
        this.merchantName = merchantName;
        this.merchantCategory = merchantCategory;
    }

    //verifica statusul cardului, apoi debiteaza creditul sau contul linked
    @Override
    public void execute() {
        try {
            if (usedCard.getStatus() != Card.CardStatus.ACTIVE)
                throw new IllegalStateException("Cardul nu este activ.");
            if (usedCard.isExpired())
                throw new IllegalStateException("Cardul este expirat.");

            if (usedCard instanceof CreditCard) {
                ((CreditCard) usedCard).charge(getAmount());
            } else {
                usedCard.getLinkedAccount().withdraw(getAmount());
            }

            setStatus(TransactionStatus.COMPLETED);
            usedCard.getLinkedAccount().addTransaction(this);
            System.out.println("Plata de " + getAmount() + " " + getCurrency()
                    + " la " + merchantName + " procesata cu succes.");
        } catch (Exception e) {
            setStatus(TransactionStatus.FAILED);
            System.out.println("Plata esuata: " + e.getMessage());
        }
    }

    //inverseaza plata doar daca a fost completata
    @Override
    public void rollback() {
        if (getStatus() == TransactionStatus.COMPLETED) {
            if (usedCard instanceof CreditCard) {
                ((CreditCard) usedCard).makePayment(getAmount());
            } else {
                usedCard.getLinkedAccount().deposit(getAmount());
            }
            setStatus(TransactionStatus.REVERSED);
            System.out.println("Plata reversata.");
        }
    }

    public Card getUsedCard() { return usedCard; }
    public String getMerchantName() { return merchantName; }
    public String getMerchantCategory() { return merchantCategory; }
}