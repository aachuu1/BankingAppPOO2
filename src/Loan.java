import java.util.Date;

public class Loan {

    public enum LoanStatus {
        ACTIVE, PAID, DEFAULTED
    }

    public enum LoanType {
        PERSONAL, MORTGAGE, AUTO
    }

    private String loanId;
    private User borrower;
    private double principalAmount;
    private double remainingBalance;
    private double interestRate;
    private int termMonths;
    private double monthlyPayment;
    private Date startDate;
    private Date endDate;
    private LoanStatus status;
    private LoanType type;

    //constructor - calculeaza rata lunara si data scadenta automat
    public Loan(String loanId, User borrower, double principalAmount,
                double interestRate, int termMonths, LoanType type) {
        this.loanId = loanId;
        this.borrower = borrower;
        this.principalAmount = principalAmount;
        this.remainingBalance = principalAmount;
        this.interestRate = interestRate;
        this.termMonths = termMonths;
        this.type = type;
        this.startDate = new Date();
        this.status = LoanStatus.ACTIVE;
        this.monthlyPayment = calculateMonthlyPayment();

        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.setTime(startDate);
        cal.add(java.util.Calendar.MONTH, termMonths);
        this.endDate = cal.getTime();
    }

    //formula pentru rata lunara; daca dobanda e 0, se imparte egal
    public double calculateMonthlyPayment() {
        double monthlyRate = interestRate / 100 / 12;
        if (monthlyRate == 0) return principalAmount / termMonths;
        return principalAmount * monthlyRate * Math.pow(1 + monthlyRate, termMonths)
                / (Math.pow(1 + monthlyRate, termMonths) - 1);
    }

    //reduce soldul ramas si marcheaza imprumutul ca achitat daca ajunge la 0
    public void makePayment(double amount) {
        if (status != LoanStatus.ACTIVE)
            throw new IllegalStateException("Imprumutul nu este activ.");
        remainingBalance -= amount;
        if (remainingBalance <= 0) {
            remainingBalance = 0;
            status = LoanStatus.PAID;
            System.out.println("Imprumutul " + loanId + " a fost achitat integral!");
        } else {
            System.out.println("Plata de " + amount + " procesata. Sold ramas: " + remainingBalance);
        }
    }

    public String getLoanId() { return loanId; }
    public User getBorrower() { return borrower; }
    public double getPrincipalAmount() { return principalAmount; }
    public double getRemainingBalance() { return remainingBalance; }
    public double getInterestRate() { return interestRate; }
    public int getTermMonths() { return termMonths; }
    public double getMonthlyPayment() { return monthlyPayment; }
    public Date getStartDate() { return startDate; }
    public Date getEndDate() { return endDate; }
    public LoanStatus getStatus() { return status; }
    public LoanType getType() { return type; }

    @Override
    public String toString() {
        return "Loan{" +
                "ID='" + loanId + '\'' +
                ", Tip=" + type +
                ", Suma=" + principalAmount +
                ", SoldRamas=" + remainingBalance +
                ", RataLunara=" + String.format("%.2f", monthlyPayment) +
                ", Status=" + status +
                '}';
    }
}