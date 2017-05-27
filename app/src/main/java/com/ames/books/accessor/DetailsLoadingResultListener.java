package com.ames.books.accessor;

import com.ames.books.struct.Book;

/**
 * Accepts notification that some result has been loaded.
 */
public interface DetailsLoadingResultListener {

  void onDetailsLoaded(Book details);

}
