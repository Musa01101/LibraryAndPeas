package com.library.models;

import java.util.ArrayList;

public class Student extends User {
    private String major;
    private final ArrayList<Book> borrowedBooks;
    private final ArrayList<Book> reservedBooks;
    private boolean hasPendingNotification = false;

    public Student(String major, String name, String userId, String email, String password) {
        super(name, userId, email, password);
        setMajor(major);
        this.borrowedBooks = new ArrayList<Book>();
        this.reservedBooks = new ArrayList<Book>();
    }

    public boolean hasPendingNotification() {return hasPendingNotification;}
    public void setHasPendingNotification(boolean hasPendingNotification) {this.hasPendingNotification = hasPendingNotification;}


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

    public ArrayList<Book> getBorrowedBooks() {
        return borrowedBooks;
    }

    public ArrayList<Book> getReservedBooks() {
        return reservedBooks;
    }

    public void borrowBook(Book book) {
        if (book != null) {
            this.borrowedBooks.add(book);
        }
    }

    public void giveBorrowedBook(Book book) {
        if (book != null) {
            this.borrowedBooks.remove(book);
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

