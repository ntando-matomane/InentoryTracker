import java.util.List;
import java.util.Scanner;

/**
 * Entry point for the Inventory Tracker CLI application.
 * Provides a menu-driven interface for all CRUD, search, sort, and report operations.
 *
 * Run:
 *   javac src/*.java -d out
 *   java -cp out Main
 */
public class Main {

    // ── Constants ─────────────────────────────────────────────────────────────

    private static final String CSV_FILE  = "inventory.csv";
    private static final String SEPARATOR = "═".repeat(72);
    private static final String THIN_SEP  = "─".repeat(72);
    private static final String HEADER    = String.format(
            "%-6s %-25s %-10s %-12s %s", "ID", "Name", "Quantity", "Price", "Details");

    // ── State ─────────────────────────────────────────────────────────────────

    private static InventoryManager manager;
    private static Scanner          scanner;

    // ── Entry point ───────────────────────────────────────────────────────────

    public static void main(String[] args) {
        manager = new InventoryManager(CSV_FILE);
        scanner = new Scanner(System.in);

        printBanner();
        System.out.printf("  Data file  : %s%n", CSV_FILE);
        System.out.printf("  Loaded     : %d item(s)%n%n", manager.getTotalItemCount());

        boolean running = true;
        while (running) {
            printMainMenu();
            int choice = readInt("  Enter choice: ", 0, 9);

            switch (choice) {
                case 1 -> handleAddItem();
                case 2 -> handleAddPerishable();
                case 3 -> handleViewAll();
                case 4 -> handleViewItem();
                case 5 -> handleUpdateItem();
                case 6 -> handleDeleteItem();
                case 7 -> handleSearch();
                case 8 -> handleSort();
                case 9 -> handleReports();
                case 0 -> running = false;
            }
        }

        System.out.println("\n  Thank you for using Inventory Tracker. Goodbye!");
        scanner.close();
    }

    // ── Menu Handlers ─────────────────────────────────────────────────────────

    /** 1. Add a new general item. */
    private static void handleAddItem() {
        printSectionHeader("Add New Item");

        String name     = readString("  Name     : ");
        int    quantity = readInt("  Quantity : ", 0, Integer.MAX_VALUE);
        double price    = readDouble("  Price    : R ");

        try {
            Item added = manager.addItem(name, quantity, price);
            System.out.println("\n   Item added successfully:");
            printTableRow(added);
        } catch (IllegalArgumentException e) {
            printError(e.getMessage());
        }

        pause();
    }

    /** 8. Add a new perishable item. */
    private static void handleAddPerishable() {
        printSectionHeader("Add Perishable Item");

        String name       = readString("  Name        : ");
        int    quantity   = readInt("  Quantity    : ", 0, Integer.MAX_VALUE);
        double price      = readDouble("  Price       : R ");
        String expiryDate = readString("  Expiry Date (YYYY-MM-DD): ");

        try {
            Item added = manager.addPerishableItem(name, quantity, price, expiryDate);
            System.out.println("\n   Perishable item added:");
            printTableRow(added);
        } catch (IllegalArgumentException e) {
            printError(e.getMessage());
        }

        pause();
    }

    /** 2. View all items in a formatted table. */
    private static void handleViewAll() {
        printSectionHeader("All Inventory Items");
        List<Item> items = manager.getAllItems();

        if (items.isEmpty()) {
            System.out.println("  (No items in inventory.)");
        } else {
            printTable(items);
            System.out.printf("%n  Total items: %d  |  Total value: R%.2f%n",
                    manager.getTotalItemCount(), manager.getTotalInventoryValue());
        }

        pause();
    }

    /** 9. View a single item by ID. */
    private static void handleViewItem() {
        printSectionHeader("View Item by ID");
        int id = readInt("  Enter item ID: ", 1, Integer.MAX_VALUE);
        Item item = manager.findById(id);

        if (item == null) {
            printError("No item found with ID " + id);
        } else {
            printTableHeader();
            printTableRow(item);
        }

        pause();
    }

    /** 3. Update an existing item. */
    private static void handleUpdateItem() {
        printSectionHeader("Update Item");
        int id = readInt("  Enter ID to update: ", 1, Integer.MAX_VALUE);

        Item existing = manager.findById(id);
        if (existing == null) {
            printError("No item found with ID " + id);
            pause();
            return;
        }

        System.out.println("  Current: " + existing.getSummary());
        System.out.println("  (Press ENTER to keep current value)\n");

        System.out.print("  New name     [" + existing.getName() + "]: ");
        String name = scanner.nextLine().trim();

        System.out.print("  New quantity [" + existing.getQuantity() + "]: ");
        String qtyStr = scanner.nextLine().trim();

        System.out.print("  New price    [" + String.format("%.2f", existing.getPrice()) + "]: ");
        String priceStr = scanner.nextLine().trim();

        try {
            Integer newQty   = qtyStr.isEmpty()   ? null : Integer.parseInt(qtyStr);
            Double  newPrice = priceStr.isEmpty()  ? null : Double.parseDouble(priceStr);
            String  newName  = name.isEmpty()      ? null : name;

            Item updated = manager.updateItem(id, newName, newQty, newPrice);
            System.out.println("\n   Item updated:");
            printTableRow(updated);

        } catch (NumberFormatException e) {
            printError("Invalid number entered.");
        } catch (IllegalArgumentException e) {
            printError(e.getMessage());
        }

        pause();
    }

    /** 4. Delete an item by ID. */
    private static void handleDeleteItem() {
        printSectionHeader("Delete Item");
        int id = readInt("  Enter ID to delete: ", 1, Integer.MAX_VALUE);

        Item item = manager.findById(id);
        if (item == null) {
            printError("No item found with ID " + id);
            pause();
            return;
        }

        System.out.println("  You are about to delete: " + item.getSummary());
        System.out.print("  Confirm delete? (yes/no): ");
        String confirm = scanner.nextLine().trim().toLowerCase();

        if (confirm.equals("yes") || confirm.equals("y")) {
            manager.deleteItem(id);
            System.out.println("   Item deleted.");
        } else {
            System.out.println("  Delete cancelled.");
        }

        pause();
    }

    /** 5. Search by name or ID. */
    private static void handleSearch() {
        printSectionHeader("Search Inventory");
        System.out.println("  1. Search by Name");
        System.out.println("  2. Search by ID");
        int choice = readInt("  Choice: ", 1, 2);

        if (choice == 1) {
            String query = readString("  Enter name (or part of name): ");
            List<Item> results = manager.searchByName(query);

            if (results.isEmpty()) {
                System.out.println("  No items matched \"" + query + "\".");
            } else {
                System.out.printf("  Found %d result(s):%n%n", results.size());
                printTable(results);
            }
        } else {
            int id = readInt("  Enter ID: ", 1, Integer.MAX_VALUE);
            Item item = manager.findById(id);

            if (item == null) {
                System.out.println("  No item found with ID " + id + ".");
            } else {
                printTableHeader();
                printTableRow(item);
            }
        }

        pause();
    }

    /** 6. Sort items. */
    private static void handleSort() {
        printSectionHeader("Sort Items");
        System.out.println("  1. Sort by Price (lowest first)");
        System.out.println("  2. Sort by Quantity (lowest first)");
        System.out.println("  3. Sort by Name (A–Z)");
        int choice = readInt("  Choice: ", 1, 3);

        List<Item> sorted = switch (choice) {
            case 1  -> manager.sortedByPrice();
            case 2  -> manager.sortedByQuantity();
            default -> manager.sortedByName();
        };

        String label = (choice == 1) ? "Price" : (choice == 2) ? "Quantity" : "Name";
        System.out.println("  Sorted by " + label + ":\n");
        printTable(sorted);

        pause();
    }

    /** 7. Reports. */
    private static void handleReports() {
        printSectionHeader("Inventory Reports");
        System.out.println("  1. Low Stock Alert");
        System.out.println("  2. Inventory Value Summary");
        int choice = readInt("  Choice: ", 1, 2);

        if (choice == 1) {
            List<Item> lowStock = manager.getLowStockItems();
            System.out.printf("%n    Items with quantity ≤ %d:%n%n",
                    manager.getLowStockThreshold());

            if (lowStock.isEmpty()) {
                System.out.println("  All items are adequately stocked.");
            } else {
                printTable(lowStock);
            }
        } else {
            System.out.println();
            System.out.printf("  Total distinct items  : %d%n", manager.getTotalItemCount());
            System.out.printf("  Total inventory value : $%.2f%n", manager.getTotalInventoryValue());
        }

        pause();
    }

    // ── Display Helpers ───────────────────────────────────────────────────────

    private static void printBanner() {
        System.out.println(SEPARATOR);
        System.out.println("           INVENTORY TRACKER  v1.0  —  Java OOP Demo");
        System.out.println(SEPARATOR);
    }

    private static void printMainMenu() {
        System.out.println("\n" + THIN_SEP);
        System.out.println("  MAIN MENU");
        System.out.println(THIN_SEP);
        System.out.println("  1. Add Item (General)");
        System.out.println("  2. Add Item (Perishable)");
        System.out.println("  3. View All Items");
        System.out.println("  4. View Item by ID");
        System.out.println("  5. Update Item");
        System.out.println("  6. Delete Item");
        System.out.println("  7. Search Items");
        System.out.println("  8. Sort Items");
        System.out.println("  9. Reports");
        System.out.println("  0. Exit");
        System.out.println(THIN_SEP);
    }

    private static void printSectionHeader(String title) {
        System.out.println("\n" + SEPARATOR);
        System.out.println("  " + title.toUpperCase());
        System.out.println(SEPARATOR);
    }

    private static void printTableHeader() {
        System.out.println("  " + HEADER);
        System.out.println("  " + THIN_SEP);
    }

    private static void printTableRow(Item item) {
        System.out.println("  " + item.getSummary());
    }

    private static void printTable(List<Item> items) {
        printTableHeader();
        items.forEach(Main::printTableRow);
        System.out.println("  " + THIN_SEP);
    }

    private static void printError(String msg) {
        System.out.println("\n   Error: " + msg);
    }

    private static void pause() {
        System.out.print("\n  Press ENTER to continue...");
        scanner.nextLine();
    }

    // ── Input Helpers ─────────────────────────────────────────────────────────

    /** Reads a non-blank string from the user. */
    private static String readString(String prompt) {
        String value;
        do {
            System.out.print(prompt);
            value = scanner.nextLine().trim();
            if (value.isBlank()) System.out.println("  Input cannot be empty. Try again.");
        } while (value.isBlank());
        return value;
    }

    /** Reads an integer within [min, max], re-prompting on bad input. */
    private static int readInt(String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine().trim();
            try {
                int value = Integer.parseInt(line);
                if (value < min || value > max) {
                    System.out.printf("  Please enter a number between %d and %d.%n", min, max);
                } else {
                    return value;
                }
            } catch (NumberFormatException e) {
                System.out.println("  Invalid input. Please enter a whole number.");
            }
        }
    }

    /** Reads a non-negative double, re-prompting on bad input. */
    private static double readDouble(String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine().trim();
            try {
                double value = Double.parseDouble(line);
                if (value < 0) {
                    System.out.println("  Value cannot be negative.");
                } else {
                    return value;
                }
            } catch (NumberFormatException e) {
                System.out.println("  Invalid input. Please enter a decimal number (e.g. 9.99).");
            }
        }
    }
}
