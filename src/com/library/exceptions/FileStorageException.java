package com.library.exceptions;


public class FileStorageException extends Exception {
    private final String fileName;

    public FileStorageException(String action, String fileName, String errorDetail) {
        super(String.format("Error %s file '%s': %s", action, fileName, errorDetail));
        this.fileName = fileName;
    }

    public String getFileName() { return fileName; }
}
