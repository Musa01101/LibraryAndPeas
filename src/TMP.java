import com.library.models.Book;
import com.library.models.Student;
import com.library.models.Librarian;
import com.library.services.LibrarySystem;

public class TMP {
    public static void main(String[] args) {
        System.out.println("=== BOOTING MY LIBRARY  ===");

        try {
            // 1. Turn the system on.
            // This will automatically fire your FileManager to look for old data!
            LibrarySystem library = new LibrarySystem();

            // 2. Create some brand new test data
            System.out.println("\n Creating Test Data ");
            Book testBook = new Book("B01", "Java 101", "Smith", "Education", "123-456", 3, 2024);
            Student testStudent = new Student("Computer Science", "Musa", "S01", "musa@uni.edu", "pass123");
            Librarian testStaff = new Librarian("Baptist", "L01", "baptist@uni.edu", "admin123", "STAFF-01");
            // Create TWO Librarians
            Librarian staff1 = new Librarian("Barquf", "L01", "barquf@uni.edu", "admin1", "STAFF-01");
            Librarian staff2 = new Librarian("Aisha", "L02", "aisha@uni.edu", "admin2", "STAFF-02");

            staff1.getManagedBooks().add(testBook);

            // 3. Register the data into the system's arrays
            library.addBookToCatalog(testBook);
            library.registerStudent(testStudent);
            library.registerStaff(testStaff);
            library.registerStaff(staff1);
            library.registerStaff(staff2);
            System.out.println("Data successfully registered in memory.");

            // 4. Test the Logic Engine
            System.out.println("\n Running Transactions ");
            library.borrowBook("S01", "B01"); // Musa borrows Java 101!

            // 5. The Magic Moment: Shut down and save everything to the hard drive
            System.out.println("\n SHUTTING DOWN SYSTEM ");
            library.saveSystemData();

        } catch (Exception e) {
            System.out.println("\n CRITICAL CRASH DETECTED:");
            e.printStackTrace();
        }
    }
}