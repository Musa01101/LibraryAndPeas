package com.library.exceptions;

public class ReservationLimitException extends RuntimeException {

    private final int limit;

    public ReservationLimitException(String message) {
        super(message);
        this.limit = -1; // unknown limit when using this constructor
    }

    /*
     Auto-generates a message using the student's name and the reservation cap.
     */
    public ReservationLimitException(String studentName, int limit) {
        super("Reservation limit reached for " + studentName + ". "
            + "You may hold a maximum of " + limit + " reservation(s) at a time. "
            + "Please cancel an existing reservation before adding a new one.");
        this.limit = limit;
    }

    public int getLimit() { return limit; }
}
