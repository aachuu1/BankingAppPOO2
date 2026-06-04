import java.util.*;

public class BankService {

    //stocheaza clientii dupa id si conturile sortate dupa IBAN
    private Map<String, User> customers;
    private TreeMap<String, BankAccount> accounts;
    private List<Transaction> allTransactions;
    private int transactionCounter;

    //constructor
    public BankService() {
        this.customers = new HashMap<>();
        this.accounts = new TreeMap<>();
        this.allTransactions = new ArrayList<>();
        this.transactionCounter = 1;
    }

    //creeaza un user nou si il adauga in map
    public User registerCustomer(String firstName, String lastName, String CNP,
                                 String email, String phone, Date dob) {
        String id = "USR-" + System.currentTimeMillis();
        User user = new User(id, firstName, lastName, CNP, email, phone, dob);
        customers.put(id, user);
        System.out.println("Client inregistrat: " + user.getFullName() + " (ID: " + id + ")");
        return user;
    }

    //instantiaza tipul de cont corespunzator si il leaga de user
    public BankAccount openAccount(User user, String accountType, String currency) {
        String id = "ACC-" + System.currentTimeMillis();
        String iban = generateIBAN();
        BankAccount account;

        switch (accountType.toUpperCase()) {
            case "CURRENT":
                account = new CurrentAccount(id, iban, currency, user, 1000.0, 5.0);
                break;
            case "SAVINGS":
                account = new SavingsAccount(id, iban, currency, user, 5.5, 6, 100.0);
                break;
            default:
                throw new IllegalArgumentException("Tip cont necunoscut: " + accountType);
        }

        accounts.put(iban, account);
        user.addAccount(account);
        System.out.println("Cont deschis: " + iban + " (" + account.getAccountType() + ")");
        return account;
    }

    //creeaza si executa o tranzactie de depunere
    public void deposit(BankAccount account, double amount, String source) {
        String txId = "TX-" + transactionCounter++;
        Deposit deposit = new Deposit(txId, amount, account.getCurrency(), account, source);
        deposit.execute();
        allTransactions.add(deposit);
    }

    //creeaza si executa o tranzactie de retragere
    public void withdraw(BankAccount account, double amount, Card card, String location) {
        String txId = "TX-" + transactionCounter++;
        Withdrawal withdrawal = new Withdrawal(txId, amount, account.getCurrency(), account, card, location);
        withdrawal.execute();
        allTransactions.add(withdrawal);
    }

    //creeaza si executa un transfer intre doua conturi
    public void transfer(BankAccount source, BankAccount destination, double amount) {
        String txId = "TX-" + transactionCounter++;
        Transfer transfer = new Transfer(txId, amount, source.getCurrency(), source, destination,
                "Transfer intre conturi");
        transfer.execute();
        allTransactions.add(transfer);
    }

    //emite un card de debit sau credit si il asociaza userului si contului
    public Card issueCard(User user, BankAccount account, String cardType) {
        String cardId = "CRD-" + System.currentTimeMillis();
        String cardNumber = generateCardNumber();
        String cvv = String.valueOf((int)(Math.random() * 900) + 100);

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.YEAR, 4);
        Date expiryDate = cal.getTime();

        Card card;
        if (cardType.equalsIgnoreCase("CREDIT")) {
            card = new CreditCard(cardId, cardNumber, user.getFullName(), cvv,
                    expiryDate, account, 5000.0, 18.5, new Date());
        } else {
            card = new DebitCard(cardId, cardNumber, user.getFullName(), cvv,
                    expiryDate, account, 2000.0, 5000.0);
        }

        account.addCard(card);
        user.addCard(card);
        System.out.println("Card emis: " + card.maskedCardNumber() + " (" + card.getCardType() + ")");
        return card;
    }

    public void blockCard(Card card) {
        card.block();
    }

    public BankStatement generateStatement(BankAccount account, Date startDate, Date endDate) {
        String stmtId = "STMT-" + System.currentTimeMillis();
        BankStatement statement = new BankStatement(stmtId, account, startDate, endDate);
        statement.printStatement();
        return statement;
    }

    //creeaza si executa o plata cu cardul
    public void cardPayment(Card card, double amount, String merchant, String category) {
        String txId = "TX-" + transactionCounter++;
        CardPayment payment = new CardPayment(txId, amount, card.getLinkedAccount().getCurrency(),
                card, merchant, category);
        payment.execute();
        allTransactions.add(payment);
    }

    //acorda un imprumut unui client si il adauga in lista lui de imprumuturi
    public Loan grantLoan(User user, double amount, double rate, int months, Loan.LoanType type) {
        String loanId = "LOAN-" + System.currentTimeMillis();
        Loan loan = new Loan(loanId, user, amount, rate, months, type);
        user.addLoan(loan);
        System.out.println("Imprumut acordat clientului " + user.getFullName()
                + " | Suma: " + amount + " | Rata lunara: "
                + String.format("%.2f", loan.getMonthlyPayment()));
        return loan;
    }

    public void printAllAccountsSorted() {
        System.out.println("Conturile sortate dupa IBAN");
        accounts.forEach((iban, acc) ->
                System.out.println(iban + " | " + acc.getAccountType()
                        + " | Sold: " + acc.getBalance() + " " + acc.getCurrency()));
    }

    public void printCustomerDetails(User user) {
        System.out.println("Detalii client");
        System.out.println(user);
        System.out.println("Conturi:");
        user.getAccounts().forEach(a -> System.out.println("  " + a));
        System.out.println("Carduri:");
        user.getCards().forEach(c -> System.out.println("  " + c));
        System.out.println("Imprumuturi:");
        user.getLoans().forEach(l -> System.out.println("  " + l));
    }

    public BankAccount findAccountByIBAN(String iban) {
        return accounts.get(iban);
    }

    public User findCustomerById(String id) {
        return customers.get(id);
    }

    //genereaza un IBAN
    private String generateIBAN() {
        return "RO49AAAA" + String.format("%016d", (long)(Math.random() * 1_000_000_000_000_000L));
    }

    //genereaza un numar de card
    private String generateCardNumber() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 16; i++) sb.append((int)(Math.random() * 10));
        return sb.toString();
    }

    public Map<String, User> getCustomers() { return customers; }
    public TreeMap<String, BankAccount> getAccounts() { return accounts; }
    public List<Transaction> getAllTransactions() { return allTransactions; }
}