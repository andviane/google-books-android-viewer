package com.ames.books.data;

/**
 * A marks the given range as "search in progress", to prevent sending the multiple overlapping requests.
 */
public class SearchBlockPending {

  private final int from;

  private final int to;

  public SearchBlockPending(int from, int to) {
    this.from = from;
    this.to = to;
  }

  public boolean inRange(int position) {
    return  position >= from && position < to;
  }

  /**
   * Checks if the given pending block is covered by the provided
   * search block so can be discarded.
   */
  public boolean isCoveredBy(SearchBlock block) {
    return block.inRange(from) && block.inRange(to - 1);
  }
}
