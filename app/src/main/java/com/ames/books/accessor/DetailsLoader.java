package com.ames.books.accessor;


import android.os.AsyncTask;
import android.util.Log;

import com.ames.books.BuildConfig;
import com.ames.books.struct.Book;
import com.ames.books.struct.BookDetails;
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

  public void doSearch(final Book query) {
    new AsyncTask<Book, Void, Book>() {

      @Override
      protected Book doInBackground(Book... params) {
        return search(params[0]);
      }

      @Override
      protected void onPostExecute(Book book) {
        listener.onDetailsLoaded(book);
      }
    }.execute(query);
  }

  /**
   * Main search method, runs outside UI thread.
   */
  private Book search(Book query) {
    Books books = new Books.Builder(AndroidHttp.newCompatibleTransport(), AndroidJsonFactory.getDefaultInstance(), null)
       .setApplicationName(BuildConfig.APPLICATION_ID)
       .build();

    try {
      // Executes the query
      Log.d(TAG, "Fetching details for "+query.getId());
      Books.Volumes.Get get = books.volumes().get(query.getId());
      final Volume execute = get.execute();
      BookDetails details = new BookDetails(execute);
      query.setDetails(details);
      Log.d(TAG, "Details loaded " + details.getSubtitle());
      return query;
    } catch (IOException e) {
      Log.e(TAG, "IO ex", e);
      return query;
    }
  }
}
