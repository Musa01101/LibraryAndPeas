package com.library.exceptions;
public class BookNotFoundException extends Exception {

    public enum LookupField { ID, ISBN, TITLE }

    private final String lookupValue;
    private final LookupField lookupField;

    public BookNotFoundException(String lookupValue, LookupField lookupField) {
        super(String.format(
            "No book found with %s: '%s'.",
            lookupField.name().toLowerCase(), lookupValue
        ));
        this.lookupValue = lookupValue;
        this.lookupField = lookupField;
    }

    public String getLookupValue()      { return lookupValue; }
    public LookupField getLookupField() { return lookupField; }
}
