/**
 * Subclass of Item representing perishable goods.
 * Demonstrates Inheritance and Polymorphism (overrides getSummary & toCsv).
 */
public class PerishableItem extends Item {

    private String expiryDate; // Format: YYYY-MM-DD

    // ── Constructor ───────────────────────────────────────────────────────────

    public PerishableItem(int id, String name, int quantity, double price, String expiryDate) {
        super(id, name, quantity, price);
        this.expiryDate = expiryDate;
    }

    // ── Getter / Setter ───────────────────────────────────────────────────────

    public String getExpiryDate()              { return expiryDate; }
    public void setExpiryDate(String expiry)   { this.expiryDate = expiry; }

    // ── Polymorphic overrides ─────────────────────────────────────────────────

    @Override
    public String getSummary() {
        return super.getSummary() + String.format(" [Perishable | Exp: %s]", expiryDate);
    }

    @Override
    public String toCsv() {
        return String.format("%d,%s,%d,%.2f,Perishable,%s",
                getId(), getName(), getQuantity(), getPrice(), expiryDate);
    }
}
