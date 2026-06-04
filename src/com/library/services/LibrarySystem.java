package com.library.services;

import com.library.models.*;
import com.library.exceptions.*;
import java.util.ArrayList;
//Add edition for the books for librarian and really more o teh notification like oh you browsed this book
// or like oh this book was returned, and could I try adding confirmation and stuff?
// Also make the bookshelf more beautifully
public class LibrarySystem {
    private ArrayList<Book> catalog;
    private ArrayList<Student> registeredStudents;
    private ArrayList<Librarian> registeredStaff;
    private  ArrayList<StudyRoom> rooms = new ArrayList<>();

    private  FileManager fileManager;

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
            fileManager.loadTransactions(this.registeredStudents, this.catalog, this.rooms);

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

    //getter methods for our "database"
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
            fileManager.saveTransactions(this.registeredStudents, this.rooms);
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

            if (librarian != null) {
                librarian.addManagedBook(book);
            }
        }
    }

    // Completely remove a book from the system
    public void removeBookFromSystem(String bookId) throws Exception {
        Book targetBook = findBookById(bookId);

        // 1. Safety Check: Is anyone currently borrowing or waiting for this book?
// 1. Safety Check: Is anyone currently borrowing or waiting for this book?
        for (Student student : registeredStudents) {
            if (student.getBorrowedBooks().contains(targetBook)) {
                throw new BookInUseException(bookId, student.getName(), "borrowed");
            }
            if (student.getReservedBooks().contains(targetBook)) {
                throw new BookInUseException(bookId, student.getName(), "reserved");
            }
        }

        // 2. Remove it from the main catalog
        catalog.remove(targetBook);

        // 3. Remove it from any librarian's managed list
        for (Librarian staff : registeredStaff) {
            staff.getManagedBooks().remove(targetBook);
        }

        System.out.println("System Update: " + targetBook.getTitle() + " was completely removed from the database.");
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
        //Step 1 and 2: Search for bookId and StudentId in arrays using methods
        Student foundStudent = findStudentById(studentId);
        Book foundBook = findBookById(bookId);
        // Step 3: Check if book.getAvailableCopies() > 0
        if (foundBook.getAvailableCopies() <= 0) {
            throw new BookUnavailableException(foundBook.getTitle(), true);
        }
        //  NEW: Check the Borrowing Cap
        if (foundStudent.getBorrowedBooks().size() >= 5) {
            throw new BorrowLimitExceededException(studentId, foundStudent.getBorrowedBooks().size());
        }
        // Step 4: If yes, subtract 1 copy and add the book to the student's borrowed list
        foundBook.setAvailableCopies(foundBook.getAvailableCopies() - 1);
        foundStudent.borrowBook(foundBook);
        System.out.println("Transaction Successful: " + foundBook.getTitle() + " borrowed by " + foundStudent.getName());
        // Step 5: If no, throw  BookUnavailableException, when Suhasini gives the package

    }

    // The Return of Books
    public void returnBook(String studentId, String bookId) throws Exception {
        //Step 1 and 2: Search for bookId and StudentId in arrays using methods
        Student foundStudentR = findStudentById(studentId);
        Book foundBookR = findBookById(bookId);
        //Step 3: Does the student own the book?
        if (!foundStudentR.getBorrowedBooks().contains(foundBookR)) {
            throw new InvalidTransactionException(studentId, bookId, InvalidTransactionException.Reason.BOOK_NOT_BORROWED);
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
            nextInLine.setHasPendingNotification(true); // Will truer the pop-up
            System.out.println("Smart Update: '" + foundBookR.getTitle() + "' was automatically assigned to waiting student: " + nextInLine.getName());
        } else {
            // Nobody is waiting, so put it back on the open shelf
            foundBookR.setAvailableCopies(foundBookR.getAvailableCopies() + 1);
        }

        System.out.println("Transaction Successful: " + foundBookR.getTitle() + " returned by " + foundStudentR.getName());
    }

    //The Reservation of Books
    public void reserveBook(String studentId, String bookId) throws Exception {
        //Step 1 and 2: Search for bookId and StudentId in arrays using methods
        Student foundStudentRes = findStudentById(studentId);
        Book foundBookRes = findBookById(bookId);

        // Step 3: Verify the Book is actually out of stock
        if (foundBookRes.getAvailableCopies() > 0) {
            throw new ReservationLimitException("Cannot reserve an available book.");
        }

        //Step 4: Check the Reservation Cap; At my Library it's 3 at max
        if (foundStudentRes.getReservedBooks().size() >= 3) {
            throw new ReservationLimitException(foundStudentRes.getName(), 3);
        }

        //Step 5: Check if the study have already reserved this exact Book
        if (foundStudentRes.getReservedBooks().contains(foundBookRes)) {
            throw new InvalidTransactionException(studentId, bookId, InvalidTransactionException.Reason.DUPLICATE_BORROW);
        }

        //Step 6: Check if the student have already borrowed this book?
        if (foundStudentRes.getBorrowedBooks().contains(foundBookRes)) {
            throw new InvalidTransactionException(studentId, bookId, InvalidTransactionException.Reason.DUPLICATE_BORROW);
        }

        // Step 7: Execute the Reservation safely
        foundStudentRes.addReserveBook(foundBookRes);
        System.out.println("Reservation Successful: '" + foundBookRes.getTitle() + "' has been reserved for " + foundStudentRes.getName());
    }

    // Update existing book details
    public void updateBookDetails(String bookId, String newTitle, String newAuthor, String newCategory, int newCopies, int newYear) throws Exception {
        Book targetBook = findBookById(bookId);

        targetBook.setTitle(newTitle);
        targetBook.setAuthor(newAuthor);
        targetBook.setCategory(newCategory);
        targetBook.setAvailableCopies(newCopies);
        targetBook.setPublicationYear(newYear);

        System.out.println("System Update: " + targetBook.getBookId() + " has been successfully updated.");
    }

    //  Manual Reservation Cancellation
    public void cancelReservation(String studentId, String bookId) throws Exception {
        // Search for book and student
        Student foundStudentCan = findStudentById(studentId);
        Book foundBookCan = findBookById(bookId);

        // Check if the reservation actually exists
        if (!foundStudentCan.getReservedBooks().contains(foundBookCan)) {
            throw new InvalidTransactionException(studentId, bookId, InvalidTransactionException.Reason.RESERVATION_NOT_FOUND);
        }

        // Remove the reservation safely
        foundStudentCan.removeReservedBook(foundBookCan);

        System.out.println("Reservation Cancelled: '" + foundBookCan.getTitle() + "' removed from " + foundStudentCan.getName() + "'s waitlist.");
    }
    // clear the transactions PDF, for cases when librarian adds copies of an empty book
    public void clearReservations(String bookId) {
        Book book = null;
        try { book = findBookById(bookId); } catch (Exception e) { return; }

        if (book.getAvailableCopies() > 0) {
            for (Student s : registeredStudents) {
                if (s.getReservedBooks().contains(book)) {
                    s.removeReservedBook(book); // Clear the reservation
                    s.borrowBook(book);
                    s.setHasPendingNotification(true);
                    book.setAvailableCopies(book.getAvailableCopies() - 1);
                    System.out.println("Notification: " + s.getName() + " has been auto-assigned " + book.getTitle());
                }
            }
        }
    }

    public void bookStudyRoom(String userId, int roomNum) throws Exception {
        // Boundary check for valid rooms (1-5)
        if (roomNum < 1 || roomNum > 5) {
            throw new InvalidRoomException(roomNum);
        }
        // 1. Check if the student already has a room
        for (com.library.models.StudyRoom room : rooms) {
            if (room.isBooked() && userId.equals(room.getOccupantId())) {
                throw new InvalidTransactionException(userId, "ROOM", InvalidTransactionException.Reason.ALREADY_HAS_ROOM);
            }
        }
        // 2. Book the specific room
        com.library.models.StudyRoom target = rooms.get(roomNum - 1);
        if (target.isBooked()) {
            throw new RoomOccupiedException(roomNum, "Current Session", target.getOccupantId());
        }
        target.setBooked(true);
        target.setOccupantId(userId);
    }
    public void leaveStudyRoom(String userId) throws Exception {
        for (com.library.models.StudyRoom room : rooms) {
            if (room.isBooked() && userId.equals(room.getOccupantId())) {
                room.setBooked(false);
                room.setOccupantId(null);
                return;
            }
        }
        throw new InvalidTransactionException(userId, "ROOM", InvalidTransactionException.Reason.NO_ROOM_BOOKED);
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
        throw new UserNotFoundException(studentId, UserNotFoundException.LookupField.ID);
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
        throw new UserNotFoundException(userId, UserNotFoundException.LookupField.ID);
    }

    private Book findBookById(String bookId) throws Exception {
        for (Book book : catalog) {
            if (book.getBookId().equals(bookId)) {
                return book; // Return immediately when found
            }
        }
        throw new BookNotFoundException(bookId, BookNotFoundException.LookupField.ID);
    }


}


