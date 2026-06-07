package bank.model;

/**
 * Customer – represents a bank customer (row in the customers table).
 */
public class Customer {

    private int    customerId;
    private String fullName;
    private String email;
    private String phone;
    private String address;

    // ── Constructors ─────────────────────────────────────────────
    public Customer() {}

    public Customer(String fullName, String email, String phone, String address) {
        this.fullName = fullName;
        this.email    = email;
        this.phone    = phone;
        this.address  = address;
    }

    // ── Getters & Setters ────────────────────────────────────────
    public int    getCustomerId()            { return customerId; }
    public void   setCustomerId(int id)      { this.customerId = id; }

    public String getFullName()              { return fullName; }
    public void   setFullName(String n)      { this.fullName = n; }

    public String getEmail()                 { return email; }
    public void   setEmail(String e)         { this.email = e; }

    public String getPhone()                 { return phone; }
    public void   setPhone(String p)         { this.phone = p; }

    public String getAddress()               { return address; }
    public void   setAddress(String a)       { this.address = a; }

    @Override
    public String toString() {
        return String.format("[%d] %s | %s | %s", customerId, fullName, email, phone);
    }
}
