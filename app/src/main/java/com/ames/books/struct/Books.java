package com.ames.books.struct;

import java.util.ArrayList;

/**
 * Defined to avoid clutter by putting full blown declaration everywhere.
 */
public class Books extends ArrayList<Book> {

  public Books(int expectedSize) {
    super(expectedSize);
  }
}
