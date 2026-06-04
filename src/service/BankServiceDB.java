package service;

import audit.AuditService;
import audit.AuditService.Action;
import db.DatabaseConnection;
import db.DatabaseInitializer;
import repository.*;
import repository.UserRepository.UserDTO;
import repository.BankAccountRepository.BankAccountDTO;
import repository.CardRepository.CardDTO;
import repository.LoanRepository.LoanDTO;
import repository.TransactionRepository.TransactionDTO;

import java.sql.SQLException;
import java.util.*;

/**
 * Serviciu bancar extins cu persistenta JDBC si audit CSV.
 *
 * Aceasta clasa:
 *  - Extinde logica din BankService (etapa I)
 *  - Persista toate entitatile in SQLite prin repository-urile singleton
 *  - Auditeaza fiecare actiune in fisierul audit_log.csv
 *
 * Toate metodele sunt self-contained: nu depind de obiectele Java din memorie
 * dupa repornire — toate datele se regasesc in DB.
 */
public class BankServiceDB {

    // Repository-uri singleton
    private final UserRepository        userRepo        = UserRepository.getInstance();
    private final BankAccountRepository accountRepo     = BankAccountRepository.getInstance();
    private final CardRepository        cardRepo        = CardRepository.getInstance();
    private final LoanRepository        loanRepo        = LoanRepository.getInstance();
    private final TransactionRepository transactionRepo = TransactionRepository.getInstance();

    // Serviciu de audit singleton
    private final AuditService audit = AuditService.getInstance();

    // -------------------------------------------------------------------------
    // Initializare
    // -------------------------------------------------------------------------

    public BankServiceDB() throws SQLException {
        DatabaseInitializer.initialize();
        System.out.println("[BankServiceDB] Serviciu bancar cu persistenta initializat.");
    }

    // -------------------------------------------------------------------------
    // USER CRUD
    // -------------------------------------------------------------------------

    /** Inregistreaza un client si il persista in DB. */
    public UserDTO registerCustomer(String firstName, String lastName, String cnp,
                                    String email, String phone, Date dob) throws SQLException {
        String id = "USR-" + System.currentTimeMillis();
        UserDTO user = new UserDTO(id, firstName, lastName, cnp, email, phone, dob, "ACTIVE");
        userRepo.save(user);
        audit.log(Action.REGISTER_CUSTOMER, firstName + " " + lastName);
        System.out.println("Client inregistrat: " + firstName + " " + lastName + " (ID: " + id + ")");
        return user;
    }

    /** Actualizeaza datele unui client. */
    public void updateCustomer(UserDTO user) throws SQLException {
        userRepo.update(user);
        audit.log(Action.DB_UPDATE_USER, user.customerId);
    }

    /** Sterge un client din DB. */
    public void deleteCustomer(String customerId) throws SQLException {
        userRepo.delete(customerId);
        audit.log(Action.DB_DELETE_USER, customerId);
    }

    /** Cauta un client dupa ID. */
    public Optional<UserDTO> findCustomerById(String id) throws SQLException {
        audit.log(Action.FIND_CUSTOMER_BY_ID, id);
        return userRepo.findById(id);
    }

    /** Returneaza toti clientii. */
    public List<UserDTO> getAllCustomers() throws SQLException {
        return userRepo.findAll();
    }

    // -------------------------------------------------------------------------
    // BANK ACCOUNT CRUD
    // -------------------------------------------------------------------------

    /** Deschide un cont nou si il persista in DB. */
    public BankAccountDTO openAccount(UserDTO owner, String accountType, String currency)
            throws SQLException, InterruptedException {
        Thread.sleep(1); // Asigura unicitate timestamp
        String id   = "ACC-" + System.currentTimeMillis();
        String iban = generateIBAN();
        BankAccountDTO acc = new BankAccountDTO(id, iban, currency,
                accountType.toUpperCase(), "ACTIVE", 0.0, owner.customerId, new Date());

        switch (accountType.toUpperCase()) {
            case "SAVINGS":
                acc.interestRate      = 5.5;
                acc.lockPeriodMonths  = 6;
                acc.minimumBalance    = 100.0;
                break;
            case "CURRENT":
                acc.overdraftLimit = 1000.0;
                acc.overdraftUsed  = 0.0;
                acc.monthlyFee     = 5.0;
                break;
            default:
                throw new IllegalArgumentException("Tip cont necunoscut: " + accountType);
        }

        accountRepo.save(acc);
        audit.log(Action.OPEN_ACCOUNT, iban + " (" + accountType + ")");
        System.out.println("Cont deschis: " + iban + " (" + accountType + ")");
        return acc;
    }

    /** Actualizeaza un cont (sold, status etc.). */
    public void updateAccount(BankAccountDTO acc) throws SQLException {
        accountRepo.update(acc);
        audit.log(Action.DB_UPDATE_ACCOUNT, acc.accountId);
    }

    /** Sterge un cont din DB. */
    public void deleteAccount(String accountId) throws SQLException {
        accountRepo.delete(accountId);
        audit.log(Action.DB_DELETE_ACCOUNT, accountId);
    }

    /** Cauta cont dupa IBAN. */
    public Optional<BankAccountDTO> findAccountByIBAN(String iban) throws SQLException {
        audit.log(Action.FIND_ACCOUNT_BY_IBAN, iban);
        return accountRepo.findByIBAN(iban);
    }

    /** Returneaza toate conturile. */
    public List<BankAccountDTO> getAllAccounts() throws SQLException {
        return accountRepo.findAll();
    }

    // -------------------------------------------------------------------------
    // DEPOSIT
    // -------------------------------------------------------------------------

    public void deposit(BankAccountDTO acc, double amount, String source) throws SQLException {
        if (amount <= 0) throw new IllegalArgumentException("Suma trebuie sa fie pozitiva.");
        if (!"ACTIVE".equals(acc.status)) throw new IllegalStateException("Contul nu este activ.");

        acc.balance += amount;
        accountRepo.updateBalance(acc.iban, acc.balance);

        String txId = "TX-" + System.currentTimeMillis();
        TransactionDTO tx = new TransactionDTO(txId, amount, acc.currency,
                "DEPOSIT", "COMPLETED", "Depunere din: " + source, "REF-" + txId, new Date());
        tx.targetAccountId = acc.accountId;
        tx.depositSource   = source;
        transactionRepo.save(tx);

        audit.log(Action.DEPOSIT, amount + " " + acc.currency + " -> " + acc.iban);
        System.out.println("Depunere de " + amount + " " + acc.currency
                + " in contul " + acc.iban + " realizata cu succes.");
    }

    // -------------------------------------------------------------------------
    // WITHDRAWAL
    // -------------------------------------------------------------------------

    public void withdraw(BankAccountDTO acc, double amount, String cardId, String location)
            throws SQLException {
        if (amount <= 0) throw new IllegalArgumentException("Suma trebuie sa fie pozitiva.");
        if (!"ACTIVE".equals(acc.status)) throw new IllegalStateException("Contul nu este activ.");

        if ("CURRENT".equals(acc.accountType)) {
            double overdraftAvail = (acc.overdraftLimit != null ? acc.overdraftLimit : 0)
                    - (acc.overdraftUsed != null ? acc.overdraftUsed : 0);
            double disponibil = acc.balance + overdraftAvail;
            if (amount > disponibil) throw new IllegalStateException("Depasesti limita de overdraft.");
            if (amount <= acc.balance) {
                acc.balance -= amount;
            } else {
                double dinOverdraft = amount - acc.balance;
                acc.balance = 0;
                acc.overdraftUsed = (acc.overdraftUsed != null ? acc.overdraftUsed : 0) + dinOverdraft;
            }
        } else {
            double minBal = acc.minimumBalance != null ? acc.minimumBalance : 0;
            if (acc.balance - amount < minBal)
                throw new IllegalStateException("Nu poti retrage sub soldul minim.");
            acc.balance -= amount;
        }

        accountRepo.update(acc);

        String txId = "TX-" + System.currentTimeMillis();
        TransactionDTO tx = new TransactionDTO(txId, amount, acc.currency,
                "WITHDRAWAL", "COMPLETED", "Retragere ATM la: " + location, "REF-" + txId, new Date());
        tx.sourceAccountId = acc.accountId;
        tx.cardId          = cardId;
        tx.location        = location;
        transactionRepo.save(tx);

        audit.log(Action.WITHDRAWAL, amount + " " + acc.currency + " din " + acc.iban);
        System.out.println("Retragere de " + amount + " " + acc.currency
                + " din contul " + acc.iban + " la " + location);
    }

    // -------------------------------------------------------------------------
    // TRANSFER
    // -------------------------------------------------------------------------

    public void transfer(BankAccountDTO src, BankAccountDTO dst, double amount) throws SQLException {
        if (amount <= 0) throw new IllegalArgumentException("Suma trebuie sa fie pozitiva.");
        if (src.balance < amount) throw new IllegalStateException("Fonduri insuficiente.");

        src.balance -= amount;
        dst.balance += amount;
        accountRepo.updateBalance(src.iban, src.balance);
        accountRepo.updateBalance(dst.iban, dst.balance);

        String txId = "TX-" + System.currentTimeMillis();
        TransactionDTO tx = new TransactionDTO(txId, amount, src.currency,
                "TRANSFER", "COMPLETED", "Transfer intre conturi", "REF-" + txId, new Date());
        tx.sourceAccountId = src.accountId;
        tx.targetAccountId = dst.accountId;
        transactionRepo.save(tx);

        audit.log(Action.TRANSFER, amount + " " + src.currency
                + " din " + src.iban + " catre " + dst.iban);
        System.out.println("Transfer efectuat: " + amount + " " + src.currency
                + " din " + src.iban + " catre " + dst.iban);
    }

    // -------------------------------------------------------------------------
    // CARD CRUD
    // -------------------------------------------------------------------------

    /** Emite un card si il persista in DB. */
    public CardDTO issueCard(UserDTO owner, BankAccountDTO account, String cardType)
            throws SQLException, InterruptedException {
        Thread.sleep(1);
        String cardId     = "CRD-" + System.currentTimeMillis();
        String cardNumber = generateCardNumber();
        String cvv        = String.valueOf((int)(Math.random() * 900) + 100);

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.YEAR, 4);
        Date expiryDate = cal.getTime();

        CardDTO card = new CardDTO(cardId, cardNumber, owner.firstName + " " + owner.lastName,
                cvv, expiryDate, new Date(), "ACTIVE", cardType.toUpperCase(),
                account.accountId, owner.customerId);

        if ("CREDIT".equalsIgnoreCase(cardType)) {
            card.creditLimit    = 5000.0;
            card.usedCredit     = 0.0;
            card.interestRate   = 18.5;
            card.paymentDueDate = new Date();
        } else {
            card.dailyWithdrawalLimit = 2000.0;
            card.dailyPaymentLimit    = 5000.0;
        }

        cardRepo.save(card);
        audit.log(Action.ISSUE_CARD, card.maskedNumber() + " (" + cardType + ")");
        System.out.println("Card emis: " + card.maskedNumber() + " (" + cardType + ")");
        return card;
    }

    /** Blocheaza un card. */
    public void blockCard(CardDTO card) throws SQLException {
        card.status = "BLOCKED";
        cardRepo.updateStatus(card.cardId, "BLOCKED");
        audit.log(Action.BLOCK_CARD, card.maskedNumber());
        System.out.println("Cardul " + card.maskedNumber() + " a fost blocat.");
    }

    /** Deblocheaza un card. */
    public void unblockCard(CardDTO card) throws SQLException {
        card.status = "ACTIVE";
        cardRepo.updateStatus(card.cardId, "ACTIVE");
        audit.log(Action.UNBLOCK_CARD, card.maskedNumber());
        System.out.println("Cardul " + card.maskedNumber() + " a fost deblocat.");
    }

    /** Sterge un card din DB. */
    public void deleteCard(String cardId) throws SQLException {
        cardRepo.delete(cardId);
        audit.log(Action.DB_DELETE_CARD, cardId);
    }

    /** Returneaza toate cardurile unui client. */
    public List<CardDTO> getCardsForOwner(String ownerId) throws SQLException {
        return cardRepo.findByOwner(ownerId);
    }

    // -------------------------------------------------------------------------
    // CARD PAYMENT
    // -------------------------------------------------------------------------

    public void cardPayment(CardDTO card, BankAccountDTO linkedAccount,
                            double amount, String merchant, String category) throws SQLException {
        if (!"ACTIVE".equals(card.status))
            throw new IllegalStateException("Cardul nu este activ.");

        if ("CREDIT".equals(card.cardType)) {
            double available = (card.creditLimit != null ? card.creditLimit : 0)
                    - (card.usedCredit != null ? card.usedCredit : 0);
            if (amount > available) throw new IllegalStateException("Credit insuficient.");
            card.usedCredit = (card.usedCredit != null ? card.usedCredit : 0) + amount;
            cardRepo.update(card);
        } else {
            if (linkedAccount.balance < amount) throw new IllegalStateException("Fonduri insuficiente.");
            linkedAccount.balance -= amount;
            accountRepo.updateBalance(linkedAccount.iban, linkedAccount.balance);
        }

        String txId = "TX-" + System.currentTimeMillis();
        TransactionDTO tx = new TransactionDTO(txId, amount, linkedAccount.currency,
                "CARD_PAYMENT", "COMPLETED", "Plata la: " + merchant, "REF-" + txId, new Date());
        tx.cardId          = card.cardId;
        tx.sourceAccountId = linkedAccount.accountId;
        tx.merchantName    = merchant;
        transactionRepo.save(tx);

        audit.log(Action.CARD_PAYMENT, amount + " " + linkedAccount.currency + " la " + merchant);
        System.out.println("Plata de " + amount + " " + linkedAccount.currency
                + " la " + merchant + " procesata cu succes.");
    }

    // -------------------------------------------------------------------------
    // LOAN CRUD
    // -------------------------------------------------------------------------

    /** Acorda un imprumut si il persista in DB. */
    public LoanDTO grantLoan(UserDTO borrower, double amount, double rate,
                             int months, String loanType) throws SQLException, InterruptedException {
        Thread.sleep(1);
        String loanId = "LOAN-" + System.currentTimeMillis();

        double monthlyRate = rate / 100 / 12;
        double monthlyPayment = monthlyRate == 0
                ? amount / months
                : amount * monthlyRate * Math.pow(1 + monthlyRate, months)
                  / (Math.pow(1 + monthlyRate, months) - 1);

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, months);
        Date endDate = cal.getTime();

        LoanDTO loan = new LoanDTO(loanId, borrower.customerId, amount, amount, rate,
                months, monthlyPayment, new Date(), endDate, "ACTIVE", loanType.toUpperCase());
        loanRepo.save(loan);
        audit.log(Action.GRANT_LOAN, amount + " RON, " + months + " luni, client: " + borrower.customerId);
        System.out.printf("Imprumut acordat clientului %s %s | Suma: %.2f | Rata lunara: %.2f%n",
                borrower.firstName, borrower.lastName, amount, monthlyPayment);
        return loan;
    }

    /** Inregistreaza o plata la imprumut. */
    public void makeLoanPayment(LoanDTO loan, double amount) throws SQLException {
        if (!"ACTIVE".equals(loan.status))
            throw new IllegalStateException("Imprumutul nu este activ.");
        loan.remainingBalance -= amount;
        if (loan.remainingBalance <= 0) {
            loan.remainingBalance = 0;
            loan.status = "PAID";
            System.out.println("Imprumutul " + loan.loanId + " a fost achitat integral!");
        } else {
            System.out.printf("Plata de %.2f procesata. Sold ramas: %.2f%n", amount, loan.remainingBalance);
        }
        loanRepo.update(loan);
        audit.log(Action.LOAN_PAYMENT, loan.loanId + " | plata: " + amount);
    }

    /** Sterge un imprumut din DB. */
    public void deleteLoan(String loanId) throws SQLException {
        loanRepo.delete(loanId);
        audit.log(Action.DB_DELETE_LOAN, loanId);
    }

    /** Returneaza toate imprumuturile unui client. */
    public List<LoanDTO> getLoansForBorrower(String borrowerId) throws SQLException {
        return loanRepo.findByBorrower(borrowerId);
    }

    // -------------------------------------------------------------------------
    // RAPOARTE / UTILITARE
    // -------------------------------------------------------------------------

    /** Afiseaza toate conturile sortate dupa IBAN (TreeMap in DB = ORDER BY iban). */
    public void printAllAccountsSorted() throws SQLException {
        System.out.println("\n=== Conturile sortate dupa IBAN ===");
        List<BankAccountDTO> accounts = accountRepo.findAll();
        accounts.stream()
                .sorted(Comparator.comparing(a -> a.iban))
                .forEach(a -> System.out.printf("%-35s | %-10s | Sold: %10.2f %s%n",
                        a.iban, a.accountType, a.balance, a.currency));
        audit.log(Action.PRINT_ACCOUNTS_SORTED);
    }

    /** Afiseaza detalii complete pentru un client, citind din DB. */
    public void printCustomerDetails(UserDTO user) throws SQLException {
        System.out.println("\n=== Detalii client ===");
        System.out.println("ID: " + user.customerId + " | Nume: " + user.firstName + " " + user.lastName
                + " | CNP: " + user.cnp + " | Email: " + user.email + " | Status: " + user.status);

        System.out.println("  Conturi:");
        accountRepo.findByOwner(user.customerId)
                .forEach(a -> System.out.printf("    %s | %s | %.2f %s | %s%n",
                        a.iban, a.accountType, a.balance, a.currency, a.status));

        System.out.println("  Carduri:");
        cardRepo.findByOwner(user.customerId)
                .forEach(c -> System.out.printf("    %s | %s | %s%n",
                        c.maskedNumber(), c.cardType, c.status));

        System.out.println("  Imprumuturi:");
        loanRepo.findByBorrower(user.customerId)
                .forEach(l -> System.out.printf("    %s | %s | Principal: %.2f | Ramas: %.2f | %s%n",
                        l.loanId, l.loanType, l.principalAmount, l.remainingBalance, l.status));

        audit.log(Action.PRINT_CUSTOMER_DETAILS, user.customerId);
    }

    /** Genereaza un extras de cont simplu din DB. */
    public void generateStatement(BankAccountDTO acc, Date start, Date end) throws SQLException {
        System.out.println("\n=== Extras de cont: " + acc.iban + " ===");
        System.out.println("Perioada: " + start + " - " + end);
        System.out.printf("Sold curent: %.2f %s%n", acc.balance, acc.currency);
        System.out.println("Tranzactii:");
        transactionRepo.findByDateRange(start, end).stream()
                .filter(tx -> acc.accountId.equals(tx.sourceAccountId)
                        || acc.accountId.equals(tx.targetAccountId))
                .forEach(tx -> System.out.printf("  [%s] %s | %.2f %s | %s%n",
                        tx.transactionType, tx.description, tx.amount, tx.currency, tx.status));
        audit.log(Action.GENERATE_STATEMENT, acc.iban);
    }

    // -------------------------------------------------------------------------
    // Utilitare private
    // -------------------------------------------------------------------------

    private String generateIBAN() {
        return "RO49AAAA" + String.format("%016d",
                (long)(Math.random() * 1_000_000_000_000_000L));
    }

    private String generateCardNumber() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 16; i++) sb.append((int)(Math.random() * 10));
        return sb.toString();
    }

    /** Inchide conexiunea DB si fisierul de audit. */
    public void shutdown() throws SQLException {
        audit.close();
        DatabaseConnection.getInstance().closeConnection();
    }
}
