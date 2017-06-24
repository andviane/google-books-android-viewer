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

import com.ames.uncover.impl.AvailableSegment;
import com.ames.uncover.impl.DataFetchManager;
import com.ames.uncover.primary.Query;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class UncoveringModelTest {

  public void setup() {

  }

  @Test
  public void testGetPageSize() {
    UncoveringDataModel<TestItem> model = new UncoveringDataModel<>();

    assertEquals(10, model.getPageSize());

    for (int i = 0; i < 100; i++) {
      assertEquals("at "+i, i / 10, model.getPage(i));
    }

    model.setPageSize(2);
    for (int i = 0; i < 100; i++) {
      assertEquals("at "+i, i / 2, model.getPage(i));
    }
  }

  @Test
  public void testGetItemRequesting() {
    DataFetchManager manager = mock(DataFetchManager.class);
    UncoveringDataModel<TestItem> model = new UncoveringDataModel<>();
    model.setDataFetcher(manager);

    assertNull(model.getItem(20));

    final AvailableSegment<TestItem> s1 = new AvailableSegment<>(model, 2);
    verify(manager).requestData(2);

    model.setLoadingPlaceHolder(new TestItem("loading"));
    assertEquals("loading", model.getItem(30).value);
    final AvailableSegment<TestItem> s2 = new AvailableSegment<>(model, 3);
    verify(manager).requestData(3);

    // Proactive
    AvailableSegment<TestItem> s1n = new AvailableSegment<>(model, 2);
    s1n.dataProvided(TestItem.range(s1.getFrom(), s1.getTo()));
    model.dataAvailable(s1n);

    assertEquals("loading", model.getItem(19).value);
    assertEquals("p.20", model.getItem(20).value);
    assertEquals("loading", model.getItem(30).value);
    assertEquals(30, model.size());

   // Via segment
    s2.dataProvided(TestItem.range(s2.getFrom(), s2.getTo()));

    // No data available call but the segment notifies the model.

    assertEquals("loading", model.getItem(19).value);
    assertEquals("p.20", model.getItem(20).value);
    assertEquals("p.35", model.getItem(35).value);
    assertEquals(40, model.size());

    model.reset();
    assertEquals("loading", model.getItem(30).value);
    assertEquals(0, model.size());
    verify(manager).reset();
  }

  @Test
  public void testNoRepetetiveFetch() {
    DataFetchManager manager = mock(DataFetchManager.class);
    UncoveringDataModel<TestItem> model = new UncoveringDataModel<>();
    model.setDataFetcher(manager);

    assertNull(model.getItem(20));
    final AvailableSegment<TestItem> s1 = new AvailableSegment<>(model, 2);
    verify(manager).requestData(2);
    verify(manager).alreadyFetching(2);

    reset(manager);
    when(manager.alreadyFetching(2)).thenReturn(true);
    assertNull(model.getItem(20));
    verify(manager, never()).requestData(anyInt());
  }

  @Test
  public void testNoExisting() {
    DataFetchManager manager = mock(DataFetchManager.class);
    UncoveringDataModel<TestItem> model = new UncoveringDataModel<>();
    model.setDataFetcher(manager);

    AvailableSegment<TestItem> s1 = new AvailableSegment<>(model, 2);
    s1.dataProvided(TestItem.range(s1.getFrom(), s1.getTo()));
    model.dataAvailable(s1);

    assertEquals("p.20", model.getItem(20).value);
    verify(manager, never()).requestData((anyInt()));

    assertNull(model.getItem(300));
    verify(manager).requestData(30);
  }

  @Test
  public void testSetQuery() {
    DataFetchManager manager = mock(DataFetchManager.class);
    UncoveringDataModel<TestItem> model = new UncoveringDataModel<>();
    model.setDataFetcher(manager);

    // Some data that must be discarded
    AvailableSegment<TestItem> s1 = new AvailableSegment<>(model, 2);
    s1.dataProvided(TestItem.range(s1.getFrom(), s1.getTo()));
    model.dataAvailable(s1);

    model.setQuery(new Query("abc"));

    verify(manager).reset();
    verify(manager).requestData(0);
    assertNull(model.getItem(0));
  }
}
