package com.library.exceptions;

public class InvalidBookDataException extends Exception {

    private final String fieldName;
    private final String invalidValue;

    public InvalidBookDataException(String message) {
        super(message);
        // field-level details unavailable when using this constructor
        this.fieldName    = null;
        this.invalidValue = null;
    }

    /*
     Identifies the exact field that failed so the GUI can highlight it.
     */
    public InvalidBookDataException(String fieldName, String invalidValue, String reason) {
        super("Invalid value for field \"" + fieldName + "\": "
            + "\"" + invalidValue + "\" — " + reason);
        this.fieldName    = fieldName;
        this.invalidValue = invalidValue;
    }

    public String getFieldName()    { return fieldName; }
    public String getInvalidValue() { return invalidValue; }
}
