package com.library.exceptions;

public class BookInUseException extends Exception {
  private final String bookId;
  private final String studentName;

  public BookInUseException(String bookId, String studentName, String status) {
    super(String.format("Cannot delete book '%s': %s currently has it %s!", bookId, studentName, status));
    this.bookId = bookId;
    this.studentName = studentName;
  }

  public String getBookId() { return bookId; }
  public String getStudentName() { return studentName; }
}