public class Withdrawal extends Transaction {

    private BankAccount sourceAccount;
    private Card usedCard; //poate fi null pentru retrageri de la ghiseu
    private String location;

    //constructor
    public Withdrawal(String transactionId, double amount, String currency,
                      BankAccount sourceAccount, Card usedCard, String location) {
        super(transactionId, amount, currency, TransactionType.WITHDRAWAL,
                "Retragere ATM la: " + location);
        this.sourceAccount = sourceAccount;
        this.usedCard = usedCard;
        this.location = location;
    }

    //verifica cardul doar daca exista, apoi retrage din cont
    @Override
    public void execute() {
        try {
            if (usedCard != null && usedCard.getStatus() != Card.CardStatus.ACTIVE)
                throw new IllegalStateException("Cardul nu este activ.");
            sourceAccount.withdraw(getAmount());
            setStatus(TransactionStatus.COMPLETED);
            sourceAccount.addTransaction(this);
            System.out.println("Retragere de " + getAmount() + " " + getCurrency()
                    + " din contul " + sourceAccount.getIBAN() + " la " + location);
        } catch (Exception e) {
            setStatus(TransactionStatus.FAILED);
            System.out.println("Retragere esuata: " + e.getMessage());
        }
    }

    //inverseaza retragerea prin depunerea sumei inapoi
    @Override
    public void rollback() {
        if (getStatus() == TransactionStatus.COMPLETED) {
            sourceAccount.deposit(getAmount());
            setStatus(TransactionStatus.REVERSED);
            System.out.println("Retragere reversata.");
        }
    }

    public BankAccount getSourceAccount() { return sourceAccount; }
    public Card getUsedCard() { return usedCard; }
    public String getLocation() { return location; }
}