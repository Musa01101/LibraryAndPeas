package com.library.exceptions;
public class InvalidRoomException extends Exception {

    private static final int MIN_ROOM = 1;
    private static final int MAX_ROOM = 5;
    private final int requestedRoom;

    public InvalidRoomException(int requestedRoom) {
        super(String.format(
            "Room number %d is invalid. Valid study rooms are %d to %d.",
            requestedRoom, MIN_ROOM, MAX_ROOM
        ));
        this.requestedRoom = requestedRoom;
    }

    public int getRequestedRoom() { return requestedRoom; }
    public int getMinRoom()       { return MIN_ROOM; }
    public int getMaxRoom()       { return MAX_ROOM; }
}
