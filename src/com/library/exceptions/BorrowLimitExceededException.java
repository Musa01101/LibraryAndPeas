package com.library.exceptions;
public class BorrowLimitExceededException extends Exception {

    private static final int BORROW_LIMIT = 5;
    private final String studentId;
    private final int currentCount;

    public BorrowLimitExceededException(String studentId, int currentCount) {
        super(String.format(
            "Student '%s' has reached the borrowing limit. Currently borrowed: %d/%d.",
            studentId, currentCount, BORROW_LIMIT
        ));
        this.studentId = studentId;
        this.currentCount = currentCount;
    }

    public String getStudentId() { return studentId; }
    public int getCurrentCount() { return currentCount; }
    public int getBorrowLimit() { return BORROW_LIMIT; }
}
