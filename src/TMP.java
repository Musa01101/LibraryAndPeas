import com.library.models.Book;
import com.library.models.Librarian;
import com.library.models.Student;
import com.library.models.User;
import com.library.services.LibrarySystem;

public class TMP {
    public static void main(String[] args) {
        System.out.println("=========================================");
        System.out.println("   STARTING CORE LIBRARY SYSTEM TEST    ");
        System.out.println("=========================================\n");

        // 1. Initialize System (Automatically triggers text file loading)
        LibrarySystem library = new LibrarySystem();

        // 2. Create Sample Objects
        Book book1 = new Book("B01", "Java 101", "Smith", "Education", "123-456", 1, 2024);
        Book book2 = new Book("B02", "Data Structures", "Jones", "Computer Science", "789-012", 0, 2023);
        Book book3 = new Book("B03", "Algorthihmmsmssm", "Bones", "Computer Science", "7895-012", 1, 2023);

        Student student1 = new Student("Computer Engineering", "Musa Mammadov", "S01", "musa@uni.edu", "pass123");
        Student student2 = new Student("Electrical Engineering", "Ali", "S02", "ali@uni.edu", "pass456");
        Librarian librarian = new Librarian("Barquf", "L01", "barquf@uni.edu", "admin123", "STAFF-01");
        Librarian librarian2 = new Librarian("Alof", "L090", "alof@uni.edu", "admin103", "STAFF-02");
        // 3. Register Users & Test Inventory Management
        library.registerStudent(student1);
        library.registerStudent(student2);
        library.registerStaff(librarian);

        // Add books to catalog passing the librarian for tracking accountability!
        library.addBookToCatalog(book1, librarian);
        library.addBookToCatalog(book2, librarian);
        library.addBookToCatalog(book3, librarian2);

        System.out.println("--- Setup Verification ---");
        System.out.println("Librarian managed books count: " + librarian.getManagedBooks().size());
        System.out.println("Librarianw managed books count: " + librarian2.getManagedBooks().size());
        System.out.println("Catalog size: " + library.getCatalog().size() + " books registered.\n");

        // 4. Test Polymorphic User Lookup Engine
        System.out.println("--- Testing Polymorphic Search ---");
        try {
            User found = library.findUserById("S01");
            System.out.println("Success: Found user account associated with name: " + found.getName());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        // 5. Test Borrowing Rules & Limits
        System.out.println("\n--- Testing Borrowing Logic ---");
        try {
            library.borrowBook("S01", "B01"); // Musa borrows Java 101 (copies go from 1 to 0)
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        // 6. Test Book Reservation Queues (FIFO Waitlist)
        System.out.println("\n--- Testing Reservation Queue ---");
        try {
            // Since book1 copies hit 0, Ali shouldn't be able to borrow it
            library.borrowBook("S02", "B01");
        } catch (Exception e) {
            System.out.println("Caught Expected Error: " + e.getMessage());
        }

        try {
            // Ali reserves the out-of-stock book instead
            library.reserveBook("S02", "B01");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        // 7. Test Study Room Booking Bounds (1-5)
        System.out.println("\n--- Testing Study Room Bookings ---");
        try {
            library.bookStudyRoom("S01", 3);  // Valid room
            library.bookStudyRoom("S02", 3);  // Should fail (occupied)
        } catch (Exception e) {
            System.out.println("Caught Expected Error: " + e.getMessage());
        }

        // 8. Test Book Returns & Auto-Transfer triggering
        System.out.println("\n--- Testing Returns & Queue Release ---");
        try {
            // Musa returns the book; it should auto-assign to Ali who is waiting on the FIFO queue!
            library.returnBook("S01", "B01");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        System.out.println("ALi's books"+student2.getBorrowedBooks());
        // 9. Shut down and dump the state data into text ledgers
        System.out.println("\n--- SHUTTING DOWN SYSTEM ---");
        library.saveSystemData();
        System.out.println("\n=========================================");
        System.out.println("     ALL BACKEND CHECKS COMPLETED        ");
        System.out.println("=========================================");
    }
}