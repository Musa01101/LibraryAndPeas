package com.library.exceptions;
public class UserNotFoundException extends Exception {

    public enum LookupField { ID, EMAIL, NAME }

    private final String lookupValue;
    private final LookupField lookupField;

    public UserNotFoundException(String lookupValue, LookupField lookupField) {
        super(String.format(
            "No user found with %s: '%s'.",
            lookupField.name().toLowerCase(), lookupValue
        ));
        this.lookupValue = lookupValue;
        this.lookupField = lookupField;
    }

    public String getLookupValue()      { return lookupValue; }
    public LookupField getLookupField() { return lookupField; }
}
