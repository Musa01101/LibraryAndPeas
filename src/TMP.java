import com.library.models.Book;
import com.library.models.Librarian;
import com.library.models.Student;
import com.library.services.FileManager;
import com.library.services.LibrarySystem;

public class TMP {
    public static void main(String[] args) {
        System.out.println("=========================================");
        System.out.println("   STARTING CORE LIBRARY SYSTEM TEST    ");
        System.out.println("=========================================\n");

        // 1. Initialize System and File Manager
        LibrarySystem library = new LibrarySystem();
        FileManager fileManager = new FileManager();

        // 2. Create Sample Objects
        Book book1 = new Book("B01", "Java 101", "Smith", "Education", "123-456", 2, 2024);
        Book book2 = new Book("B02", "Data Structures", "Jones", "Computer Science", "789-012", 1, 2023);

        Student student = new Student("Musa Mammadov", "S101", "musa@adu.ac.ae", "password123", "Computer Engineering");
        Librarian librarian = new Librarian("Barquf", "L501", "barquf@adu.ac.ae", "securepass", "EMP999");

        // 3. Register Users and Load/Add Books
        library.registerUser(student);
        library.registerUser(librarian);

        // Pass the librarian as the second argument to track accountability!
        library.addBookToCatalog(book1, librarian);
        library.addBookToCatalog(book2, librarian);

        System.out.println("--- Setup Verification ---");
        System.out.println("Librarian managed books count: " + librarian.getManagedBooks().size());
        System.out.println("Catalog size: " + library.getCatalog().size() + " books registered.\n");

        // 4. Test Borrowing Logic & Core Limits
        System.out.println("--- Testing Borrowing & Caps ---");
        System.out.println("Attempting copy 1 of Java 101...");
        library.borrowBook("S101", "B01");

        System.out.println("Attempting copy 2 of Java 101...");
        library.borrowBook("S101", "B01");

        System.out.println("\n--- Testing FIFO Waitlist Queue ---");
        System.out.println("Attempting copy 3 of Java 101 (Should join waitlist)...");
        library.borrowBook("S101", "B01");

        // 5. Test Study Room Booking Limits
        System.out.println("\n--- Testing Room Booking Caps ---");
        System.out.println("Booking Room 1: " + library.bookStudyRoom("S101", 1));
        System.out.println("Booking Room 2: " + library.bookStudyRoom("S101", 2));
        System.out.println("Booking Room 3: " + library.bookStudyRoom("S101", 3));
        System.out.println("Booking Room 4: " + library.bookStudyRoom("S101", 4));
        System.out.println("Booking Room 5: " + library.bookStudyRoom("S101", 5));
        System.out.println("Booking Room 6 (Should fail - cap hit): " + library.bookStudyRoom("S101", 6));

        // 6. Test Book Returns & Waitlist Processing
        System.out.println("\n--- Testing Returns & Queue Release ---");
        System.out.println("Returning Java 101 (Should automatically assign it to next person in queue)...");
        library.returnBook("S101", "B01");

        // 7. Test Persistence (File I/O Ledger)
        System.out.println("\n--- Testing File I/O Persistence ---");
        System.out.println("Saving data structures, user directories, and transaction ledgers...");

        // Simulating data save to text files
        fileManager.saveBooks(library.getCatalog());
        fileManager.saveUsers(library.getUsers());
        fileManager.saveTransactions(library.getTransactions());

        System.out.println("\n=========================================");
        System.out.println("     ALL BACKEND CHECKS COMPLETED        ");
        System.out.println("=========================================");
    }
}