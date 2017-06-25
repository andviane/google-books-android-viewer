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

import org.junit.Test;

import com.ames.uncover.TestItem;
import com.ames.uncover.impl.Segment;

import com.ames.uncover.UncoveringDataModel;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SegmentTest {

  @Test
  public void testSegment() {
    UncoveringDataModel<TestItem> model = mock(UncoveringDataModel.class);

    when(model.getPageSize()).thenReturn(10);

    Segment s = new Segment(model, 20);
    assertEquals(200, s.getFrom());
    assertEquals(210, s.getTo());
    assertEquals(20, s.getPage());
  }

}
