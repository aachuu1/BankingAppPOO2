public class Transfer extends Transaction {

    private BankAccount sourceAccount;
    private BankAccount destinationAccount;
    private double exchangeRate;

    //constructor
    public Transfer(String transactionId, double amount, String currency,
                    BankAccount sourceAccount, BankAccount destinationAccount, String description) {
        super(transactionId, amount, currency, TransactionType.TRANSFER, description);
        this.sourceAccount = sourceAccount;
        this.destinationAccount = destinationAccount;
        this.exchangeRate = 1.0;
    }

    //retrage din sursa si depune in destinatie, tinand cont de cursul valutar
    @Override
    public void execute() {
        try {
            sourceAccount.withdraw(getAmount());
            destinationAccount.deposit(getAmount() * exchangeRate);
            setStatus(TransactionStatus.COMPLETED);
            sourceAccount.addTransaction(this);
            destinationAccount.addTransaction(this);
            System.out.println("Transfer efectuat: " + getAmount() + " " + getCurrency()
                    + " din " + sourceAccount.getIBAN() + " catre " + destinationAccount.getIBAN());
        } catch (Exception e) {
            setStatus(TransactionStatus.FAILED);
            System.out.println("Transfer esuat: " + e.getMessage());
        }
    }

    //inverseaza transferul doar daca a fost completat
    @Override
    public void rollback() {
        if (getStatus() == TransactionStatus.COMPLETED) {
            destinationAccount.withdraw(getAmount() * exchangeRate);
            sourceAccount.deposit(getAmount());
            setStatus(TransactionStatus.REVERSED);
            System.out.println("Transfer reversat cu succes.");
        }
    }

    public BankAccount getSourceAccount() { return sourceAccount; }
    public BankAccount getDestinationAccount() { return destinationAccount; }
    public double getExchangeRate() { return exchangeRate; }
    public void setExchangeRate(double exchangeRate) { this.exchangeRate = exchangeRate; }
}