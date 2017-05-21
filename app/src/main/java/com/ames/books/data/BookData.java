package com.ames.books.data;


import android.util.Log;

import com.ames.books.accessor.AsyncSearcher;
import com.google.api.services.books.model.Volume;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * The data storage that feeds the book list adapter. The storage supports chunked updates that are results of the subsequent queries. This class
 * also cares about enough data to be present and sends requests to fetch more.
 */
public class BookData implements SearchResultListener {
  private static String TAG = "books.data";

  private final ArrayList<SearchBlock> volumes = new ArrayList<>();

  private final ArrayList<SearchBlockPending> searches = new ArrayList<>();

  private final AsyncSearcher searcher;

  /**
   * As this field is included on every volume, the largest value is taken.
   */
  private int size;

  /**
   * We need to remember query as this class sends repetitive requests.
   */
  private String query;

  private DataChangeListener listener;

  /**
   * Use the state serialization version number to prevent attempt to load data
   * for the wrong version.
   */
  private static final long stateSerializationVersion = 1L;

  public BookData(DataChangeListener listener) {
    this.listener = listener;
    this.searcher = new AsyncSearcher(this);
  }

  public BookData(DataChangeListener listener, AsyncSearcher searcher) {
    this.listener = listener;
    this.searcher = searcher;
  }

  /**
   * Get book at position
   */
  public Volume get(int position) {
    for (SearchBlock block : volumes) {
      Volume vol = block.get(position);
      if (vol != null) {
        return vol;
      }
    }
    acceptRequirement(position);
    return null;
  }

  public void setBookList(SearchBlock block, String query) {
    volumes.clear();
    volumes.add(block);
    size = block.getTotalItems();
    this.query = query;
    searches.clear();
    listener.notifyDataChanged();
  }

  /**
   * Ge the total number of items.
   */
  public int size() {
    return size;
  }

  /**
   * Add additional fetched search block.
   */
  protected synchronized void extendBookList(SearchBlock block) {
    size = Math.max(size, block.getTotalItems());
    volumes.add(block);

    // Remove the pendings block that is now covered by real data
    Iterator<SearchBlockPending> pendings = searches.iterator();
    while (pendings.hasNext()) {
      if (pendings.next().isCoveredBy(block)) {
        pendings.remove();
      }
    }
    listener.notifyRegionChanged(block.getFrom(), block.getTo());
  }

  /**
   * Accept requirement we need data for this position.
   */
  private synchronized void acceptRequirement(int position) {
    Log.d(TAG, "Need more data from " + position + " form " + query);
    if (!alreadyPending(position)) {
      int pos = getRecommendedOffset(position);
      Log.d(TAG, position + " is new requirement, initiating new search from " + pos);
      SearchBlockPending pending = new SearchBlockPending(pos, pos + searcher.getItemsPerRequest());
      searches.add(pending);
      searcher.doSearch(query, position);
    } else {
      Log.d(TAG, position + " is already pending");
    }
  }

  /**
   * Check maybe we are already searching for data under this position
   */
  private boolean alreadyPending(int position) {
    for (SearchBlockPending pending : searches) {
      if (pending.inRange(position)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Get the recommended offset for the given position
   */
  private int getRecommendedOffset(int position) {
    // Round so that chunks would fit nicely side by side.
    return (position / searcher.getItemsPerRequest()) * searcher.getItemsPerRequest();
  }

  @Override
  public void onQueryResult(SearchBlock books, String query) {
    Log.d(TAG, "New query result " + books.getFrom() + ".. " + books.getTo() + " on " + query);
    if (!query.equals(this.query)) {
      // new list
      setBookList(books, query);
    } else {
      // extending list
      extendBookList(books);
    }
  }

  /**
   * Apply the earlier state. The passed object must be previously returned
   * by getState on the same app installation (long term storage not
   * expected or supported)
   */
  public void setState(Serializable state) {
    size = 0;
    byte[] bytes = (byte[]) state;
    if (bytes == null || bytes.length == 0) {
      return; // Broken state, nothing to do.
    }

    Log.d(TAG, "Applyiing the state, size " + bytes.length);

    try {
      DataInputStream din = new DataInputStream(new ByteArrayInputStream(bytes));
      long version = din.readLong();

      if (version != stateSerializationVersion) {
        Log.d(TAG, "Saved state version mismatch " + version + " ours " + stateSerializationVersion);
        return;
      }

      query = din.readUTF();

      int vols = din.readInt();
      for (int v = 0; v < vols; v++) {
        SearchBlock block = new SearchBlock();
        block.readState(din);
        volumes.add(block);
        size = Math.max(size, block.getTotalItems());
      }
    } catch (IOException e) {
      Log.e(TAG, "Failed to set the state", e);
    }
  }

  /**
   * Get the state that may be applied to show the same data set. The returned
   * object can be later passed to setState
   */
  public Serializable getState() {
    byte[] output;
    try {
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      DataOutputStream dout = new DataOutputStream((out));
      dout.writeLong(stateSerializationVersion);
      if (query != null) {
        dout.writeUTF(query);
      } else {
        dout.writeUTF("");
      }
      dout.writeInt(volumes.size());
      for (SearchBlock block : volumes) {
        block.writeState(dout);
      }
      dout.close();
      output = out.toByteArray();
      Log.d(TAG, "Stated saved " + output.length + " bytes");
    } catch (IOException e) {
      Log.e(TAG, "Failed to save the state", e);
      output = new byte[0];
    }
    return output;
  }
}
