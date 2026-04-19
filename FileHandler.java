import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles all CSV file I/O for the Inventory Tracker.
 *
 * CSV format (per row):
 *   id,name,quantity,price,category[,expiryDate]
 *
 * Example rows:
 *   1,Widget A,50,9.99,General
 *   2,Fresh Milk,30,2.49,Perishable,2025-08-01
 */
public class FileHandler {

    private final String filePath;

    // ── Constructor ───────────────────────────────────────────────────────────

    public FileHandler(String filePath) {
        this.filePath = filePath;
    }

    // ── Public API ────────────────────────────────────────────────────────────

    /**
     * Loads all items from the CSV file.
     * Returns an empty list if the file does not exist yet.
     */
    public List<Item> loadItems() {
        List<Item> items = new ArrayList<>();
        File file = new File(filePath);

        if (!file.exists()) {
            return items; // First run – no data yet
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            int lineNumber = 0;

            while ((line = reader.readLine()) != null) {
                lineNumber++;
                line = line.trim();

                // Skip header row or blank lines
                if (line.isEmpty() || line.startsWith("id,")) {
                    continue;
                }

                Item item = parseLine(line, lineNumber);
                if (item != null) {
                    items.add(item);
                }
            }
        } catch (IOException e) {
            System.err.println("[FileHandler] Error reading file: " + e.getMessage());
        }

        return items;
    }

    /**
     * Saves all items to the CSV file, overwriting any existing content.
     */
    public void saveItems(List<Item> items) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
            // Write header
            writer.println("id,name,quantity,price,category,extra");

            for (Item item : items) {
                writer.println(item.toCsv());
            }
        } catch (IOException e) {
            System.err.println("[FileHandler] Error writing file: " + e.getMessage());
        }
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    /**
     * Parses a single CSV line into an Item (or PerishableItem).
     * Returns null and logs a warning on malformed lines.
     */
    private Item parseLine(String line, int lineNumber) {
        String[] parts = line.split(",", -1);

        // Minimum required fields: id, name, quantity, price, category
        if (parts.length < 5) {
            System.err.printf("[FileHandler] Line %d skipped (too few fields): %s%n", lineNumber, line);
            return null;
        }

        try {
            int    id       = Integer.parseInt(parts[0].trim());
            String name     = parts[1].trim();
            int    quantity = Integer.parseInt(parts[2].trim());
            double price    = Double.parseDouble(parts[3].trim());
            String category = parts[4].trim();

            if ("Perishable".equalsIgnoreCase(category) && parts.length >= 6) {
                String expiry = parts[5].trim();
                return new PerishableItem(id, name, quantity, price, expiry);
            } else {
                return new Item(id, name, quantity, price);
            }

        } catch (NumberFormatException e) {
            System.err.printf("[FileHandler] Line %d skipped (bad number): %s%n", lineNumber, line);
            return null;
        }
    }
}
