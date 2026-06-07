package bank.service;

import bank.dao.*;
import bank.model.*;
import bank.model.Account.AccountStatus;
import bank.model.Loan.LoanStatus;
import bank.model.Transaction.TransactionType;

import java.util.List;

/**
 * BankService – the Business Logic Layer.
 *
 * The UI calls methods here. This class enforces all rules (e.g. "you cannot
 * withdraw more than your balance") before delegating to the DAOs.
 *
 * PATTERN: UI → Service → DAO → Database
 */
public class BankService {

    // One DAO instance per entity type
    private final CustomerDAO    customerDAO    = new CustomerDAO();
    private final AccountDAO     accountDAO     = new AccountDAO();
    private final TransactionDAO transactionDAO = new TransactionDAO();
    private final LoanDAO        loanDAO        = new LoanDAO();

    // ════════════════════════════════════════════════════════════
    //  CUSTOMER OPERATIONS
    // ════════════════════════════════════════════════════════════

    public boolean addCustomer(Customer c)              { return customerDAO.addCustomer(c); }
    public List<Customer> getAllCustomers()              { return customerDAO.getAllCustomers(); }
    public Customer getCustomerById(int id)             { return customerDAO.getCustomerById(id); }
    public List<Customer> searchCustomers(String kw)    { return customerDAO.searchCustomers(kw); }
    public boolean updateCustomer(Customer c)           { return customerDAO.updateCustomer(c); }
    public boolean deleteCustomer(int id)               { return customerDAO.deleteCustomer(id); }

    // ════════════════════════════════════════════════════════════
    //  ACCOUNT OPERATIONS
    // ════════════════════════════════════════════════════════════

    /** Open a new account. Auto-generates account number. */
    public boolean openAccount(Account a) {
        a.setAccountNumber(accountDAO.generateAccountNumber());
        return accountDAO.addAccount(a);
    }

    public List<Account> getAllAccounts()                       { return accountDAO.getAllAccounts(); }
    public Account getAccountById(int id)                      { return accountDAO.getAccountById(id); }
    public Account getAccountByNumber(String num)              { return accountDAO.getAccountByNumber(num); }
    public List<Account> getAccountsByCustomer(int custId)     { return accountDAO.getAccountsByCustomer(custId); }
    public boolean freezeAccount(int id)                       { return accountDAO.updateStatus(id, AccountStatus.FROZEN); }
    public boolean activateAccount(int id)                     { return accountDAO.updateStatus(id, AccountStatus.ACTIVE); }
    public boolean closeAccount(int id)                        { return accountDAO.updateStatus(id, AccountStatus.CLOSED); }
    public boolean deleteAccount(int id)                       { return accountDAO.deleteAccount(id); }

    // ════════════════════════════════════════════════════════════
    //  TRANSACTION OPERATIONS
    // ════════════════════════════════════════════════════════════

    /**
     * Deposit money into an account.
     * @return error message string, or null on success.
     */
    public String deposit(int accountId, double amount, String description) {
        if (amount <= 0) return "Amount must be greater than zero.";

        Account acc = accountDAO.getAccountById(accountId);
        if (acc == null) return "Account not found.";
        if (acc.getStatus() != AccountStatus.ACTIVE) return "Account is not active.";

        double newBalance = acc.getBalance() + amount;
        accountDAO.updateBalance(accountId, newBalance);

        Transaction tx = new Transaction(accountId, TransactionType.DEPOSIT, amount,
                description.isEmpty() ? "Cash Deposit" : description);
        transactionDAO.addTransaction(tx);
        return null;  // null = success
    }

    /**
     * Withdraw money from an account.
     * @return error message string, or null on success.
     */
    public String withdraw(int accountId, double amount, String description) {
        if (amount <= 0) return "Amount must be greater than zero.";

        Account acc = accountDAO.getAccountById(accountId);
        if (acc == null) return "Account not found.";
        if (acc.getStatus() != AccountStatus.ACTIVE) return "Account is not active.";
        if (acc.getBalance() < amount) return "Insufficient balance. Available: ₹" + acc.getBalance();

        accountDAO.updateBalance(accountId, acc.getBalance() - amount);

        Transaction tx = new Transaction(accountId, TransactionType.WITHDRAWAL, amount,
                description.isEmpty() ? "Cash Withdrawal" : description);
        transactionDAO.addTransaction(tx);
        return null;
    }

    /**
     * Transfer money between two accounts.
     * @return error message string, or null on success.
     */
    public String transfer(int fromAccountId, String toAccountNumber, double amount) {
        if (amount <= 0) return "Amount must be greater than zero.";

        Account from = accountDAO.getAccountById(fromAccountId);
        Account to   = accountDAO.getAccountByNumber(toAccountNumber);

        if (from == null) return "Source account not found.";
        if (to == null)   return "Destination account not found.";
        if (from.getAccountId() == to.getAccountId()) return "Cannot transfer to the same account.";
        if (from.getStatus() != AccountStatus.ACTIVE) return "Source account is not active.";
        if (to.getStatus()   != AccountStatus.ACTIVE) return "Destination account is not active.";
        if (from.getBalance() < amount) return "Insufficient balance.";

        accountDAO.updateBalance(from.getAccountId(), from.getBalance() - amount);
        accountDAO.updateBalance(to.getAccountId(),   to.getBalance()   + amount);

        transactionDAO.addTransaction(new Transaction(from.getAccountId(),
                TransactionType.TRANSFER_OUT, amount, "Transfer to " + toAccountNumber));
        transactionDAO.addTransaction(new Transaction(to.getAccountId(),
                TransactionType.TRANSFER_IN, amount, "Transfer from " + from.getAccountNumber()));
        return null;
    }

    public List<Transaction> getTransactionHistory(int accountId) {
        return transactionDAO.getTransactionsByAccount(accountId);
    }

    public List<Transaction> getRecentTransactions(int limit) {
        return transactionDAO.getRecentTransactions(limit);
    }

    // ════════════════════════════════════════════════════════════
    //  LOAN OPERATIONS
    // ════════════════════════════════════════════════════════════

    public boolean applyForLoan(Loan l)                        { return loanDAO.addLoan(l); }
    public List<Loan> getAllLoans()                            { return loanDAO.getAllLoans(); }
    public List<Loan> getLoansByCustomer(int custId)           { return loanDAO.getLoansByCustomer(custId); }
    public Loan getLoanById(int id)                            { return loanDAO.getLoanById(id); }
    public boolean approveLoan(int loanId)                     { return loanDAO.updateStatus(loanId, LoanStatus.ACTIVE); }
    public boolean rejectLoan(int loanId)                      { return loanDAO.updateStatus(loanId, LoanStatus.REJECTED); }

    /** Pay one EMI instalment. */
    public String payEMI(int loanId) {
        Loan loan = loanDAO.getLoanById(loanId);
        if (loan == null) return "Loan not found.";
        if (loan.getStatus() != LoanStatus.ACTIVE) return "Loan is not active.";
        if (loan.getOutstandingAmount() <= 0) return "Loan already fully paid.";

        loanDAO.makePayment(loanId, loan.getEmi());

        // Auto-close if fully paid
        double remaining = loan.getOutstandingAmount() - loan.getEmi();
        if (remaining <= 0) loanDAO.updateStatus(loanId, LoanStatus.CLOSED);

        return null;
    }
}
