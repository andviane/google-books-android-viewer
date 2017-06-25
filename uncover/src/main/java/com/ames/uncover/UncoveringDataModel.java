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

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.ames.uncover.impl.AsyncBridge;
import com.ames.uncover.impl.AvailableSegment;
import com.ames.uncover.impl.DataFetchManager;
import com.ames.uncover.primary.PrimaryDataProvider;
import com.ames.uncover.primary.Query;
import com.ames.uncover.primary.SearchCompleteListener;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Central class of implementation, providing the fast model for viewing from one side and grouped/async interface from
 * another side.
 * <p>
 * The model is itself the DataAvailableListener, but would also propagate the data available event to
 * the view for repainting,
 */
public class UncoveringDataModel<ITEM> {
  private static final String TAG = "Model";


  /**
   * The number of items in the page
   */
  private int pageSize = 10;

  /**
   * The total length of the list presented
   */
  private int size = 0;

  /**
   * The listener of the view or presenter that the model notifies when the data have changed
   * due update arrivals.
   */
  private RecyclerView.Adapter adapter;

  /**
   * The delegate where the model requests fetching the new data when needed.
   */
  private DataFetchManager dataFetcher;

  /**
   * Linear layout manager. It is not required to set this component, but if available,
   * it is used to discard pending fetch requests already outside the visible area.
   */
  private LinearLayoutManager layoutManager;

  /**
   * Already available data items, maps page number to the data array.
   */
  private Map<Integer, AvailableSegment<ITEM>> data = new ConcurrentHashMap<>();

  /**
   * The "empty" item indicating data have been requested and now are in the process of loading.
   */
  private ITEM loadingPlaceHolder;

  private Query query;

  private SearchCompleteListener searchCompleteListener;

  /**
   * Internal method used by data fetch manager
   */
  public boolean isFirstQueryResult() {
    return firstQueryResult;
  }

  /**
   * Tracks if the current "data available" call is the first result for this query.
   * This allow to have the "search complete" listener for tasks like hiding the progress bar.
   */
  private boolean firstQueryResult = true;

  public void setPrimaryDataProvider(PrimaryDataProvider provider) {
    dataFetcher = new DataFetchManager();
    final AsyncBridge<ITEM> bridge = new AsyncBridge<>(provider);
    this.dataFetcher.setDataFetcher(bridge);
    this.dataFetcher.setDataModel(this);
    if (query != null) {
      requestPage(0);
    }
  }

  /**
   * Set the query for the data, shown by this model. The query is provided to your
   * primary data fetcher when requesting the data. After the new query is set, the
   * model is invalidated, requesting all data to be newly loaded.
   *
   * @param query the query to set
   */
  public void setQuery(Query query) {
    reset();
    this.query = query;

    // Automatically request the first block. Once fetched, the listeners will fire.
    if (dataFetcher != null) {
      requestPage(0);
    }
  }

  /**
   * Get the configured paga size
   *
   * @return
   */
  public int getPageSize() {
    return pageSize;
  }

  /**
   * Set the configured page size (the number of items wanted in requests). This must be set before
   * the model is used and cannot be modified later.
   *
   * @param pageSize the number of items per request
   */
  public UncoveringDataModel setPageSize(int pageSize) {
    this.pageSize = pageSize;
    return this;
  }

  /**
   * Get the number of the page for the given position
   *
   * @param position the position in the model
   *
   * @return the number of the page
   */
  public int getPage(int position) {
    return (position / pageSize);
  }

  /**
   * Get the item returned to indicate data are still being loaded. If not set, null is used.
   */
  public ITEM getLoadingPlaceHolder() {
    return loadingPlaceHolder;
  }

  /**
   * Set the item returned to indicate data are still being loaded. If not set, null is used.
   */
  public UncoveringDataModel setLoadingPlaceHolder(ITEM loadingPlaceHolder) {
    this.loadingPlaceHolder = loadingPlaceHolder;
    return this;
  }

  /**
   * Get item at the given position. If the value is not known, returns loading place holder and starts
   * fetching the data segment in the background.
   *
   * @param position position of the item to get
   * @return item at the given position.
   */
  public ITEM getItem(int position) {
    int page = getPage(position);
    AvailableSegment<ITEM> segment = data.get(page);
    if (segment != null) {
      return segment.get(position);
    }

    requestPage(page);
    return loadingPlaceHolder;
  }

  /**
   * Request the content for the given position. Expanding strategies must be applied and
   * then the data fetcher must be invoked.
   *
   * @param page the page currently missing data.
   */
  protected void requestPage(int page) {
    if (dataFetcher != null && !dataFetcher.alreadyFetching(page)) {
      dataFetcher.requestData(page);
    }
  }

  /**
   * Internal method.
   */
  public UncoveringDataModel setDataFetcher(DataFetchManager dataFetcher) {
    this.dataFetcher = dataFetcher;
    return this;
  }

  /**
   * Get the number of items in the model. If not explicitly set, applies the best guess.
   *
   * @return number of items in the model.
   */
  public int size() {
    return size;
  }

  /**
   * Invoked by data fetcher when data become available.
   *
   * @param segment - the segment of now newly available data.
   */
  public void dataAvailable(AvailableSegment<ITEM> segment) {
    data.put(segment.getPage(), segment);

    size = Math.max(size, segment.getMaxIndex());
    if (adapter != null) {
      adapter.notifyItemRangeChanged(segment.getFrom(),
         segment.getTo() - segment.getFrom());
    }
    if (firstQueryResult && searchCompleteListener != null) {
      searchCompleteListener.onQuerySearchComplete(query);
    }
    firstQueryResult = false;
  }

  /**
   * Reset the model, making the data set empty.
   */
  public void reset() {
    firstQueryResult = true;
    size = 0;
    data.clear();
    if (dataFetcher != null) {
      dataFetcher.reset();
    }
  }

  /**
   * This can be called on low memory conditions to drop the cached data.
   */
  public void lowMemory() {
    data.clear();
    dataFetcher.lowMemory();
  }

  public SearchCompleteListener getSearchCompleteListener() {
    return searchCompleteListener;
  }

  /**
   * Set the search listener that is fired when the data fetcher returns the first results. Can be
   * used to hide the progress indicator, enable submit query button and things the like.
   */
  public UncoveringDataModel setSearchCompleteListener(SearchCompleteListener searchCompleteListener) {
    this.searchCompleteListener = searchCompleteListener;
    return this;
  }

  /**
   * Get the currently set query
   */
  public Query getQuery() {
    return query;
  }

  /**
   * Optionally set layout manager to handle the expired areas.
   */
  public UncoveringDataModel setLayoutManager(LinearLayoutManager layoutManager) {
    this.layoutManager = layoutManager;
    return this;
  }

  /**
   * Internal method used by data fetch manager.
   */
  public LinearLayoutManager getLayoutManager() {
    return layoutManager;
  }

  /**
   * Set the state that is required to support Android life cycles.
   * If items are serializable, they are stored as part of state.
   */
  public void setState(Serializable state) {
    ByteArrayInputStream bin = new ByteArrayInputStream((byte[]) state);
    try {
      ObjectInputStream oin = new ObjectInputStream(bin);
      pageSize = oin.readInt();
      size = oin.readInt();
      query = (Query) oin.readObject();
      loadingPlaceHolder = (ITEM) oin.readObject();
      firstQueryResult = oin.readBoolean();
      data = (Map<Integer, AvailableSegment<ITEM>>) oin.readObject();
      for (AvailableSegment<ITEM> s : data.values()) {
        s.setModel(this);
      }
      oin.close();
    } catch (IOException e) {
      Log.e(TAG, "Failed to read the state", e);
    } catch (ClassNotFoundException e) {
      Log.e(TAG, "Failed to read the state", e);
    }
  }

  /**
   * Get the state that is required to support Android life cycles.
   * If items are serializable, they are stored as part of state.
   */
  public Serializable getState() {
    boolean elementsSerializable;

    try {
      elementsSerializable =
         !data.isEmpty() &&
            data.values().iterator().next() instanceof Serializable;
    } catch (Exception e) {
      Log.e(TAG, "Failed to say if elements are serializable");
      elementsSerializable = false;
    }

    ByteArrayOutputStream bout = new ByteArrayOutputStream();
    try {
      ObjectOutputStream oout = new ObjectOutputStream(bout);
      oout.writeInt(pageSize);
      oout.writeInt(size);
      oout.writeObject(query);
      oout.writeObject(loadingPlaceHolder);
      oout.writeBoolean(firstQueryResult);
      if (elementsSerializable) {
        oout.writeObject(data);
      } else {
        oout.writeObject(new ConcurrentHashMap<Integer, ITEM>());
      }
      oout.close();
    } catch (IOException e) {
      Log.e(TAG, "Failed to write the state", e);
    }
    return bout.toByteArray();
  }

  /**
   * The main method to configure recycler view, its adapter and this model to work together. This
   * method must be called once during the initialization. It wires all listeners and adds some
   * other internal classes
   *
   * @param recyclerView the view that will work with this model
   * @param adapter the adapter that will work with this model
   */
  public void install(final RecyclerView recyclerView, final RecyclerView.Adapter adapter) {
    recyclerView.setAdapter(adapter);
    this.adapter = adapter;

    final LinearLayoutManager lm = new LinearLayoutManager(recyclerView.getContext());
    recyclerView.setLayoutManager(lm);
    setLayoutManager(lm);
    recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
      @Override
      public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        dataFetcher.notifyVisibleArea(lm.findFirstCompletelyVisibleItemPosition(),
           lm.findLastVisibleItemPosition() + 1);
      }
    });
  }
}
