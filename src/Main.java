import java.util.Calendar;
import java.util.Date;

public class Main {

    public static void main(String[] args) {

        BankService bank = new BankService();

        System.out.println(" 1.Inregistrare clienti");
        User ion = bank.registerCustomer("Ion", "Popescu", "1900101123456",
                "ion.popescu@email.com", "0721000001",
                new Date(90, 1, 1));
        User maria = bank.registerCustomer("Maria", "Ionescu", "2950215654321",
                "maria.ionescu@email.com", "0721000002",
                new Date(95, 2, 15));

        System.out.println("\n 2.Deschidere conturi");
        BankAccount contCurentIon   = bank.openAccount(ion, "CURRENT", "RON");
        BankAccount contEconomiiIon = bank.openAccount(ion, "SAVINGS", "RON");
        BankAccount contCurentMaria = bank.openAccount(maria, "CURRENT", "RON");

        System.out.println("\n 3.Depuneri");
        bank.deposit(contCurentIon, 5000.0, "Ghiseu");
        bank.deposit(contEconomiiIon, 2000.0, "Transfer");
        bank.deposit(contCurentMaria, 3000.0, "Ghiseu");

        System.out.println("\n 4.Retragere numerar");
        bank.withdraw(contCurentIon, 500.0, null, "ATM Victoriei");

        System.out.println("\n 5.Transfer intre conturi");
        bank.transfer(contCurentIon, contCurentMaria, 1000.0);

        System.out.println("\n 6.Emitere carduri");
        Card debitIon   = bank.issueCard(ion, contCurentIon, "DEBIT");
        Card creditIon  = bank.issueCard(ion, contCurentIon, "CREDIT");
        Card debitMaria = bank.issueCard(maria, contCurentMaria, "DEBIT");

        System.out.println("\n 7.Plata cu cardul");
        bank.cardPayment(debitIon, 150.0, "Carrefour", "Retail");
        bank.cardPayment(creditIon, 800.0, "Booking.com", "Travel");

        System.out.println("\n 8.Blocare card");
        bank.blockCard(debitMaria);

        System.out.println("\n 9.Acordare imprumut");
        Loan loanIon = bank.grantLoan(ion, 20000.0, 8.5, 60, Loan.LoanType.PERSONAL);
        //prima rata achitata imediat dupa acordare
        loanIon.makePayment(loanIon.getMonthlyPayment());

        System.out.println("\n 10.Generare extras de cont (ultima luna)");
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, -1);
        Date acumOLuna = cal.getTime();
        bank.generateStatement(contCurentIon, acumOLuna, new Date());

        System.out.println("\n Interogare sold:");
        System.out.println("Sold cont Ion (curent): " + contCurentIon.getBalance() + " RON");
        System.out.println("Sold cont Ion (economii): " + contEconomiiIon.getBalance() + " RON");
        System.out.println("Sold cont Maria: " + contCurentMaria.getBalance() + " RON");

        System.out.println("\nCalculare dobanda cont economii Ion:");
        SavingsAccount savings = (SavingsAccount) contEconomiiIon;
        System.out.println("Dobanda anuala estimata: " + savings.calculateInterest() + " RON");

        bank.printAllAccountsSorted();
        bank.printCustomerDetails(ion);
    }
}