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

import com.ames.uncover.primary.PrimaryRequest;

/**
 * Data fetcher
 */
public interface DataFetcher<ITEM> {

  /**
   * Set the on data available listener to be notified when data are availab.e
   */
  void setOnDataAvailableListener(DataAvailableListener<ITEM> listener);

  /**
   * Initiate data request
   */
  void requestData(PrimaryRequest request);

  /**
   * Create the new instance of the data fetcher free from all processes pending.
   */
  DataFetcher reset();
}
