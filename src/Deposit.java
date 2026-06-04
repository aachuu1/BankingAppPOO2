public class Deposit extends Transaction {

    private BankAccount targetAccount;
    private String depositSource;

    //constructor
    public Deposit(String transactionId, double amount, String currency,
                   BankAccount targetAccount, String depositSource) {
        super(transactionId, amount, currency, TransactionType.DEPOSIT,
                "Depunere din: " + depositSource);
        this.targetAccount = targetAccount;
        this.depositSource = depositSource;
    }

    //depune suma in cont si inregistreaza tranzactia
    @Override
    public void execute() {
        try {
            targetAccount.deposit(getAmount());
            setStatus(TransactionStatus.COMPLETED);
            targetAccount.addTransaction(this);
            System.out.println("Depunere de " + getAmount() + " " + getCurrency()
                    + " in contul " + targetAccount.getIBAN() + " realizata cu succes.");
        } catch (Exception e) {
            setStatus(TransactionStatus.FAILED);
            System.out.println("Depunere esuata: " + e.getMessage());
        }
    }

    //anuleaza depunerea prin retragerea sumei
    @Override
    public void rollback() {
        if (getStatus() == TransactionStatus.COMPLETED) {
            targetAccount.withdraw(getAmount());
            setStatus(TransactionStatus.REVERSED);
            System.out.println("Depunere reversata.");
        }
    }

    public BankAccount getTargetAccount() { return targetAccount; }
    public String getDepositSource() { return depositSource; }
}