import java.util.Date;

public class SavingsAccount extends BankAccount {

    private double interestRate;
    private int lockPeriodMonths; //perioada in care banii sunt blocati
    private double minimumBalance;
    private Date nextInterestDate;

    //constructor
    public SavingsAccount(String accountId, String IBAN, String currency, User owner,
                          double interestRate, int lockPeriodMonths, double minimumBalance) {
        super(accountId, IBAN, currency, AccountType.SAVINGS, owner);
        this.interestRate = interestRate;
        this.lockPeriodMonths = lockPeriodMonths;
        this.minimumBalance = minimumBalance;
        this.nextInterestDate = new Date();
    }

    @Override
    public String getAccountType() {
        return "Cont de Economii";
    }

    //dobanda anuala calculata pe soldul curent
    @Override
    public double calculateInterest() {
        return balance * (interestRate / 100);
    }

    //adauga dobanda direct la sold
    public void applyInterest() {
        double interest = calculateInterest();
        balance += interest;
        System.out.println("Dobanda aplicata: " + interest + " " + getCurrency());
    }

    //suprascriere withdraw: nu permite retragere sub soldul minim
    @Override
    public void withdraw(double amount) {
        if (amount <= 0) throw new IllegalArgumentException("Suma trebuie sa fie pozitiva.");
        if (getStatus() != AccountStatus.ACTIVE) throw new IllegalStateException("Contul nu este activ.");
        if (balance - amount < minimumBalance)
            throw new IllegalStateException("Nu poti retrage sub soldul minim de " + minimumBalance);
        balance -= amount;
    }

    public double getInterestRate() { return interestRate; }
    public void setInterestRate(double interestRate) { this.interestRate = interestRate; }
    public int getLockPeriodMonths() { return lockPeriodMonths; }
    public double getMinimumBalance() { return minimumBalance; }
    public void setMinimumBalance(double minimumBalance) { this.minimumBalance = minimumBalance; }
    public Date getNextInterestDate() { return nextInterestDate; }
    public void setNextInterestDate(Date nextInterestDate) { this.nextInterestDate = nextInterestDate; }

    @Override
    public String toString() {
        return super.toString() + " | Dobanda: " + interestRate + "% | SoldMinim: " + minimumBalance;
    }
}