package com.library.models;

import java.util.List;

public interface IBookSearch {

    List<Book> searchByTitle(List<Book> books, String keyword);

    List<Book> searchByAuthor(List<Book> books, String author);

    List<Book> filterByCategory(List<Book> books, String category);

    List<Book> findAvailableBooks(List<Book> books);

    List<Book> sortByTitle(List<Book> books);

    List<Book> prioritizeAvailableBooks(List<Book> books);
}
