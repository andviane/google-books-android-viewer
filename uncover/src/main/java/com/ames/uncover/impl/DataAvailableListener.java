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
import com.ames.uncover.primary.PrimaryResponse;

/**
 * Callback to receive notifications that that data have recently been provided for the given data range.
 */
public interface DataAvailableListener<ITEM> {


  /**
   * Notify that data for this range has not been successful. Another
   * queued request may be send. The failed request may potentially
   * be retried.
   */
  void dataUnavailable(PrimaryRequest request);


  /**
   * Notify that data are now available for this request
   *
   * @param request request
   * @param response response
   */
  void dataAvailable(PrimaryRequest request, PrimaryResponse<ITEM> response);


  /**
   * Notify the fiven area is now visible, and data are very likely to be requested from here
   */
  void notifyVisibleArea(int fromInclusive, int endExclusive);
}
