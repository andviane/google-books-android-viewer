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

import com.ames.uncover.UncoveringDataModel;

/**
 * Represents the range (interval) of items, but does not include items themselves. The class is used directly to store the
 * fetch queries that do not include items. The derived class AvailableSegment also provide field to store the data.
 *
 * The segment is normally expected to cover the single page.
 *
 * The segment always belongs to the model that is
 * required to compute values dependent on the page size.
 */
public class Segment<ITEM> implements Serializable {
  protected transient UncoveringDataModel<ITEM> model;

  /**
   * The page where this segment belongs.
   */
  protected int page;

  /**
   * Create the segment.
   *
   * @param page the number of the page this segment represents.
   */
  public Segment(UncoveringDataModel<ITEM> model, int page) {
    this.model = model;
    this.page = page;
  }

  /**
   * Get start of the range, inclusive
   */
  public int getFrom() {
    return model.getPageSize() * page;
  }

  /**
   * Get end of the range, exclusive
   */
  public int getTo() {
    return model.getPageSize() * (page + 1);
  }

  public int getPage() {
    return page;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Segment<?> segment = (Segment<?>) o;
    return page == segment.page;
  }

  public void setModel(UncoveringDataModel<ITEM> model) {
    this.model = model;
  }

  @Override
  public int hashCode() {
    return page;
  }

  public String toString() {
    return "page "+page;
  }
}
