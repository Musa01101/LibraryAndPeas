package com.library.exceptions;
public class InvalidTransactionException extends Exception {

    public enum Reason {
        BOOK_NOT_BORROWED,
        DUPLICATE_BORROW,
        MALFORMED_RECORD,
        MISMATCHED_USER,
        RESERVATION_NOT_FOUND,
        ALREADY_HAS_ROOM,
        NO_ROOM_BOOKED
    }

    private final String studentId;
    private final String bookId;
    private final Reason reason;

    public InvalidTransactionException(String studentId, String bookId, Reason reason) {
        super(String.format(
            "Invalid transaction for student '%s' on book '%s': %s.",
            studentId, bookId, reason
        ));
        this.studentId = studentId;
        this.bookId    = bookId;
        this.reason    = reason;
    }

    public String getStudentId() { return studentId; }
    public String getBookId()    { return bookId; }
    public Reason getReason()    { return reason; }
}
