package com.library.models;

public abstract class User {
    private String name;
    private String userId;
    private String email;
    private String password;
    private boolean receiveDueDateNotifs;
    private boolean receiveReservationNotifs;

    public User(String name, String userId, String email, String password) {
        this.name = name;
        setUserId(userId);
        setEmail(email);
        setPassword(password);
        this.receiveDueDateNotifs = true;
        this.receiveReservationNotifs = true;
    }


    public boolean isReceiveDueDateNotifs() {
        return receiveDueDateNotifs;
    }

    public void setReceiveDueDateNotifs(boolean receiveDueDateNotifs) {
        this.receiveDueDateNotifs = receiveDueDateNotifs;
    }

    public boolean isReceiveReservationNotifs() {
        return receiveReservationNotifs;
    }

    public void setReceiveReservationNotifs(boolean receiveReservationNotifs) {
        this.receiveReservationNotifs = receiveReservationNotifs;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        if (email != null && email.contains("@") && email.contains(".")) {
            this.email = email;
        } else {
            this.email = "Invalid@library.com";
        }

    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        if (userId != null && !userId.isEmpty()) {
            this.userId = userId;
        }
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        if (password != null && !password.isEmpty() && password.trim().length() >= 6 &&
                !password.contains("|") && !password.contains(",") &&
                !password.contains("\\") && !password.contains("/")) {
            this.password = password;
        } else {
            this.password = "InvalidPassword";
        }
    }

    public boolean login(String inputEmail, String inputPassword) {
        return this.email.equalsIgnoreCase(inputEmail) && this.password.equals(inputPassword);
    }
}
