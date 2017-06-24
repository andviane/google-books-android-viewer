/*
Copyright 2017 Audrius Meskauskas

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */
package com.ames.uncover.impl;

import java.io.Serializable;
import java.util.List;

import com.ames.uncover.UncoveringDataModel;
import com.ames.uncover.impl.Segment;

/**
 * Available data range, spanning over the given region.
 */
public class AvailableSegment<ITEM> extends Segment implements Serializable {

  /**
   * The list of items, immutable.
   */
  private List<ITEM> items;

  /**
   * The maximal possible value for the index in the whole data set. While logically somewhat out of scope, the
   * total number of items is very often a part of the otherwise limited range query. If not provided, best guess
   * is used.
   */
  private int maxIndex;

  /**
   * Create the new list of available items.
   */
  public AvailableSegment(UncoveringDataModel<?> model, int page) {
    super(model, page);
  }

  public int getMaxIndex() {
    return maxIndex;
  }

  /**
   * Get the item at the given position or null if the item is outside the range, covered by this segment.
   *
   * @param position the position
   * @return item at position or null
   */
  public ITEM get(int position) {
    if (items == null) {
      return null;
    }
    if (covered(position)) {
      int index = position - getFrom();
      return index < items.size() ? items.get(index) : null;
    }
    return null;
  }

  /**
   * Checks if the given position is covered by the segment provided.
   */
  public boolean covered(int position) {
    return model.getPage(position) == page;
  }

  /**
   * Set the data when they become available, notifying the data model listener.
   * Make the best guess about the actual number of items.
   */
  public void dataProvided(List<ITEM> items) {
    if (items == null) {
      dataProvided(items, 0);
    } else {
      dataProvided(items, getFrom() + items.size());
    }
  }

  /**
   * Set the data when they become available, notifying the data model listener.
   */
  public void dataProvided(List<ITEM> items, int maxIndex) {
    if (items != null) {
      this.items = items;
      this.maxIndex = maxIndex;
    } else {
      this.items = null;
      this.maxIndex = maxIndex;
    }
    model.dataAvailable(this);
  }

  /**
   * Get the data items as set for this segment
   */
  public List<ITEM> getData() {
    return items;
  }

  public String toString() {
    return "page " + page + " items " + items + " max " + maxIndex;
  }


}
