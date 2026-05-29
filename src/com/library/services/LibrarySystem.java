package com.library.services;

import com.library.models.Book;
import com.library.models.Librarian;
import com.library.models.Student;
import com.library.models.User;
import com.library.models.StudyRoom;

import java.util.ArrayList;

public class LibrarySystem {
    private ArrayList<Book> catalog;
    private ArrayList<Student> registeredStudents;
    private ArrayList<Librarian> registeredStaff;
    private ArrayList<StudyRoom> rooms = new ArrayList<>();

    private FileManager fileManager;

    public LibrarySystem() {
        this.catalog = new ArrayList<>();
        this.registeredStudents = new ArrayList<>();
        this.registeredStaff = new ArrayList<>();
        this.fileManager = new FileManager();
        if (rooms.isEmpty()) {
            for (int i = 1; i <= 5; i++) {
                rooms.add(new StudyRoom(i));
            }
        }
        try {
            // 1. Load Books
            this.catalog = fileManager.loadBooks();

            // 2. Load People (Look! loadStudents is empty now!)
            this.registeredStudents = fileManager.loadStudents();
            this.registeredStaff = fileManager.loadLibrarians(this.catalog);

            // 3. Load Transactions to link the books to the students!
            fileManager.loadTransactions(this.registeredStudents, this.catalog);

            System.out.println("System Boot: All library data loaded successfully.");
        } catch (Exception e) {
            System.out.println("System Boot Warning: Could not load existing data. Starting fresh.");
            System.out.println(e.getMessage());

            // Failsafe
            this.catalog = new ArrayList<>();
            this.registeredStudents = new ArrayList<>();
            this.registeredStaff = new ArrayList<>();
        }
    }

    //getter methods for our "data base"
    public ArrayList<Book> getCatalog() {
        return this.catalog;
    }
    public ArrayList<Student> getRegisteredStudents() {
        return this.registeredStudents;
    }
    public ArrayList<Librarian> getRegisteredStaff() {
        return this.registeredStaff;
    }
    public ArrayList<StudyRoom> getRooms() {
        return rooms;
    }

    //  System Shutdown (Save all data to files)
    public void saveSystemData() {
        try {
            System.out.println("Initiating system shutdown save...");
            fileManager.saveBooks(this.catalog);
            fileManager.saveStudents(this.registeredStudents);
            fileManager.saveLibrarians(this.registeredStaff);
            fileManager.saveTransactions(this.registeredStudents);
            System.out.println("Shutdown Complete: All data safely saved.");
        } catch (Exception e) {
            System.out.println("ERROR: Failed to save system data!");
            System.out.println(e.getMessage());
        }
    }

    //  Management Methods
    public void addBookToCatalog(Book book, Librarian librarian) {
        if (book != null && !catalog.contains(book)) {
            catalog.add(book);

            if(librarian != null) {
                librarian.addManagedBook(book);
            }
        }
    }

    public void registerStudent(Student student) {
        if (student != null && !registeredStudents.contains(student)) {
            registeredStudents.add(student);
        }
    }

    public void registerStaff(Librarian librarian) {
        if (librarian != null && !registeredStaff.contains(librarian)) {
            registeredStaff.add(librarian);
        }
    }

    //  The Borrowing of Books
    public void borrowBook(String studentId, String bookId) throws Exception {
        //Step 1 and 2: Search for bookId and StucdentId in arrays using methods
        Student foundStudent = findStudentById(studentId);
        Book foundBook = findBookById(bookId);
        // Step 3: Check if book.getAvailableCopies() > 0
        if (foundBook.getAvailableCopies() <= 0) {
            // TODO: Replace with Suhail's BookUnavailableException
            throw new Exception("Error: No available copies left for this book.");
        }
        //  NEW: Check the Borrowing Cap
        if (foundStudent.getBorrowedBooks().size() >= 5) {
            throw new Exception("Error: Student has reached the maximum borrowing limit of 5 books.");
        }
        // Step 4: If yes, subtract 1 copy and add the book to the student's borrowed list
        foundBook.setAvailableCopies(foundBook.getAvailableCopies() - 1);
        foundStudent.borrowBook(foundBook);
        System.out.println("Transaction Successful: " + foundBook.getTitle() + " borrowed by " + foundStudent.getName());
        // Step 5: If no, throw  BookUnavailableException, when suhali gives the package

    }

    // The Return of Books
    public void returnBook(String studentId, String bookId) throws Exception {
        //Step 1 and 2: Search for bookId and StucdentId in arrays using methods
        Student foundStudentR = findStudentById(studentId);
        Book foundBookR = findBookById(bookId);
        //Step 3: Does the student own the book?
        if (!foundStudentR.getBorrowedBooks().contains(foundBookR)) {
            throw new Exception("Error: Book is not borrowed in the system.");
        }
        // Step 4: Execute the Return safely
        foundStudentR.giveBorrowedBook(foundBookR);

        // --- NEW: Smart Reservation Check ---
        Student nextInLine = null;

        // Scan our students to see if anyone has this specific book reserved
        for (Student student : registeredStudents) {
            if (student.getReservedBooks().contains(foundBookR)) {
                nextInLine = student;
                break; // First-In, First-Out (FIFO) match found
            }
        }

        if (nextInLine != null) {
            // A student was waiting! Auto-transfer the book to them
            nextInLine.removeReservedBook(foundBookR); // Clear their reservation
            nextInLine.borrowBook(foundBookR);       // Hand them the book
            System.out.println("Smart Update: '" + foundBookR.getTitle() + "' was automatically assigned to waiting student: " + nextInLine.getName());
        } else {
            // Nobody is waiting, so put it back on the open shelf
            foundBookR.setAvailableCopies(foundBookR.getAvailableCopies() + 1);
        }

        System.out.println("Transaction Successful: " + foundBookR.getTitle() + " returned by " + foundStudentR.getName());
    }

    //The Reservation of Books
    public void reserveBook(String studentId, String bookId) throws Exception {
        //Step 1 and 2: Search for bookId and StucdentId in arrays using methods
        Student foundStudentRes = findStudentById(studentId);
        Book foundBookRes = findBookById(bookId);
        // Step 3: Verify the Book is actually out of stock
        // A student shouldn't reserve a book if there are copies sitting on the shelf
        if (foundBookRes.getAvailableCopies() > 0) {
            throw new Exception("Error: This book is currently available on the shelf. Use the borrow feature instead.");
        }
        //Step 4: Check the Reservation Cap; At my Library it's 3 at max
        if (foundStudentRes.getReservedBooks().size() >= 3) {
            throw new Exception("Error: Student has reached the maximum limit of 3 book reservations");
        }
        //Step 5: Check if the study have already reserved this exact Book
        if (foundStudentRes.getReservedBooks().contains(foundBookRes)) {
            throw new Exception("Error: The student has already placed a reservation on this book.");
        }
// Step 6: Execute the Reservation safely
        foundStudentRes.addReserveBook(foundBookRes);
        System.out.println("Reservation Successful: '" + foundBookRes.getTitle() + "' has been reserved for " + foundStudentRes.getName());
    }
    //  Manual Reservation Cancellation
    public void cancelReservation(String studentId, String bookId) throws Exception {

        //Step 1 and 2: Search for bookId and StucdentId in arrays using methods
        Student foundStudentCan = findStudentById(studentId);
        Book foundBookCan = findBookById(bookId);

        // Step 3: Check if the reservation actually exists
        if (!foundStudentCan.getReservedBooks().contains(foundBookCan)) {
            throw new Exception("Error: This student does not have a reservation for this book.");
        }

        // Step 4: Remove the reservation safely using your Student helper method
        foundStudentCan.removeReservedBook(foundBookCan);

        System.out.println("Reservation Cancelled: '" + foundBookCan.getTitle() + "' removed from " + foundStudentCan.getName() + "'s waitlist.");
    }

    // Books a study room if it is available
    public void bookStudyRoom(String userId, int roomNumber) throws Exception {
        for (StudyRoom room : rooms) {
            if (room.getRoomNumber() == roomNumber) {
                if (room.isBooked()) {
                    throw new Exception("Room " + roomNumber + " is already occupied!");
                }
                room.setBooked(true);
                System.out.println("User " + userId + " successfully booked Room " + roomNumber);
                return; // Exit once successful
            }
        }
        throw new Exception("Room " + roomNumber + " does not exist!");
    }

    // Vacates a study room
    public void vacateStudyRoom(int roomNumber) throws Exception {
        for (StudyRoom room : rooms) {
            if (room.getRoomNumber() == roomNumber) {
                room.setBooked(false);
                System.out.println("Room " + roomNumber + " is now open.");
                return;
            }
        }
        throw new Exception("Room " + roomNumber + " does not exist!");
    }



    //  My Helper Methods for studentId and bookId and UserId, just in case if
    //  I didn't want to elaborate, whether a student or staff is in front?
    private Student findStudentById(String studentId) throws Exception {
        for (Student student : registeredStudents) {
            if (student.getUserId().equals(studentId)) {
                return student; // Return immediately when found
            }
        }
        // If the loop finishes without returning, the student doesn't exist
        throw new Exception("Error: Student ID '" + studentId + "' not found.");
    }
    public User findUserById(String userId) throws Exception {
        // Scan students
        for (Student s : registeredStudents) {
            if (s.getUserId().equals(userId)) return s;
        }
        // Scan staff
        for (Librarian l : registeredStaff) {
            if (l.getUserId().equals(userId)) return l;
        }
        throw new Exception("Error: User ID '" + userId + "' not found.");
    }
    private Book findBookById(String bookId) throws Exception {
        for (Book book : catalog) {
            if (book.getBookId().equals(bookId)) {
                return book; // Return immediately when found
            }
        }
        throw new Exception("Error: Book ID '" + bookId + "' not found.");
    }


}


