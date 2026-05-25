package com.library.services;

import com.library.models.Book;
import com.library.models.Librarian;
import com.library.models.Student;

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
                        book.getBookId() + "," +
                                book.getTitle() + "," +
                                book.getAuthor() + "," +
                                book.getCategory() + "," +
                                book.getIsbn() + "," +
                                book.getAvailableCopies() + "," +
                                book.getPublicationYear()
                );
            }
            System.out.println("Success: Catalog have been saved to " + BOOKS_FILE);
        } catch (Exception e) {
            throw new Exception("Error saving books to file: " + e.getMessage());
        }
    }

    //Loading the Books method
    public ArrayList<Book> loadBooks() throws Exception {
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
            throw new Exception("Error loading books from file: " + e.getMessage());
        }
        return loadedCatalog;
    }

    //helper method;
    // I didn't do the same helper method for other methods,as making them would be too messy
    // as they implement ArrayList and call for catalog and the line;
    // It works perfectly fine as for now !:)
    private static Book getBook(String line) {
        String[] data = line.split(",");
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
                out.println(student.getMajor() + "," +
                        student.getName() + "," +
                        student.getUserId() + "," +
                        student.getEmail() + "," +
                        student.getPassword());
            }
            System.out.println("Success: Students have been saved to " + STUDENT_FILE);
        } catch (Exception e) {
            throw new Exception("Error saving students to a file: " + e.getMessage());
        }
    }

    public ArrayList<Student> loadStudents() throws Exception {
        ArrayList<Student> loadedStudents = new ArrayList<>();
        File file = new File(STUDENT_FILE);
        if (!file.exists()) return loadedStudents;

        try (Scanner scanner = new Scanner(file)) {
            while (scanner.hasNextLine()) {
                String[] data = scanner.nextLine().split(",");
                loadedStudents.add(new Student(data[0], data[1], data[2], data[3], data[4]));
            }
            System.out.println("Success: Students loaded from " + STUDENT_FILE);
        } catch (Exception e) {
            throw new Exception("Error loading students: " + e.getMessage());
        }
        return loadedStudents;
    }

    //______Transactions (The Ledger)______
    public void saveTransactions(ArrayList<Student> students) throws Exception {
        try (PrintWriter out = new PrintWriter(TRANSACTIONS_FILE)) {
            for (Student student : students) {
                for (Book book : student.getBorrowedBooks()) {
                    out.println(student.getUserId() + "," + book.getBookId() + ",BORROWED");
                }
                for (Book book : student.getReservedBooks()) {
                    out.println(student.getUserId() + "," + book.getBookId() + ",RESERVED");
                }
            }
            System.out.println("Success: Transactions saved to " + TRANSACTIONS_FILE);
        } catch (Exception e) {
            throw new Exception("Error saving transactions: " + e.getMessage());
        }
    }

    public void loadTransactions(ArrayList<Student> students, ArrayList<Book> catalog) throws Exception {
        File file = new File(TRANSACTIONS_FILE);
        if (!file.exists()) return;

        try (Scanner scanner = new Scanner(file)) {
            while (scanner.hasNextLine()) {
                String[] data = scanner.nextLine().split(",");
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
            throw new Exception("Error loading transactions: " + e.getMessage());
        }
    }


    //______Librarians______
    //Saving the Librarians  method
    public void saveLibrarians(ArrayList<Librarian> registeredStaff) throws Exception {
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

                out.println(name + ","
                        + id + ","
                        + email + ","
                        + password + ","
                        + staffNumber + ","
                        + managedIds);
                System.out.println("Success: Staff saved to " + STAFF_FILE);
            }
        } catch (Exception e) {
            throw new Exception("Error saving staff to file: " + e.getMessage());
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
                String[] data = line.split(",");
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

                loadedLibrarian.add(rebuiltLibrarian);
            }
            System.out.println("Success: Librarian successfully loaded from " + STAFF_FILE);
        } catch (Exception e) {
            throw new Exception("Error reading from librarian file: " + e.getMessage());
        }
        return loadedLibrarian;
    }

}






