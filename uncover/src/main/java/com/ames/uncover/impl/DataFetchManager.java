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

import android.support.annotation.NonNull;
import android.util.Log;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

import com.ames.uncover.UncoveringDataModel;
import com.ames.uncover.primary.PrimaryRequest;
import com.ames.uncover.primary.PrimaryResponse;

/**
 * Holds the data fetch queue and performs all grouping optimizations for data fetching. A layer between
 * data model and data fetcher, this class is responsible for all advanced logic.
 */
public class DataFetchManager<ITEM> implements DataAvailableListener<ITEM> {
  /**
   * Time to delay the next fetch when the current one is in processing.
   */
  protected long delayWhenPending = 5_000L;
  /**
   * Make timer to fire slightly later, to make sure delayWhenPending passed
   */
  protected long timerExtraDelay = 100L;
  private DataFetcher dataFetcher;
  private UncoveringDataModel<ITEM> dataModel;
  private int channels = 2;
  /**
   * The time since when the last fetching is in processing, or 0 in no such.
   */
  protected long[] fetchInProcessing = new long[channels];
  /**
   * Contains areas where data are or should be provided, either by pending or by
   * the already served requests. Prevents re-fetching the same data from
   * the server.
   */
  private Map<Integer, AvailableSegment> coverage = new ConcurrentHashMap<>();
  /**
   * We need to schedule update for the case if the request sent never arrives.
   */
  private Timer timer;

  /**
   * "Inverted" queue (most recently added request are serviced first)
   */
  private LinkedList<PrimaryRequest> queue = new LinkedList<>();

  public DataFetchManager() {
    timer = new Timer(true); // set daemon
  }

  public DataFetcher getDataFetcher() {
    return dataFetcher;
  }

  public DataFetchManager setDataFetcher(DataFetcher dataFetcher) {
    this.dataFetcher = dataFetcher;
    dataFetcher.setOnDataAvailableListener(this);
    return this;
  }

  public UncoveringDataModel<ITEM> getDataModel() {
    return dataModel;
  }

  public DataFetchManager setDataModel(UncoveringDataModel<ITEM> dataModel) {
    this.dataModel = dataModel;
    return this;
  }

  /**
   * Request to start loading data for the given position. The model will call alreadyFetching(page)
   * to be sure same data are not requested repeatedly.
   */
  public synchronized void requestData(int page) {
    AvailableSegment<ITEM> segment = new AvailableSegment<>(dataModel, page);
    PrimaryRequest request = new PrimaryRequest(segment.getFrom(), segment.getTo(), dataModel.getQuery(),
       dataModel.isFirstQueryResult());
    request.setPage(segment.getPage());

    coverage.put(segment.getPage(), segment);
    queue.push(request);

    int c = bestChannel();
    if (now() - fetchInProcessing[c] > delayWhenPending) {
      servePending();
    } else {
      // deferred fetch if none is already scheduled
      scheduleServePending();
    }
  }

  protected long now() {
    return System.currentTimeMillis();
  }

  public long getDelayWhenPending() {
    return delayWhenPending;
  }

  public DataFetchManager setDelayWhenPending(long delayWhenPending) {
    this.delayWhenPending = delayWhenPending;
    return this;
  }

  /**
   * Check if this page is not already being fetched.
   */
  public boolean alreadyFetching(int page) {
    return coverage.containsKey(page);
  }

  /**
   * Callback made by data fetcher when data becomes available.
   */
  @Override
  public synchronized void dataAvailable(PrimaryRequest request, PrimaryResponse<ITEM> data) {
    fetchInProcessing[request.getChannel()] = 0L;
    try {
      AvailableSegment<ITEM> segment = getSegmentForStartingPosition(request.getFrom());
      if (data.getTotalItems() != null) {
        segment.dataProvided(data.getData(),
           data.getTotalItems().intValue());
      } else {
        segment.dataProvided(data.getData());
      }
    } finally {
      // It is important to continue processing the queued items
      // regardless that.
      servePending();
    }
  }

  /**
   * Notify the area that is currently visible. This can be called at any
   * time to discard pending requests outside the visible area.
   */
  @Override
  public void notifyVisibleArea(int fromInclusive, int endExclusive) {
    Iterator<PrimaryRequest> iter = queue.iterator();
    while (iter.hasNext()) {
      PrimaryRequest request = iter.next();
      if (request.getFrom() >= endExclusive || request.getTo() < fromInclusive) {
        coverage.remove(request.getPage());
        iter.remove();
      }
    }
  }

  @Override
  public synchronized void dataUnavailable(PrimaryRequest request) {
    fetchInProcessing[request.getChannel()] = 0L;
    servePending();
  }

  @NonNull
  private AvailableSegment<ITEM> getSegmentForStartingPosition(int from) {
    int page = dataModel.getPage(from);
    AvailableSegment<ITEM> segment = coverage.get(page);
    if (segment == null) {
      // Proactive fetcher supported
      segment = new AvailableSegment<>(dataModel, page);
      coverage.put(page, segment);
    }
    return segment;
  }

  public void reset() {
    queue.clear();
    dataFetcher = dataFetcher.reset();
    coverage.clear();
    Arrays.fill(fetchInProcessing, 0L);
  }

  /**
   * Serve pending tasks if the previous tasks have been already acknowledged
   * or takes too long. Only send one at time.
   * <p>
   * This method is synchronized because also time thread may call it.
   */
  protected synchronized void servePending() {
    if (!queue.isEmpty()) {
      int c = bestChannel();
      if (now() - fetchInProcessing[c] > delayWhenPending) {
        // Nothing pending or takes too long, send next
        fetchInProcessing[c] = now();
        PrimaryRequest request = queue.pop();
        request.setChannel(c);
        System.out.println("Serve pending " + queue.size() + " " + request + " on channel " + c);
        request.setSent(now());
        dataFetcher.requestData(request);
      } else {
        // postpone further
        scheduleServePending();
      }
    }
  }

  private void scheduleServePending() {
    timer.schedule(new TimerTask() {
      @Override
      public void run() {
        servePending();
      }
    }, delayWhenPending + timerExtraDelay);
  }

  /**
   * Get the channel best suitable for the task submission
   */
  private int bestChannel() {
    int p = 0;
    for (int i = 0; i < channels; i++) {
      if (fetchInProcessing[i] < fetchInProcessing[p]) {
        p = i;
      }
    }
    return p;
  }

  public DataFetchManager<ITEM> setThreads(int threads) {
    this.channels = threads;
    this.fetchInProcessing = new long[threads];
    return this;
  }

  public int getTreads() {
    return channels;
  }

  /**
   * Drop cached items
   */
  public void lowMemory() {
    coverage.clear();
  }
}
