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
 * Simple data provider that can provide data in synchronuos blocking call.
 */
public interface PrimaryDataProvider<ITEM> {

  /**
   * Simply fetch the data. The thread is expected to block till the data are available.
   */
  PrimaryResponse<ITEM> fetch(PrimaryRequest request);
}
