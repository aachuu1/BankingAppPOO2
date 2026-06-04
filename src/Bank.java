public class Bank {
    private String bankId;
    private String bankName;
    private String BIC;
    private String country;
    private String headOfficeAddress;
    //gestioneaza clientii si conturile bancii
    private BankService bankService;

    //constructor
    public Bank(String bankId, String bankName, String BIC, String country, String headOfficeAddress) {
        this.bankId = bankId;
        this.bankName = bankName;
        this.BIC = BIC;
        this.country = country;
        this.headOfficeAddress = headOfficeAddress;
        this.bankService = new BankService();
    }
    //getteri si setteri
    public BankService getService() {
        return bankService;
    }

    public String getBankId() { return bankId; }

    public String getBankName() { return bankName; }
    public void setBankName(String bankName) { this.bankName = bankName; }

    public String getBIC() { return BIC; }

    public String getCountry() { return country; }

    public String getHeadOfficeAddress() { return headOfficeAddress; }
    public void setHeadOfficeAddress(String headOfficeAddress) { this.headOfficeAddress = headOfficeAddress; }

    //numarul de clienti
    public int getTotalCustomers() {
        return bankService.getCustomers().size();
    }

    //numarul de conturi
    public int getTotalAccounts() {
        return bankService.getAccounts().size();
    }

    public void printBankInfo() {
        System.out.println("Informatii Banca");
        System.out.println("Nume:        " + bankName);
        System.out.println("BIC:         " + BIC);
        System.out.println("Tara:        " + country);
        System.out.println("Sediu:       " + headOfficeAddress);
        System.out.println("Clienti:     " + getTotalCustomers());
        System.out.println("Conturi:     " + getTotalAccounts());
    }

    @Override
    public String toString() {
        return "Bank{" +
                "Nume='" + bankName + '\'' +
                ", BIC='" + BIC + '\'' +
                ", Tara='" + country + '\'' +
                ", Clienti=" + getTotalCustomers() +
                ", Conturi=" + getTotalAccounts() +
                '}';
    }
}