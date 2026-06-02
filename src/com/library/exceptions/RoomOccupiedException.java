package com.library.exceptions;
public class RoomOccupiedException extends Exception {

    private final int roomNumber;
    private final String requestedTimeSlot;
    private final String occupiedBy;

    public RoomOccupiedException(int roomNumber, String requestedTimeSlot, String occupiedBy) {
        super(String.format(
            "Study room %d is already booked for %s (held by student '%s').",
            roomNumber, requestedTimeSlot, occupiedBy
        ));
        this.roomNumber        = roomNumber;
        this.requestedTimeSlot = requestedTimeSlot;
        this.occupiedBy        = occupiedBy;
    }

    public int getRoomNumber()          { return roomNumber; }
    public String getRequestedTimeSlot(){ return requestedTimeSlot; }
    public String getOccupiedBy()       { return occupiedBy; }
}
