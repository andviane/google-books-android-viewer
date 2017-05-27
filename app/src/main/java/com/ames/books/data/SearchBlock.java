package com.ames.books.data;

import com.ames.books.struct.Book;
import com.ames.books.struct.Books;

import java.io.Serializable;

/**
 * Represents a single search result. Volumes data structure, required by Google, does not contain the query range.
 */
public class SearchBlock implements Serializable {
  /**
   * The query result
   */
  private Books books;

  /**
   * From, inclusive
   */
  private int from;

  /**
   * To, exclusive
   */
  private int to;

  private int totalItems;

  public int getTotalItems() {
    return totalItems;
  }

  public SearchBlock(Books books, Integer totalItems, int offset) {
    this.from = offset;
    this.books = books;

    this.to = this.books.size() + offset;
    this.totalItems = extractTotalItems(totalItems);

  }

  public int getFrom() {
    return from;
  }

  public int getTo() {
    return to;
  }

  /**
   * Get item at the absolute position, or null if this block does not cover the location.
   */
  public Book get(int position) {
    if (inRange(position)) {
      return books.get(position - from);
    }
    return null;
  }

  public Books getBooks() {
    return books;
  }

  public boolean inRange(int position) {
    return position >= from && position < to;
  }

  /**
   * Get the total number of items as represented by this volume
   *
   * @return total number of items. If not reported by Google for some
   * reason, return the safe lower boundary.
   */
  private int extractTotalItems(Integer count) {
    int n;
    if (count == null) {
      n = 0;
    } else {
      n = count.intValue();
    }

    if (n == 0) {
      // Safe boundary, that much really must be.
      n = from + books.size();
    }
    return n;
  }
}
