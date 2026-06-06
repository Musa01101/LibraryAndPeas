package com.library.models;

import java.time.Year;
import com.library.exceptions.*;

public class Book {
    private String bookId;
    private String title;
    private String author;
    private String category;
    private String isbn;
    private int availableCopies;
    private int publicationYear;

    public Book(String bookId, String title, String author, String category, String isbn, int availableCopies, int publicationYear) throws InvalidBookDataException {
        this.bookId = bookId;
        this.title = title;
        this.author = author;
        this.category = category;
        this.isbn = isbn;
        // Route through setters to enforce validation rules immediately
        setAvailableCopies(availableCopies);
        setPublicationYear(publicationYear);
    }

    public String getBookId() {
        return bookId;
    }


    public void setBookId(String bookId) {
        this.bookId = bookId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public int getAvailableCopies() {
        return availableCopies;
    }


     // Prevents the library system from registering negative book copies.
    public void setAvailableCopies(int availableCopies) throws InvalidBookDataException {
        if (availableCopies >= 0) {
            this.availableCopies = availableCopies;
        } else {
            throw new InvalidBookDataException("Copies", String.valueOf(availableCopies), "Cannot be negative.");
        }
    }


     //Ensures the publication year is valid and not set in the future.
    public void setPublicationYear(int publicationYear) throws InvalidBookDataException{
        int currentYear = Year.now().getValue();
        if (publicationYear >= 1000 && publicationYear <= currentYear) {
            this.publicationYear = publicationYear;
        } else {
            throw new InvalidBookDataException("Year", String.valueOf(publicationYear), "Must be between 1000 and " + currentYear + ".");
        }
    }
    public int getPublicationYear() {
        return publicationYear;
    }

    @Override
    public String toString() {
        return this.title + " (ID: " + this.bookId + ")";
    }
}
