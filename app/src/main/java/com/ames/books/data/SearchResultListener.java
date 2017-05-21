package com.ames.books.data;

/**
 * Something that can accept the search results.
 */
public interface SearchResultListener {

  /**
   * Search results available.
   */
  void onQueryResult(SearchBlock books, String query);

}
