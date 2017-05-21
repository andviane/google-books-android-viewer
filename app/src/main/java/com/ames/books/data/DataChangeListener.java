package com.ames.books.data;

/**
 * A listener, to notify that data have changed (to abstract from exactly RecyclerView)
 */
public interface DataChangeListener {

  /**
   * All data changed.
   */
  void notifyDataChanged();

  /**
   * Notify the region changed.
   */
  void notifyRegionChanged(int from, int to);
}
