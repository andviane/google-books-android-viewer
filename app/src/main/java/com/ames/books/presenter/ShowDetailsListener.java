package com.ames.books.presenter;

import android.graphics.drawable.Drawable;

import com.ames.books.struct.Book;
import com.google.api.services.books.model.Volume;

/**
 * Something that can show the book details.
 */
public interface ShowDetailsListener {

  /**
   * Show details about the given book.
   */
  void showDetails(Book book, Drawable thumb);
}
