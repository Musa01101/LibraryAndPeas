package com.library.models;

import java.util.ArrayList;

public class Student extends User {
    private String major;
    private final ArrayList<BorrowedBook> borrowedBooks;
    private final ArrayList<Book> reservedBooks;
    private boolean hasPendingNotification = false;

    public Student(String major, String name, String userId, String email, String password) {
        super(name, userId, email, password);
        setMajor(major);
        this.borrowedBooks = new ArrayList<BorrowedBook>();
        this.reservedBooks = new ArrayList<Book>();
    }

    public boolean hasPendingNotification() {
        return hasPendingNotification;
    }

    public void setHasPendingNotification(boolean hasPendingNotification) {
        this.hasPendingNotification = hasPendingNotification;
    }

    //helper method to check the ownership
    public boolean hasBorrowedBook(Book book) {
        for (BorrowedBook bb : borrowedBooks) {
            if (bb.getBook().equals(book)) return true;
        }
        return false;
    }

    public String getMajor() {
        return major;
    }

    public void setMajor(String major) {
        if (major != null && !major.trim().isEmpty()) {
            this.major = major;
        } else {
            this.major = "Undeclared";
        }
    }

    public ArrayList<BorrowedBook> getBorrowedBooks() {
        return borrowedBooks;
    }

    public ArrayList<Book> getReservedBooks() {
        return reservedBooks;
    }

    public void borrowBook(Book book) {
        if (book != null && !this.borrowedBooks.contains(book)) {
            // Wrap the book so it gets a due date automatically
            this.borrowedBooks.add(new BorrowedBook(book));
        }
    }

    public void giveBorrowedBook(Book book) {
        if (book != null) {
            // Loop through and find the specific wrapper holding this book
            for (int i = 0; i < this.borrowedBooks.size(); i++) {
                if (borrowedBooks.get(i).getBook().equals(book)) {
                    this.borrowedBooks.remove(i);
                    return;
                }
            }
        }
    }

    public void addReserveBook(Book book) {
        if (book != null && !this.reservedBooks.contains(book)) {
            this.reservedBooks.add(book);
        }
    }

    public void removeReservedBook(Book book) {
        if (book != null) {
            this.reservedBooks.remove(book);
        }
    }

    @Override
    public String toString() {
        return this.getName() + " (ID: " + this.getUserId() + ", Major: " + this.major + ")";
    }
}

