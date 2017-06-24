package com.ames.books.accessor;

import android.util.Log;

import com.ames.books.BuildConfig;
import com.ames.books.struct.Book;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.services.books.Books;
import com.google.api.services.books.BooksRequestInitializer;
import com.google.api.services.books.model.Volume;
import com.google.api.services.books.model.Volumes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.ames.uncover.primary.PrimaryDataProvider;
import com.ames.uncover.primary.PrimaryRequest;
import com.ames.uncover.primary.PrimaryResponse;

/**
 * The search module that takes the search query. It takes the search query and properly notifies the BookListActivity about the query results.
 */
public class BookDataProvider implements PrimaryDataProvider {
  // The key is not fixed to the app signature. It is the official API key for this APP. Still maybe better to provide.
  public static String API_KEY = "AIzaSyCjWRXuTr0xFXu1j9Qf3HOWSL-vIemEJE4";

  private static final String TAG = "books.search.list";

  /**
   * Main search method, runs outside UI thread.
   */
  @Override
  public PrimaryResponse fetch(PrimaryRequest request) {
    Books books = new Books.Builder(AndroidHttp.newCompatibleTransport(), AndroidJsonFactory.getDefaultInstance(), null)
       .setApplicationName(BuildConfig.APPLICATION_ID)
       .setGoogleClientRequestInitializer(new BooksRequestInitializer(API_KEY))
       .build();

    try {
      // Executes the query
      Books.Volumes.List list = books.volumes().list(request.getQuery().getQueryString());
      list.setMaxResults(Long.valueOf(request.getTo() - request.getFrom()));
      list.setStartIndex(Long.valueOf(request.getFrom()));
      list.setFields("totalItems,items(volumeInfo(title,authors,pageCount,imageLinks/smallThumbnail),id)");

      Volumes execution = list.execute();
      ArrayList<Book> bookList = convert(execution);
      return new PrimaryResponse<>(bookList, execution.getTotalItems());
    } catch (IOException e) {
      Log.e(TAG, "IO ex", e);
      return null;
    }
  }

  private ArrayList<Book> convert(Volumes volumes) {
    if (volumes != null && volumes.getItems() != null) {
      List<Volume> vols = volumes.getItems();
      ArrayList<Book> books = new ArrayList<>(vols.size());
      for (Volume vol : vols) {
        books.add(new Book(vol));
      }
      return books;
    } else {
      return new ArrayList<>();
    }
  }

}
