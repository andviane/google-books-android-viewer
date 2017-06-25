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
package com.ames.uncover.primary;

import java.util.List;

/**
 * Response as received from the primary data provider.
 */
public class PrimaryResponse<ITEM> {

  /**
   * Data for the given query range.
   */
  protected final List<ITEM> data;

  /**
   * The total number of items that might be fetched as a side result of the query.
   */
  protected final Integer totalItems;

  /**
   * Bild primary response that carries data and the total number of data for the query
   *
   * @param data the data (normally only a single page)
   * @param totalItems the number of data items in total, can be huge.
   */
  public PrimaryResponse(List<ITEM> data, int totalItems) {
    this.data = data;
    this.totalItems = totalItems;
  }

  /**
   * Build the primary response the has not data about the total number of items. The model will
   * use value from the previous response or will make the best guess.
   * @param data
   */
  public PrimaryResponse(List<ITEM> data) {
    this.data = data;
    this.totalItems = null;
  }

  /**
   * Get the data items.
   */
  public List<ITEM> getData() {
    return data;
  }

  /**
   * Get the total number of items for the query
   *
   * @return the number of items, null if unknown and not provided.
   */
  public Integer getTotalItems() {
    return totalItems;
  }
}
