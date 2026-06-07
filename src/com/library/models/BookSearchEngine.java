package com.library.models;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class BookSearchEngine implements IBookSearch {

    /*
     Returns books whose title contains the keyword.
     Uses partial, case-insensitive matching so "harry" matches "Harry Potter".
     */
    @Override
    public List<Book> searchByTitle(List<Book> books, String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return new ArrayList<>();
        }
        String lowerKeyword = keyword.toLowerCase().trim();
        List<Book> results = new ArrayList<>();
        for (Book book : books) {
            if (book.getTitle().toLowerCase().contains(lowerKeyword)) {
                results.add(book);
            }
        }
        return results;
    }

    /*
     Returns books whose author field contains the given name.
     Partial matching so "Rowling" matches "J.K. Rowling".
     */
    @Override
    public List<Book> searchByAuthor(List<Book> books, String author) {
        if (author == null || author.trim().isEmpty()) {
            return new ArrayList<>();
        }
        String lowerAuthor = author.toLowerCase().trim();
        List<Book> results = new ArrayList<>();
        for (Book book : books) {
            if (book.getAuthor().toLowerCase().contains(lowerAuthor)) {
                results.add(book);
            }
        }
        return results;
    }

    /*
     Returns only books that match the given category exactly (case-insensitive).
     Designed for dropdown selection,   not keyword search.
     */
    @Override
    public List<Book> filterByCategory(List<Book> books, String category) {
        if (category == null || category.trim().isEmpty()) {
            return new ArrayList<>();
        }
        String lowerCategory = category.toLowerCase().trim();
        List<Book> results = new ArrayList<>();
        for (Book book : books) {
            if (book.getCategory().toLowerCase().equals(lowerCategory)) {
                results.add(book);
            }
        }
        return results;
    }

    /*
     Filters out books with no available copies.
     */
    @Override
    public List<Book> findAvailableBooks(List<Book> books) {
        List<Book> results = new ArrayList<>();
        for (Book book : books) {
            if (book.getAvailableCopies() > 0) {
                results.add(book);
            }
        }
        return results;
    }

    /*
     Returns a new list sorted alphabetically by title.
     Does not modify the original list passed in.
     */
    @Override
    public List<Book> sortByTitle(List<Book> books) {
        List<Book> sorted = new ArrayList<>(books); // copy to avoid modifying the original
        sorted.sort(Comparator.comparing(book -> book.getTitle().toLowerCase()));
        return sorted;
    }

    /*
     Sorts so available books appear first, unavailable books last.
     Does not modify the original list passed in.
     */
    @Override
    public List<Book> prioritizeAvailableBooks(List<Book> books) {
        List<Book> sorted = new ArrayList<>(books); // copy to avoid modifying the original
        sorted.sort(Comparator.comparingInt(book -> (book.getAvailableCopies() > 0) ? 0 : 1));
        return sorted;
    }
}
