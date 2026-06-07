package bank.model;

import java.time.LocalDate;

/**
 * Loan – represents a loan taken by a customer.
 * EMI formula: EMI = P * r * (1+r)^n / ((1+r)^n - 1)
 *   where P = principal, r = monthly interest rate, n = duration in months
 */
public class Loan {

    public enum LoanType   { HOME, CAR, PERSONAL, EDUCATION }
    public enum LoanStatus { PENDING, ACTIVE, CLOSED, REJECTED }

    private int        loanId;
    private int        customerId;
    private String     customerName;   // display only (joined)
    private LoanType   loanType;
    private double     principal;
    private double     interestRate;   // annual %
    private int        durationMonths;
    private double     emi;            // calculated monthly payment
    private double     amountPaid;
    private LoanStatus status;
    private LocalDate  startDate;

    // ── Constructors ─────────────────────────────────────────────
    public Loan() {}

    public Loan(int customerId, LoanType type, double principal,
                double interestRate, int durationMonths) {
        this.customerId      = customerId;
        this.loanType        = type;
        this.principal       = principal;
        this.interestRate    = interestRate;
        this.durationMonths  = durationMonths;
        this.emi             = calculateEMI(principal, interestRate, durationMonths);
        this.status          = LoanStatus.PENDING;
        this.amountPaid      = 0;
    }

    /** Standard reducing-balance EMI calculation. */
    public static double calculateEMI(double principal, double annualRate, int months) {
        if (annualRate == 0) return principal / months;
        double r = annualRate / 12.0 / 100.0;   // monthly rate
        double factor = Math.pow(1 + r, months);
        return (principal * r * factor) / (factor - 1);
    }

    // ── Getters & Setters ────────────────────────────────────────
    public int        getLoanId()                    { return loanId; }
    public void       setLoanId(int id)              { this.loanId = id; }

    public int        getCustomerId()                { return customerId; }
    public void       setCustomerId(int id)          { this.customerId = id; }

    public String     getCustomerName()              { return customerName; }
    public void       setCustomerName(String n)      { this.customerName = n; }

    public LoanType   getLoanType()                  { return loanType; }
    public void       setLoanType(LoanType t)        { this.loanType = t; }

    public double     getPrincipal()                 { return principal; }
    public void       setPrincipal(double p)         { this.principal = p; }

    public double     getInterestRate()              { return interestRate; }
    public void       setInterestRate(double r)      { this.interestRate = r; }

    public int        getDurationMonths()            { return durationMonths; }
    public void       setDurationMonths(int d)       { this.durationMonths = d; }

    public double     getEmi()                       { return emi; }
    public void       setEmi(double e)               { this.emi = e; }

    public double     getAmountPaid()                { return amountPaid; }
    public void       setAmountPaid(double a)        { this.amountPaid = a; }

    public LoanStatus getStatus()                    { return status; }
    public void       setStatus(LoanStatus s)        { this.status = s; }

    public LocalDate  getStartDate()                 { return startDate; }
    public void       setStartDate(LocalDate d)      { this.startDate = d; }

    public double     getOutstandingAmount()         { return (emi * durationMonths) - amountPaid; }

    @Override
    public String toString() {
        return String.format("[%d] %s | %s | ₹%,.2f | EMI ₹%,.2f | %s",
                loanId, customerName, loanType, principal, emi, status);
    }
}
