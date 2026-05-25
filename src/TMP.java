import com.library.services.LibrarySystem;

public class TMP {
    public static void main(String[] args) {
        System.out.println("=== 🚀 BOOTING MY LIBRARY ===");

        try {
            // 1. Turn the system on (Loads everything from the text files!)
            LibrarySystem library = new LibrarySystem();

            // Notice we DELETED the "Creating Test Data" section!
            // The system already knows who Musa is now.

            System.out.println("\n--- Running Transactions ---");
            // 2. Let's test the return logic using the data we loaded!
            library.borrowBook("S01", "B01");

            System.out.println("\n--- SHUTTING DOWN SYSTEM ---");
            // 3. Save the new state
            library.saveSystemData();

        } catch (Exception e) {
            System.out.println("\n❌ CRITICAL CRASH DETECTED:");
            e.printStackTrace();
        }
    }
}