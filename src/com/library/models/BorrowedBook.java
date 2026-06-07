package com.library.models;

import java.time.LocalDate;

public class BorrowedBook {
    private Book book;
    private LocalDate dueDate;

    // Constructor sets the due date automatically (e.g. 3 days from today)
    public BorrowedBook(Book book) {
        this.book = book;
        this.dueDate = LocalDate.now().plusDays(3);
    }

    // Overloaded constructor for loading from the text files
    public BorrowedBook(Book book, LocalDate dueDate) {
        this.book = book;
        this.dueDate = dueDate;
    }

    public Book getBook() {
        return book;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    // Helper method to check if the book is overdue
    public boolean isOverdue() {
        return LocalDate.now().isAfter(dueDate);
    }

    @Override
    public String toString() {
        return book.getTitle() + " (Due: " + dueDate.toString() + ")";
    }
}