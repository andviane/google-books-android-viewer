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

/**
 * Listener to notify when the new search data arrive. Can be used for tasks
 * like hiding progress bar and re-enabling search button. This listener only
 * receives events about arrival of the first result of the new query, and
 * is not notified about the later scrolling. The listener is normally
 * set on @{@link com.ames.uncover.UncoveringDataModel}
 */
public interface SearchCompleteListener {

  /**
   * Invoked when first (not all) results arrive after calling the @{@link com.ames.uncover.UncoveringDataModel#setQuery(Query)}
   * method of the model.
   *
   * @param query the query for which results arrive.
   */
  void onQuerySearchComplete(Query query);

}
