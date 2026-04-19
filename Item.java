/**
 * Base class representing a generic inventory item.
 * Demonstrates Encapsulation via private fields + getters/setters.
 */
public class Item {

    private int id;
    private String name;
    private int quantity;
    private double price;

    // ── Constructors ──────────────────────────────────────────────────────────

    public Item(int id, String name, int quantity, double price) {
        this.id = id;
        this.name = name;
        this.quantity = quantity;
        this.price = price;
    }

    // ── Getters ───────────────────────────────────────────────────────────────

    public int getId()       { return id; }
    public String getName()  { return name; }
    public int getQuantity() { return quantity; }
    public double getPrice() { return price; }

    // ── Setters ───────────────────────────────────────────────────────────────

    public void setId(int id)           { this.id = id; }
    public void setName(String name)    { this.name = name; }
    public void setQuantity(int qty)    { this.quantity = qty; }
    public void setPrice(double price)  { this.price = price; }

    // ── Polymorphism: overridden in subclasses ────────────────────────────────

    /**
     * Returns a human-readable summary of the item.
     * Overridden by subclasses to append category-specific details.
     */
    public String getSummary() {
        return String.format("%-6d %-25s %-10d R%-10.2f", id, name, quantity, price);
    }

    /**
     * Returns a CSV representation of the item.
     * Overridden by subclasses that carry extra fields.
     */
    public String toCsv() {
        return String.format("%d,%s,%d,%.2f,General", id, name, quantity, price);
    }

    @Override
    public String toString() {
        return getSummary();
    }
}
