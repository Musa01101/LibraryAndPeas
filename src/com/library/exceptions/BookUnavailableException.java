package com.library.exceptions;

public class BookUnavailableException extends RuntimeException {

    private final String bookTitle;

    public BookUnavailableException(String message) {
        super(message);
        this.bookTitle = null;
    }

    /*
     Auto-generates a user-friendly message from the book title.
     */
    public BookUnavailableException(String bookTitle, boolean autoMessage) {
        super("Sorry, \"" + bookTitle + "\" has no available copies right now. "
            + "You may place a reservation to be notified when a copy becomes available.");
        this.bookTitle = bookTitle;
    }

    public String getBookTitle() { return bookTitle; }
}
