import service.BankServiceDB;
import repository.UserRepository.UserDTO;
import repository.BankAccountRepository.BankAccountDTO;
import repository.CardRepository.CardDTO;
import repository.LoanRepository.LoanDTO;

import java.util.Calendar;
import java.util.Date;

/**
 * Clasa principala pentru Etapa II.
 * Demonstreaza persistenta JDBC si auditul CSV.
 */
public class MainDB {

    public static void main(String[] args) {
        BankServiceDB bank = null;
        try {
            bank = new BankServiceDB();

            System.out.println("\n===== 1. Inregistrare clienti =====");
            UserDTO ion   = bank.registerCustomer("Ion", "Popescu", "1900101123456",
                    "ion.popescu@email.com", "0721000001", new Date(90, 1, 1));
            UserDTO maria = bank.registerCustomer("Maria", "Ionescu", "2950215654321",
                    "maria.ionescu@email.com", "0721000002", new Date(95, 2, 15));

            System.out.println("\n===== 2. Deschidere conturi =====");
            BankAccountDTO contCurentIon   = bank.openAccount(ion,   "CURRENT", "RON");
            BankAccountDTO contEconomiiIon = bank.openAccount(ion,   "SAVINGS", "RON");
            BankAccountDTO contCurentMaria = bank.openAccount(maria, "CURRENT", "RON");

            System.out.println("\n===== 3. Depuneri =====");
            bank.deposit(contCurentIon,   5000.0, "Ghiseu");
            bank.deposit(contEconomiiIon, 2000.0, "Transfer");
            bank.deposit(contCurentMaria, 3000.0, "Ghiseu");

            System.out.println("\n===== 4. Retragere numerar =====");
            bank.withdraw(contCurentIon, 500.0, null, "ATM Victoriei");

            System.out.println("\n===== 5. Transfer intre conturi =====");
            bank.transfer(contCurentIon, contCurentMaria, 1000.0);

            System.out.println("\n===== 6. Emitere carduri =====");
            CardDTO debitIon   = bank.issueCard(ion,   contCurentIon,   "DEBIT");
            CardDTO creditIon  = bank.issueCard(ion,   contCurentIon,   "CREDIT");
            CardDTO debitMaria = bank.issueCard(maria, contCurentMaria, "DEBIT");

            System.out.println("\n===== 7. Plata cu cardul =====");
            bank.cardPayment(debitIon,  contCurentIon,   150.0, "Carrefour",  "Retail");
            bank.cardPayment(creditIon, contCurentIon,   800.0, "Booking.com","Travel");

            System.out.println("\n===== 8. Blocare card =====");
            bank.blockCard(debitMaria);

            System.out.println("\n===== 9. Acordare imprumut =====");
            LoanDTO loanIon = bank.grantLoan(ion, 20000.0, 8.5, 60, "PERSONAL");
            bank.makeLoanPayment(loanIon, loanIon.monthlyPayment);

            System.out.println("\n===== 10. Extras de cont (ultima luna) =====");
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.MONTH, -1);
            bank.generateStatement(contCurentIon, cal.getTime(), new Date());

            System.out.println("\n===== 11. Interogare solduri =====");
            System.out.printf("Sold Ion (curent):   %.2f RON%n", contCurentIon.balance);
            System.out.printf("Sold Ion (economii): %.2f RON%n", contEconomiiIon.balance);
            System.out.printf("Sold Maria:          %.2f RON%n", contCurentMaria.balance);

            System.out.println("\n===== 12. Demo UPDATE si DELETE (CRUD complet) =====");
            // Update user
            ion.email = "ion.popescu.nou@email.com";
            bank.updateCustomer(ion);

            // Cauta cont dupa IBAN
            bank.findAccountByIBAN(contCurentIon.iban)
                    .ifPresent(a -> System.out.println("Cont gasit: " + a));

            // Update cont
            contEconomiiIon.interestRate = 6.0;
            bank.updateAccount(contEconomiiIon);

            // Delete - cream un cont de test si il stergem
            BankAccountDTO contTest = bank.openAccount(ion, "CURRENT", "EUR");
            System.out.println("Cont test creat: " + contTest.iban);
            bank.deleteAccount(contTest.accountId);

            // Stergem cardul blocat
            bank.deleteCard(debitMaria.cardId);

            System.out.println("\n===== 13. Rapoarte finale =====");
            bank.printAllAccountsSorted();
            bank.printCustomerDetails(ion);

            System.out.println("\n===== 14. Toti clientii din DB =====");
            bank.getAllCustomers().forEach(u ->
                    System.out.println("  " + u.customerId + " | " + u.firstName + " " + u.lastName
                            + " | " + u.status));

            System.out.println("\n[OK] Demonstratie Etapa II finalizata.");
            System.out.println("  -> Baza de date: banking.db");
            System.out.println("  -> Fisier audit: audit_log.csv");

        } catch (Exception e) {
            System.err.println("[EROARE] " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (bank != null) {
                try { bank.shutdown(); } catch (Exception ignored) {}
            }
        }
    }
}
