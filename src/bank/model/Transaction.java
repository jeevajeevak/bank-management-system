package bank.model;

import java.time.LocalDateTime;

/**
 * Transaction – one debit or credit event on an account.
 */
public class Transaction {

    public enum TransactionType { DEPOSIT, WITHDRAWAL, TRANSFER_IN, TRANSFER_OUT }

    private int             transactionId;
    private int             accountId;
    private TransactionType type;
    private double          amount;
    private String          description;
    private LocalDateTime   transactionDate;

    // ── Constructors ─────────────────────────────────────────────
    public Transaction() {}

    public Transaction(int accountId, TransactionType type, double amount, String description) {
        this.accountId   = accountId;
        this.type        = type;
        this.amount      = amount;
        this.description = description;
    }

    // ── Getters & Setters ────────────────────────────────────────
    public int             getTransactionId()              { return transactionId; }
    public void            setTransactionId(int id)        { this.transactionId = id; }

    public int             getAccountId()                  { return accountId; }
    public void            setAccountId(int id)            { this.accountId = id; }

    public TransactionType getType()                       { return type; }
    public void            setType(TransactionType t)      { this.type = t; }

    public double          getAmount()                     { return amount; }
    public void            setAmount(double a)             { this.amount = a; }

    public String          getDescription()                { return description; }
    public void            setDescription(String d)        { this.description = d; }

    public LocalDateTime   getTransactionDate()            { return transactionDate; }
    public void            setTransactionDate(LocalDateTime d) { this.transactionDate = d; }

    @Override
    public String toString() {
        return String.format("[%d] %-15s ₹%,10.2f  %s  %s",
                transactionId, type, amount, description, transactionDate);
    }
}
