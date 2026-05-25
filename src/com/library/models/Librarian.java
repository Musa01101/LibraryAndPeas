package com.library.models;

import java.util.ArrayList;

public class Librarian extends User {
    private String staffNumber;
    private final ArrayList<Book> managedBooks;

    public Librarian(String name, String userId, String email, String password, String staffNumber) {
        super(name, userId, email, password);
        // Validate local attributes
        setStaffNumber(staffNumber);
        //initialization to prevent NullPointerExceptions
        this.managedBooks = new ArrayList<>();
    }

    @Override
    public boolean isEligibleForService() {
        // A librarian is eligible to manage the system if they have a valid ID and staff number
        return getUserId() != null && !getUserId().isEmpty() && staffNumber != null && !staffNumber.equals("TEMP-STAFF");
    }

    public String getStaffNumber() {
        return staffNumber;
    }

    public void setStaffNumber(String staffNumber) {
        if (staffNumber != null && !staffNumber.trim().isEmpty()) {
            this.staffNumber = staffNumber;
        } else {
            this.staffNumber = "TEMP-STAFF"; // Safe fallback state
        }
    }

    public ArrayList<Book> getManagedBooks() {
        return managedBooks;
    }

    /*
     Safely adds a book to the librarian's managed list without exposing the entire array.
     */
    public void addManagedBook(Book book) {
        if (book != null && !this.managedBooks.contains(book)) {
            this.managedBooks.add(book);
        }
    }
}
