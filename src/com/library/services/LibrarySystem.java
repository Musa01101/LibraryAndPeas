package com.library.services;

import com.library.models.Book;
import com.library.models.Librarian;
import com.library.models.Student;

import java.util.ArrayList;

public class LibrarySystem {
    private ArrayList<Book> catalog;
    private ArrayList<Student> registeredStudents;
    private ArrayList<Librarian> registeredStaff;
    // If the studyRooms array is empty then there's the room is vacant,elif it contains a String(studentId) then it's occupied
    private String[] studyRooms;

    private FileManager fileManager;

    public LibrarySystem() {
        this.catalog = new ArrayList<>();
        this.registeredStudents = new ArrayList<>();
        this.registeredStaff = new ArrayList<>();
        this.studyRooms = new String[5];
        this.fileManager = new FileManager();
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
    public void addBookToCatalog(Book book) {
        if (book != null && !catalog.contains(book)) {
            catalog.add(book);
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

    //  Study Room Booking
    public void bookStudyRoom(String studentId, int roomNumber) throws Exception {
        // Step 1: Validate the room number (1 through 5)
        if (roomNumber < 1 || roomNumber > 5) {
            throw new Exception("Error: Invalid room number. Please choose 1-5.");
        }

        // Arrays start at 0, so Room 1 is index 0
        int roomIndex = roomNumber - 1;

        // Step 2: Check if it is already booked
        if (studyRooms[roomIndex] != null) {
            throw new Exception("Error: Room " + roomNumber + " is currently occupied by student " + studyRooms[roomIndex]);
        }

        // Step 3: Book it!
        studyRooms[roomIndex] = studentId;
        System.out.println("Success: Room " + roomNumber + " booked for Student ID: " + studentId);
    }

    //  Study Room Cancellation
    public void cancelRoomBooking(int roomNumber) throws Exception {
        if (roomNumber < 1 || roomNumber > 5) {
            throw new Exception("Error: Invalid room number. Please choose 1-5.");
        }
        int roomIndex = roomNumber - 1;
        // If the room is already empty, throw an error
        if (studyRooms[roomIndex] == null) {
            throw new Exception("Error: Room " + roomNumber + " is already empty.");
        }
        // To cancel it just set the student's ID to null
        studyRooms[roomIndex] = null;
        System.out.println("Success: Room " + roomNumber + " is now empty and available.");
    }


    //  My Helper Methods for studentId and bookId
    private Student findStudentById(String studentId) throws Exception {
        for (Student student : registeredStudents) {
            if (student.getUserId().equals(studentId)) {
                return student; // Return immediately when found
            }
        }
        // If the loop finishes without returning, the student doesn't exist
        throw new Exception("Error: Student ID '" + studentId + "' not found.");
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


