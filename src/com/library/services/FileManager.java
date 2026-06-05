package com.library.services;

import com.library.models.*;
import com.library.exceptions.*;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Scanner;


public class FileManager {
    // file names should be constant
    private static final String BOOKS_FILE = "books.txt";
    private static final String STUDENT_FILE = "students.txt";
    private static final String STAFF_FILE = "staff.txt";
    private static final String TRANSACTIONS_FILE = "transactions.txt";


    //______Books______
    //Saving the Books method
    public void saveBooks(ArrayList<Book> catalog) throws Exception {
        try (PrintWriter out = new PrintWriter(BOOKS_FILE)) {
            for (Book book : catalog) {
                out.println(
                        book.getBookId() + "|" +
                                book.getTitle() + "|" +
                                book.getAuthor() + "|" +
                                book.getCategory() + "|" +
                                book.getIsbn() + "|" +
                                book.getAvailableCopies() + "|" +
                                book.getPublicationYear()
                );
            }
            System.out.println("Success: Catalog have been saved to " + BOOKS_FILE);
        } catch (Exception e) {
            throw new FileStorageException("saving books to", BOOKS_FILE, e.getMessage());
        }
    }

        //Loading the Books method
        public ArrayList<Book> loadBooks () throws Exception {
            ArrayList<Book> loadedCatalog = new ArrayList<>();
            File file = new File(BOOKS_FILE);
            // Safety Check: If it's the very first time the exe is run, the file won't exist yet.
            // We catch this so the program doesn't crash on day one.
            if (!file.exists()) {
                System.out.println("Notice: No existing books.txt found. Starting with an empty catalog.");
                return loadedCatalog;
            }
            try (Scanner scanner = new Scanner(file)) {
                while (scanner.hasNextLine()) {
                    String line = scanner.nextLine();
                    // Chop the line of text into an array using the comma as the cut point
                    loadedCatalog.add(getBook(line));
                }
                System.out.println("Success: Catalog successfully loaded from " + BOOKS_FILE);
            } catch (Exception e) {
                throw new FileStorageException("loading books from", BOOKS_FILE, e.getMessage());
            }
            return loadedCatalog;
        }


    //helper method;
    // I didn't do the same helper method for other methods,as making them would be too messy
    // as they implement ArrayList and call for catalog and the line;
    // It works perfectly fine as for now !:)
    private static Book getBook(String line) {
        String[] data = line.split("\\|");
        // Rebuild the Book object
        // Note to self: Make sure the order here matches the order I saved them in Part 1!
        String id = data[0];
        String title = data[1];
        String author = data[2];
        String category = data[3];
        String isbn = data[4];
        int copies = Integer.parseInt(data[5]);
        int year = Integer.parseInt(data[6]);
        // Instantiate the book and add it to our list
        // (Adjust the constructor below if your Book class takes parameters in a different order)
        return new Book(id, title, author, category, isbn, copies, year);
    }


    //______Students______
    public void saveStudents(ArrayList<Student> registeredStudents) throws Exception {
        try (PrintWriter out = new PrintWriter(STUDENT_FILE)) {
            for (Student student : registeredStudents) {
                out.println(student.getMajor() + "|" +
                        student.getName() + "|" +
                        student.getUserId() + "|" +
                        student.getEmail() + "|" +
                        student.getPassword() + "|" +
                        student.isReceiveDueDateNotifs() + "|" +
                        student.isReceiveReservationNotifs());
                System.out.println("Success: " + student.getName() + " has been saved!");
            }
            System.out.println("Finished writing to " + STUDENT_FILE);
        } catch (Exception e) {
            throw new FileStorageException("saving students to", STUDENT_FILE, e.getMessage());
        }
    }

    public ArrayList<Student> loadStudents() throws FileStorageException {
        ArrayList<Student> loadedStudents = new ArrayList<>();
        File file = new File(STUDENT_FILE);
        if (!file.exists()) {
            System.out.println("Notice: No existing student.txt found. Starting with an empty catalog.");
            return loadedStudents;
        }

        try (Scanner scanner = new Scanner(file)) {
            while (scanner.hasNextLine()) {
                String[] data = scanner.nextLine().split("\\|");
                Student loadedStudent = new Student(data[0], data[1], data[2], data[3], data[4]);

                // Safety check: Handles both old saves (1 boolean) and new saves (2 booleans)
                if (data.length == 6) {
                    boolean oldSetting = Boolean.parseBoolean(data[5]);
                    loadedStudent.setReceiveDueDateNotifs(oldSetting);
                    loadedStudent.setReceiveReservationNotifs(oldSetting);
                } else if (data.length > 6) {
                    loadedStudent.setReceiveDueDateNotifs(Boolean.parseBoolean(data[5]));
                    loadedStudent.setReceiveReservationNotifs(Boolean.parseBoolean(data[6]));
                }

                loadedStudents.add(loadedStudent);
            }
            System.out.println("Success: Students loaded from " + STUDENT_FILE);
        } catch (Exception e) {
            throw new FileStorageException("loading students from", STUDENT_FILE, e.getMessage());
        }
        return loadedStudents;
    }

    //______Transactions (The Ledger)______
    public void saveTransactions(ArrayList<Student> students, ArrayList<StudyRoom> rooms) throws Exception {
        try (PrintWriter out = new PrintWriter(TRANSACTIONS_FILE)) {
            for (Student student : students) {
                for (Book book : student.getBorrowedBooks()) {
                    out.println(student.getUserId() + "," + book.getBookId() + ",BORROWED");
                }
                for (Book book : student.getReservedBooks()) {
                    out.println(student.getUserId() + "," + book.getBookId() + ",RESERVED");
                }
            }
            // Tag booked rooms at the bottom, now including the occupantId!
            for (StudyRoom room : rooms) {
                if (room.isBooked() && room.getOccupantId() != null) {
                    out.println("ROOM," + room.getRoomNumber() + ",BOOKED," + room.getOccupantId());
                }
            }
            System.out.println("Success: Transactions saved to " + TRANSACTIONS_FILE);
        }catch (Exception e) {
            throw new FileStorageException("saving transactions to", TRANSACTIONS_FILE, e.getMessage());
        }
    }

    public void loadTransactions(ArrayList<Student> students, ArrayList<Book> catalog, ArrayList<StudyRoom> rooms) throws Exception {
        File file = new File(TRANSACTIONS_FILE);
        if (!file.exists()) return;

        try (Scanner scanner = new Scanner(file)) {
            while (scanner.hasNextLine()) {
                String[] data = scanner.nextLine().split(",");

                // Catch the room saves first!
                if (data[0].equals("ROOM")) {
                    int roomNum = Integer.parseInt(data[1]);
                    // Safely grab the ID we just added
                    String occupantId = data.length > 3 ? data[3] : null;

                    for (StudyRoom r : rooms) {
                        if (r.getRoomNumber() == roomNum) {
                            r.setBooked(true);
                            r.setOccupantId(occupantId); // Restore ownership!
                        }
                    }
                    continue; // Skip the rest of the loop and go to the next line
                }

                String studentId = data[0];
                String bookId = data[1];
                String status = data[2];

                Student foundStudent = null;
                Book foundBook = null;

                for (Student s : students) {
                    if (s.getUserId().equals(studentId)) {
                        foundStudent = s;
                        break;
                    }
                }
                for (Book b : catalog) {
                    if (b.getBookId().equals(bookId)) {
                        foundBook = b;
                        break;
                    }
                }

                if (foundStudent != null && foundBook != null) {
                    if (status.equals("BORROWED")) foundStudent.borrowBook(foundBook);
                    else if (status.equals("RESERVED")) foundStudent.addReserveBook(foundBook);
                }
            }
            System.out.println("Success: Transactions loaded from " + TRANSACTIONS_FILE);
        } catch (Exception e) {
            throw new FileStorageException("loading transactions from", TRANSACTIONS_FILE, e.getMessage());
        }
    }

    //Saving the Librarians  method
    public void saveLibrarians(ArrayList<Librarian> registeredStaff) throws FileStorageException {
        try (PrintWriter out = new PrintWriter(STAFF_FILE)) {
            for (Librarian staff : registeredStaff) {
                String name = staff.getName();
                String id = staff.getUserId();
                String email = staff.getEmail();
                String password = staff.getPassword();
                String staffNumber = staff.getStaffNumber();

                StringBuilder managedIds = new StringBuilder();
                for (Book book : staff.getManagedBooks()) {
                    managedIds.append(book.getBookId()).append(";");
                }
                if (!managedIds.isEmpty()) {
                    managedIds.setLength(managedIds.length() - 1);
                }
                // Safe spacer trick to prevent empty-string crashes
                String mIds = managedIds.isEmpty() ? "NONE" : managedIds.toString();

                out.println(name + "|"
                        + id + "|"
                        + email + "|"
                        + password + "|"
                        + staffNumber + "|"
                        + mIds + "|"
                        + staff.isReceiveReservationNotifs());
                System.out.println("Success: " + staff.getName() + " has been saved!");
            }
            System.out.println("Finished writing to " + STAFF_FILE);
        } catch (Exception e) {
            throw new FileStorageException("saving staff to", STAFF_FILE, e.getMessage());
        }
    }

    //Loading the Librarians methods
    public ArrayList<Librarian> loadLibrarians(ArrayList<Book> catalog) throws Exception {
        ArrayList<Librarian> loadedLibrarian = new ArrayList<>();
        File file = new File(STAFF_FILE);
        if (!file.exists()) {
            System.out.println("Notice: No existing staff.txt found. Starting with empty staff list.");
            return loadedLibrarian;
        }
        try (Scanner scanner = new Scanner(file)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if(line.trim().isEmpty()) continue; // Prevent first-day errors, if the file was empty
                String[] data = line.split("\\|");
                String name = data[0];
                String userId = data[1];
                String email = data[2];
                String password = data[3];
                String staffNumber = data[4];


                Librarian rebuiltLibrarian = new Librarian(name, userId, email, password, staffNumber);
                // The Linking Trick for Managed Books the same as per student method
                if (data.length > 5 && !data[5].isEmpty()) {
                    String[] managedIds = data[5].split(";");
                    for (String mId : managedIds) {
                        for (Book book : catalog) {
                            if (book.getBookId().equals(mId)) {
                                rebuiltLibrarian.getManagedBooks().add(book);
                                break;
                            }
                        }
                    }
                }

                // --- Load the stock alert preference if it exists ---
                if (data.length > 6) {
                    rebuiltLibrarian.setReceiveReservationNotifs(Boolean.parseBoolean(data[6]));
                }

                loadedLibrarian.add(rebuiltLibrarian);
            }
            System.out.println("Success: Librarian successfully loaded from " + STAFF_FILE);
        } catch (Exception e) {
            throw new FileStorageException("loading staff from", STAFF_FILE, e.getMessage());
        }
        return loadedLibrarian;
    }

}