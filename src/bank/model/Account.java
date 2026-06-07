package bank.model;

/**
 * Account – represents a bank account (row in the accounts table).
 */
public class Account {

    // Account types must match the MySQL ENUM values
    public enum AccountType { SAVINGS, CURRENT, FIXED_DEPOSIT }
    public enum AccountStatus { ACTIVE, FROZEN, CLOSED }

    private int           accountId;
    private String        accountNumber;
    private int           customerId;
    private String        customerName;    // joined from customers table (display only)
    private AccountType   accountType;
    private double        balance;
    private AccountStatus status;

    // ── Constructors ─────────────────────────────────────────────
    public Account() {}

    public Account(String accountNumber, int customerId, AccountType type, double balance) {
        this.accountNumber = accountNumber;
        this.customerId    = customerId;
        this.accountType   = type;
        this.balance       = balance;
        this.status        = AccountStatus.ACTIVE;
    }

    // ── Getters & Setters ────────────────────────────────────────
    public int           getAccountId()                   { return accountId; }
    public void          setAccountId(int id)             { this.accountId = id; }

    public String        getAccountNumber()               { return accountNumber; }
    public void          setAccountNumber(String n)       { this.accountNumber = n; }

    public int           getCustomerId()                  { return customerId; }
    public void          setCustomerId(int id)            { this.customerId = id; }

    public String        getCustomerName()                { return customerName; }
    public void          setCustomerName(String n)        { this.customerName = n; }

    public AccountType   getAccountType()                 { return accountType; }
    public void          setAccountType(AccountType t)    { this.accountType = t; }

    public double        getBalance()                     { return balance; }
    public void          setBalance(double b)             { this.balance = b; }

    public AccountStatus getStatus()                      { return status; }
    public void          setStatus(AccountStatus s)       { this.status = s; }

    @Override
    public String toString() {
        return String.format("%s | %s | %-14s | ₹%,.2f | %s",
                accountNumber, customerName, accountType, balance, status);
    }
}
