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
package com.ames.uncover;

import com.ames.uncover.primary.PrimaryDataProvider;
import com.ames.uncover.primary.PrimaryRequest;
import com.ames.uncover.primary.PrimaryResponse;

import java.util.ArrayList;
import java.util.List;

/**
 * Simulates the primary fetcher
 */
public class PrimaryDataProviderMock implements PrimaryDataProvider<TestItem> {

  @Override
  public PrimaryResponse fetch(PrimaryRequest request) {
    List<TestItem> data = new ArrayList<>();
    for (int i = request.getFrom(); i < request.getTo(); i++) {
      TestItem t = new TestItem();
      t.value = "it."+i;
      data.add(t);
    }

    PrimaryResponse<TestItem> response = new PrimaryResponse<TestItem>(data, 100);
    return response;
  }
}
