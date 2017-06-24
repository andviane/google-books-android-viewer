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

import java.util.ArrayList;
import java.util.HashSet;

import com.ames.uncover.TestItem;
import com.ames.uncover.impl.AvailableSegment;
import com.ames.uncover.impl.DataAvailableListener;
import com.ames.uncover.impl.DataFetchManager;
import com.ames.uncover.impl.DataFetcher;

import com.ames.uncover.UncoveringDataModel;
import com.ames.uncover.primary.PrimaryRequest;
import com.ames.uncover.primary.PrimaryResponse;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Data fetch manager test
 */
public class DataFetchManagerTest {

  class DataFetcherTester implements DataFetcher<TestItem> {
    ArrayList<PrimaryRequest> requests = new ArrayList<>();
    int resets;

    @Override
    public void setOnDataAvailableListener(DataAvailableListener<TestItem> listener) {

    }

    @Override
    public void requestData(PrimaryRequest request) {
      System.out.println("Send " + request);
      requests.add(request);
    }

    @Override
    public DataFetcher reset() {
      resets++;
      requests.clear();
      return this;
    }

    /**
     * Get the last request
     */
    public PrimaryRequest request() {
      return request(requests.size() - 1);
    }

    /**
     * Request by order.
     */
    public PrimaryRequest request(int n) {
      if (requests.size() <= n) {
        return null;
      }
      return requests.get(n);
    }
  }

  UncoveringDataModel<TestItem> model;
  DataFetcherTester primary = new DataFetcherTester();

  @Before
  public void setup() {
    model = mock(UncoveringDataModel.class);
    when(model.getPageSize()).thenReturn(10);

    // Provide paging as expected by the model.
    for (int i = 0; i < 1100; i++) {
      when(model.getPage(i)).thenReturn(i / 10);
    }
  }

  @Test
  public void testRequestData() throws Exception {
    DataFetchManager<TestItem> fetcher = new DataFetchManager<>();
    fetcher.setDataFetcher(primary);
    fetcher.setDataModel(model);
    fetcher.setDelayWhenPending(700);
    fetcher.setThreads(1);

    int pg2 = 2;
    int pg10 = 10;
    int pg20 = 20;
    int pg50 = 50;

    AvailableSegment<TestItem> s2 = new AvailableSegment<>(model, pg2);
    AvailableSegment<TestItem> s10 = new AvailableSegment<>(model, pg10);
    AvailableSegment<TestItem> s20 = new AvailableSegment<>(model, pg20);
    AvailableSegment<TestItem> s50 = new AvailableSegment<>(model, pg50);

    assertFalse(fetcher.alreadyFetching(pg2));
    assertFalse(fetcher.alreadyFetching(pg10));
    assertFalse(fetcher.alreadyFetching(pg20));
    assertFalse(fetcher.alreadyFetching(pg50));

    fetcher.requestData(pg2);

    assertTrue(fetcher.alreadyFetching(pg2));
    assertFalse(fetcher.alreadyFetching(pg10));
    assertFalse(fetcher.alreadyFetching(pg20));
    assertFalse(fetcher.alreadyFetching(pg50));

    fetcher.requestData(pg10);

    assertTrue(fetcher.alreadyFetching(pg2));
    assertTrue(fetcher.alreadyFetching(pg10));
    assertFalse(fetcher.alreadyFetching(pg20));
    assertFalse(fetcher.alreadyFetching(pg50));

    assertFalse(fetcher.alreadyFetching(3));

    // Send the two. s50 must be serviced first because it has been sent later.
    fetcher.requestData(pg20);
    fetcher.requestData(pg50);

    assertTrue(fetcher.alreadyFetching(pg2));
    assertTrue(fetcher.alreadyFetching(pg10));
    assertTrue(fetcher.alreadyFetching(pg20));
    assertTrue(fetcher.alreadyFetching(pg50));

    assertNotNull(primary.request());
    assertEquals("Second request must only be queued", 1, primary.requests.size());

    assertEquals(s2.getFrom(), primary.request().getFrom());
    assertEquals(s2.getTo(), primary.request().getTo());

    PrimaryResponse r2 = new PrimaryResponse(TestItem.range(s2.getFrom(), s2.getTo()), 777);
    fetcher.dataAvailable(primary.request(), r2);

    verify(model).dataAvailable(s2);

    assertEquals("Second request must be sent", 2, primary.requests.size());
    assertEquals("p50 request must be sent first", 500, primary.request().getFrom());

    // Two requests are delayed (first not, second not because acknowledged). Add 300 ms margin.
    Thread.sleep(2 * (fetcher.getDelayWhenPending() + fetcher.timerExtraDelay) + 300);

    assertEquals("All requests must be sent", 4, primary.requests.size());

    // The expected order of delivery
    assertEquals(20, primary.requests.get(0).getFrom());
    assertEquals(500, primary.requests.get(1).getFrom());
    assertEquals(200, primary.requests.get(2).getFrom());
    assertEquals(100, primary.requests.get(3).getFrom());

    PrimaryResponse r50 = new PrimaryResponse(TestItem.range(s50.getFrom(), s50.getTo()), 777);
    fetcher.dataAvailable(primary.request(1), r50);
    verify(model).dataAvailable(s50);

    PrimaryResponse r20 = new PrimaryResponse(TestItem.range(s20.getFrom(), s20.getTo()), 777);
    fetcher.dataAvailable(primary.request(2), r20);
    verify(model).dataAvailable(s20);

    PrimaryResponse r10 = new PrimaryResponse(TestItem.range(s10.getFrom(), s10.getTo()), 777);
    fetcher.dataAvailable(primary.request(3), r10);
    verify(model).dataAvailable(s10);
  }

  @Test
  public void testDiscardOverScrolledContent() throws Exception {
    DataFetchManager<TestItem> fetcher = new DataFetchManager<>();
    fetcher.setDataFetcher(primary);
    fetcher.setDataModel(model);
    fetcher.setDelayWhenPending(700);
    fetcher.setThreads(1);

    int pg2 = 2;
    int pg10 = 10;
    int pg20 = 20;
    int pg50 = 50;

    AvailableSegment<TestItem> s10 = new AvailableSegment<>(model, pg10);

    fetcher.requestData(pg2);
    fetcher.requestData(pg10);
    fetcher.requestData(pg20);
    fetcher.requestData(pg50);

    assertTrue(fetcher.alreadyFetching(pg2));
    assertTrue(fetcher.alreadyFetching(pg10));
    assertTrue(fetcher.alreadyFetching(pg20));
    assertTrue(fetcher.alreadyFetching(pg50));

    fetcher.notifyVisibleArea(s10.getFrom(), s10.getTo());

    assertTrue(fetcher.alreadyFetching(pg2));   // Already sent
    assertTrue(fetcher.alreadyFetching(pg10));  // visible
    assertFalse(fetcher.alreadyFetching(pg20)); // discardable
    assertFalse(fetcher.alreadyFetching(pg50)); // discardable
  }

  @Test
  public void test100Channels() {
    DataFetchManager<TestItem> fetcher = new DataFetchManager<>();
    fetcher.setDataFetcher(primary);
    fetcher.setDataModel(model);
    fetcher.setDelayWhenPending(700000L);
    fetcher.setThreads(200);

    for (int i = 0; i < 100; i++) {
      fetcher.requestData(i);
    }

    // As there is no limit, all must fire immediately
    assertEquals(100, primary.requests.size());

    // Verify all unique
    HashSet<Integer> ex = new HashSet<>();
    for (PrimaryRequest r: primary.requests) {
      assertTrue("Duplicate "+r.getFrom(), ex.add(model.getPage(r.getFrom())));
    }
  }

  @Test
  public void testRequestData2Channels() throws Exception {
    DataFetchManager<TestItem> fetcher = new DataFetchManager<>();
    fetcher.setDataFetcher(primary);
    fetcher.setDataModel(model);
    fetcher.setDelayWhenPending(700);
    fetcher.setThreads(2);

    int pg2 = 2;
    int pg10 = 10;
    int pg20 = 20;
    int pg50 = 50;

    AvailableSegment<TestItem> s2 = new AvailableSegment<>(model, pg2);
    AvailableSegment<TestItem> s10 = new AvailableSegment<>(model, pg10);
    AvailableSegment<TestItem> s20 = new AvailableSegment<>(model, pg20);
    AvailableSegment<TestItem> s50 = new AvailableSegment<>(model, pg50);

    assertFalse(fetcher.alreadyFetching(pg2));
    assertFalse(fetcher.alreadyFetching(pg10));
    assertFalse(fetcher.alreadyFetching(pg20));
    assertFalse(fetcher.alreadyFetching(pg50));

    fetcher.requestData(pg2);

    assertTrue(fetcher.alreadyFetching(pg2));
    assertFalse(fetcher.alreadyFetching(pg10));
    assertFalse(fetcher.alreadyFetching(pg20));
    assertFalse(fetcher.alreadyFetching(pg50));

    fetcher.requestData(pg10);

    assertTrue(fetcher.alreadyFetching(pg2));
    assertTrue(fetcher.alreadyFetching(pg10));
    assertFalse(fetcher.alreadyFetching(pg20));
    assertFalse(fetcher.alreadyFetching(pg50));

    assertFalse(fetcher.alreadyFetching(3));

    // Send the two. s50 must be serviced first because it has been sent later.
    fetcher.requestData(pg20);
    fetcher.requestData(pg50);

    assertTrue(fetcher.alreadyFetching(pg2));
    assertTrue(fetcher.alreadyFetching(pg10));
    assertTrue(fetcher.alreadyFetching(pg20));
    assertTrue(fetcher.alreadyFetching(pg50));

    assertNotNull(primary.request());
    assertEquals("Two channels - both requests must be sent", 2, primary.requests.size());

    // S10 must be sent later by order
    assertEquals(s10.getFrom(), primary.request().getFrom());
    assertEquals(s10.getTo(), primary.request().getTo());

    PrimaryResponse r2 = new PrimaryResponse(TestItem.range(s2.getFrom(), s2.getTo()), 777);
    fetcher.dataAvailable(primary.request(0), r2);

    verify(model).dataAvailable(s2);

    assertEquals("Third request must be sent", 3, primary.requests.size());
    assertEquals("p50 request must be sent first", 500, primary.request().getFrom());

    // Two requests are delayed (first not, second not because acknowledged). Add 300 ms margin.
    Thread.sleep(2 * (fetcher.getDelayWhenPending() + fetcher.timerExtraDelay)/2 + 300);

    assertEquals("All requests must be sent", 4, primary.requests.size());

    // The expected order of delivery
    assertEquals(20, primary.requests.get(0).getFrom());
    assertEquals(100, primary.requests.get(1).getFrom());
    assertEquals(500, primary.requests.get(2).getFrom());
    assertEquals(200, primary.requests.get(3).getFrom());

    PrimaryResponse r50 = new PrimaryResponse(TestItem.range(s50.getFrom(), s50.getTo()), 777);
    fetcher.dataAvailable(primary.request(2), r50);
    verify(model).dataAvailable(s50);

    PrimaryResponse r20 = new PrimaryResponse(TestItem.range(s20.getFrom(), s20.getTo()), 777);
    fetcher.dataAvailable(primary.request(3), r20);
    verify(model).dataAvailable(s20);

    PrimaryResponse r10 = new PrimaryResponse(TestItem.range(s10.getFrom(), s10.getTo()), 777);
    fetcher.dataAvailable(primary.request(1), r10);
    verify(model).dataAvailable(s10);
  }
}
