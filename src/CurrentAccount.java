public class CurrentAccount extends BankAccount {

    private double overdraftLimit;
    private double overdraftUsed;
    private double monthlyFee;

    //constructor
    public CurrentAccount(String accountId, String IBAN, String currency, User owner,
                          double overdraftLimit, double monthlyFee) {
        super(accountId, IBAN, currency, AccountType.CURRENT, owner);
        this.overdraftLimit = overdraftLimit;
        this.overdraftUsed = 0.0;
        this.monthlyFee = monthlyFee;
    }

    @Override
    public String getAccountType() {
        return "Cont Curent";
    }

    @Override
    public double calculateInterest() {
        return 0.0; //contul curent nu genereaza dobanda
    }

    //suprascriere withdraw: permite retragere si din overdraft daca soldul nu e suficient
    @Override
    public void withdraw(double amount) {
        if (amount <= 0) throw new IllegalArgumentException("Suma trebuie sa fie pozitiva.");
        if (getStatus() != AccountStatus.ACTIVE) throw new IllegalStateException("Contul nu este activ.");

        double disponibil = balance + (overdraftLimit - overdraftUsed);
        if (amount > disponibil) throw new IllegalStateException("Depasesti limita de overdraft.");

        if (amount <= balance) {
            balance -= amount;
        } else {
            double dinOverdraft = amount - balance;
            balance = 0;
            overdraftUsed += dinOverdraft;
        }
    }

    public double getOverdraftLimit() { return overdraftLimit; }
    public void setOverdraftLimit(double overdraftLimit) { this.overdraftLimit = overdraftLimit; }
    public double getOverdraftUsed() { return overdraftUsed; }
    public double getMonthlyFee() { return monthlyFee; }
    public void setMonthlyFee(double monthlyFee) { this.monthlyFee = monthlyFee; }
    public double getAvailableOverdraft() { return overdraftLimit - overdraftUsed; }

    @Override
    public String toString() {
        return super.toString() + " | Overdraft: " + overdraftLimit + " | ComisionLunar: " + monthlyFee;
    }
}