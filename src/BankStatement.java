import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class BankStatement {

    private String statementId;
    private BankAccount account;
    private Date startDate;
    private Date endDate;
    private Date generatedAt;
    private double openingBalance;
    private double closingBalance;
    private List<Transaction> transactions;

    //constructor-filtreaza tranzactiile din intervalul dat si calculeaza soldurile
    public BankStatement(String statementId, BankAccount account, Date startDate, Date endDate) {
        this.statementId = statementId;
        this.account = account;
        this.startDate = startDate;
        this.endDate = endDate;
        this.generatedAt = new Date();
        this.transactions = account.getTransactions().stream()
                .filter(t -> !t.getTimestamp().before(startDate) && !t.getTimestamp().after(endDate))
                .collect(Collectors.toList());
        calculateBalances();
    }

    //soldul initial se deduce din soldul curent minus intrarile plus iesirile
    private void calculateBalances() {
        double totalIn = getTotalDeposits();
        double totalOut = getTotalWithdrawals();
        this.closingBalance = account.getBalance();
        this.openingBalance = closingBalance - totalIn + totalOut;
    }

    //sumeaza doar depunerile completate
    public double getTotalDeposits() {
        return transactions.stream()
                .filter(t -> t.getType() == Transaction.TransactionType.DEPOSIT
                        && t.getStatus() == Transaction.TransactionStatus.COMPLETED)
                .mapToDouble(Transaction::getAmount)
                .sum();
    }

    //sumeaza retragerile, platile cu cardul si transferurile completate
    public double getTotalWithdrawals() {
        return transactions.stream()
                .filter(t -> (t.getType() == Transaction.TransactionType.WITHDRAWAL
                        || t.getType() == Transaction.TransactionType.CARD_PAYMENT
                        || t.getType() == Transaction.TransactionType.TRANSFER)
                        && t.getStatus() == Transaction.TransactionStatus.COMPLETED)
                .mapToDouble(Transaction::getAmount)
                .sum();
    }

    public void printStatement() {
        System.out.println("Extras de cont");
        System.out.println("ID Extras:       " + statementId);
        System.out.println("IBAN:            " + account.getIBAN());
        System.out.println("Titular:         " + account.getOwner().getFullName());
        System.out.println("Perioada:        " + startDate + " - " + endDate);
        System.out.println("Sold initial:    " + openingBalance + " " + account.getCurrency());
        System.out.println("Sold final:      " + closingBalance + " " + account.getCurrency());
        System.out.println("Total intrari:   " + getTotalDeposits() + " " + account.getCurrency());
        System.out.println("Total iesiri:    " + getTotalWithdrawals() + " " + account.getCurrency());
        System.out.println("TRANZACTII:");
        if (transactions.isEmpty()) {
            System.out.println("Nu exista tranzactii in aceasta perioada.");
        } else {
            transactions.forEach(t -> System.out.println("  " + t));
        }
    }

    public String getStatementId() { return statementId; }
    public BankAccount getAccount() { return account; }
    public Date getStartDate() { return startDate; }
    public Date getEndDate() { return endDate; }
    public Date getGeneratedAt() { return generatedAt; }
    public double getOpeningBalance() { return openingBalance; }
    public double getClosingBalance() { return closingBalance; }
    public List<Transaction> getTransactions() { return transactions; }
}