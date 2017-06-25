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

import org.junit.Before;
import org.junit.Test;

import com.ames.uncover.TestItem;
import com.ames.uncover.impl.AvailableSegment;

import com.ames.uncover.UncoveringDataModel;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AvailableSegmentTest {

  UncoveringDataModel<TestItem> model = mock(UncoveringDataModel.class);

  @Before
  public void setup() {
    when(model.getPageSize()).thenReturn(10);

    // Provide paging as expected by the model.
    for (int i = 0; i < 100; i++) {
      when(model.getPage(i)).thenReturn(i / 10);
    }
  }

  @Test
  public void testGet() {
    // Segment 2 [20 .. 30[ available
    AvailableSegment<TestItem> a = new AvailableSegment<>(model, 2);
    assertEquals(20, a.getFrom());
    assertEquals(30, a.getTo());
    assertEquals(2, a.getPage());

    a.dataProvided(TestItem.range(20, 30));

    assertEquals("p.20", a.get(20).toString());
    assertEquals("p.29", a.get(29).toString());
    assertNull(a.get(19));
    assertNull(a.get(30));
  }

  @Test
  public void testCovered() {
    AvailableSegment<TestItem> a = new AvailableSegment<>(model, 2);

    when(model.getPage(anyInt())).thenReturn(2);
    assertTrue(a.covered(0));

    when(model.getPage(anyInt())).thenReturn(1);
    assertFalse(a.covered(0));
  }

  @Test
  public void testDataProvided() {
    AvailableSegment<TestItem> a = new AvailableSegment<>(model, 2);

    assertNull(a.get(20));
    assertNull(a.get(29));
    assertNull(a.get(19));
    assertNull(a.get(30));
    assertEquals(0, a.getMaxIndex());

    a.dataProvided(TestItem.range(20, 29));

    assertEquals("p.20", a.get(20).toString());
    assertEquals("p.28", a.get(28).toString());
    assertNull(a.get(19));
    assertNull(a.get(29));
    assertNull(a.get(30));
    assertEquals(29, a.getMaxIndex());

    a.dataProvided(TestItem.range(20, 30));
    assertEquals(30, a.getMaxIndex());
    assertEquals("p.29", a.get(29).toString());

    a.dataProvided(null);
    assertNull(a.get(20));
    assertNull(a.get(29));
    assertNull(a.get(19));
    assertNull(a.get(30));
    assertEquals(0, a.getMaxIndex());
  }


}
