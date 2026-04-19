import java.util.*;
import java.util.stream.Collectors;

/**
 * Core business-logic layer for the Inventory Tracker.
 * Manages the in-memory list of items and delegates persistence to FileHandler.
 */
public class InventoryManager {

    // ── Constants ─────────────────────────────────────────────────────────────

    private static final int LOW_STOCK_THRESHOLD = 5;

    // ── State ─────────────────────────────────────────────────────────────────

    private List<Item> items;
    private final FileHandler fileHandler;
    private int nextId; // Auto-incrementing ID counter

    // ── Constructor ───────────────────────────────────────────────────────────

    public InventoryManager(String csvFilePath) {
        this.fileHandler = new FileHandler(csvFilePath);
        this.items       = fileHandler.loadItems();
        // Set nextId to max existing id + 1 (or 1 if empty)
        this.nextId = items.stream()
                           .mapToInt(Item::getId)
                           .max()
                           .orElse(0) + 1;
    }

    // ── CRUD Operations ───────────────────────────────────────────────────────

    /**
     * Adds a new General item and persists the list.
     */
    public Item addItem(String name, int quantity, double price) {
        validateName(name);
        validateQuantity(quantity);
        validatePrice(price);

        Item item = new Item(nextId++, name, quantity, price);
        items.add(item);
        persist();
        return item;
    }

    /**
     * Adds a new Perishable item and persists the list.
     */
    public Item addPerishableItem(String name, int quantity, double price, String expiryDate) {
        validateName(name);
        validateQuantity(quantity);
        validatePrice(price);
        validateExpiryDate(expiryDate);

        PerishableItem item = new PerishableItem(nextId++, name, quantity, price, expiryDate);
        items.add(item);
        persist();
        return item;
    }

    /**
     * Returns an unmodifiable view of all items.
     */
    public List<Item> getAllItems() {
        return Collections.unmodifiableList(items);
    }

    /**
     * Updates an existing item by ID. Pass null/negative to skip a field.
     * Returns the updated item, or null if not found.
     */
    public Item updateItem(int id, String name, Integer quantity, Double price) {
        Item item = findById(id);
        if (item == null) return null;

        if (name != null && !name.isBlank()) {
            validateName(name);
            item.setName(name.trim());
        }
        if (quantity != null) {
            validateQuantity(quantity);
            item.setQuantity(quantity);
        }
        if (price != null) {
            validatePrice(price);
            item.setPrice(price);
        }

        persist();
        return item;
    }

    /**
     * Deletes an item by ID.
     * Returns true on success, false if ID not found.
     */
    public boolean deleteItem(int id) {
        Item item = findById(id);
        if (item == null) return false;

        items.remove(item);
        persist();
        return true;
    }

    // ── Search ────────────────────────────────────────────────────────────────

    /** Finds a single item by its numeric ID. */
    public Item findById(int id) {
        return items.stream()
                    .filter(i -> i.getId() == id)
                    .findFirst()
                    .orElse(null);
    }

    /** Case-insensitive search by name fragment. */
    public List<Item> searchByName(String query) {
        if (query == null || query.isBlank()) return Collections.emptyList();
        String lower = query.toLowerCase();
        return items.stream()
                    .filter(i -> i.getName().toLowerCase().contains(lower))
                    .collect(Collectors.toList());
    }

    // ── Sorting ───────────────────────────────────────────────────────────────

    /** Returns a new list sorted by price (ascending). */
    public List<Item> sortedByPrice() {
        return items.stream()
                    .sorted(Comparator.comparingDouble(Item::getPrice))
                    .collect(Collectors.toList());
    }

    /** Returns a new list sorted by quantity (ascending). */
    public List<Item> sortedByQuantity() {
        return items.stream()
                    .sorted(Comparator.comparingInt(Item::getQuantity))
                    .collect(Collectors.toList());
    }

    /** Returns a new list sorted by name (A-Z). */
    public List<Item> sortedByName() {
        return items.stream()
                    .sorted(Comparator.comparing(Item::getName, String.CASE_INSENSITIVE_ORDER))
                    .collect(Collectors.toList());
    }

    // ── Reporting ─────────────────────────────────────────────────────────────

    /** Returns items whose quantity is at or below the low-stock threshold. */
    public List<Item> getLowStockItems() {
        return items.stream()
                    .filter(i -> i.getQuantity() <= LOW_STOCK_THRESHOLD)
                    .collect(Collectors.toList());
    }

    /** Calculates total inventory value (sum of quantity * price). */
    public double getTotalInventoryValue() {
        return items.stream()
                    .mapToDouble(i -> i.getQuantity() * i.getPrice())
                    .sum();
    }

    /** Returns total number of items in inventory. */
    public int getTotalItemCount() {
        return items.size();
    }

    public int getLowStockThreshold() {
        return LOW_STOCK_THRESHOLD;
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    /** Writes current state to CSV. */
    private void persist() {
        fileHandler.saveItems(items);
    }

    // ── Validation helpers ────────────────────────────────────────────────────

    private void validateName(String name) {
        if (name == null || name.isBlank())
            throw new IllegalArgumentException("Item name cannot be empty.");
        if (name.contains(","))
            throw new IllegalArgumentException("Item name must not contain commas.");
    }

    private void validateQuantity(int qty) {
        if (qty < 0)
            throw new IllegalArgumentException("Quantity cannot be negative.");
    }

    private void validatePrice(double price) {
        if (price < 0)
            throw new IllegalArgumentException("Price cannot be negative.");
    }

    private void validateExpiryDate(String date) {
        if (date == null || !date.matches("\\d{4}-\\d{2}-\\d{2}"))
            throw new IllegalArgumentException("Expiry date must be in YYYY-MM-DD format.");
    }
}
