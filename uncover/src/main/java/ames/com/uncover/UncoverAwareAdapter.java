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
package ames.com.uncover;

import android.support.v7.widget.LinearLayoutManager;

import ames.com.uncover.UncoveringDataModel;

/**
 * Adapter that uses UncoveringDataModel as its data model.
 */
public interface UncoverAwareAdapter<T> {

  /**
   * Notify that data have changed withing the given range.
   *
   * @param from start of the range
   * @param to end of the range
   * @param totalNumberOfItems total number of items in the list.
   */
  void dataChanged(int from, int to, int totalNumberOfItems);

  void setLayoutManger(LinearLayoutManager manager);

  UncoveringDataModel<T> getModel();
}
