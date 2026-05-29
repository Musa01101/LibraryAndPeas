package com.library.models;

public class StudyRoom {
    private int roomNumber;
    private boolean isBooked;

    public StudyRoom(int roomNumber) {
        this.roomNumber = roomNumber;
        this.isBooked = false; // Rooms default to open
    }

    public int getRoomNumber() {
        return roomNumber;
    }

    public boolean isBooked() {
        return isBooked;
    }

    public void setBooked(boolean booked) {
        this.isBooked = booked;
    }
}
