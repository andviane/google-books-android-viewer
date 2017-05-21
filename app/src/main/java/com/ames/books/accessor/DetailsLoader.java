package com.ames.books.accessor;


import android.os.AsyncTask;
import android.util.Log;

import com.ames.books.BuildConfig;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.services.books.Books;
import com.google.api.services.books.model.Volume;

import java.io.IOException;

/**
 * The search module that takes the search query. It takes the search query and properly notifies the BookListActivity about the query results.
 */
public class DetailsLoader {
  private static final String TAG = "books.search.details";
  final DetailsLoadingResultListener listener;

  public DetailsLoader(DetailsLoadingResultListener listener) {
    this.listener = listener;
  }

  public void doSearch(final Volume query) {
    new AsyncTask<Volume, Void, Volume>() {

      @Override
      protected Volume doInBackground(Volume... params) {
        return search(params[0]);
      }

      @Override
      protected void onPostExecute(Volume book) {
        // Merge also with the previous result. We are on UI thread, safe to merge.
        query.putAll(book);
        listener.onDetailsLoaded(book);
      }
    }.execute(query);
  }

  /**
   * Main search method, runs outside UI thread.
   */
  private Volume search(Volume query) {
    Books books = new Books.Builder(AndroidHttp.newCompatibleTransport(), AndroidJsonFactory.getDefaultInstance(), null)
       .setApplicationName(BuildConfig.APPLICATION_ID)
       .build();

    try {
      // Executes the query
      Books.Volumes.Get get = books.volumes().get(query.getId());
      return get.execute();
    } catch (IOException e) {
      Log.e(TAG, "IO ex", e);
      return query;
    }
  }
}
