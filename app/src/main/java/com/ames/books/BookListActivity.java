package com.ames.books;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.ComponentCallbacks2;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;

import com.ames.books.presenter.ShowDetailsListener;
import com.ames.books.struct.Book;

/**
 * The main activity of the application
 */
public class BookListActivity extends Activity implements ShowDetailsListener {
  private static final String TAG = "books.Booklist";

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_book_list);
    applyState(savedInstanceState);
  }

  private void applyState(Bundle savedInstanceState) {
    if (savedInstanceState != null) {
      // hot start
      boolean list = savedInstanceState.getBoolean("a.list", true);
      Log.d(TAG, "list " + list);
      if (list) {
        showList();
      } else {
        showDetails(null, null);
      }
    } else {
      showList(); // cold start
    }
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    FragmentManager fragmentManager = getFragmentManager();
    BookListFragment lf = (BookListFragment) fragmentManager.findFragmentById(R.id.book_list);
    outState.putBoolean("a.list", lf.isVisible());
  }

  @Override
  protected void onRestoreInstanceState(Bundle savedInstanceState) {
    super.onRestoreInstanceState(savedInstanceState);
    applyState(savedInstanceState);
  }

  public void showList() {
    final FragmentManager fragmentManager = getFragmentManager();
    FragmentTransaction ft = fragmentManager.beginTransaction();
    ft.show(fragmentManager.findFragmentById(R.id.book_list));
    ft.hide(fragmentManager.findFragmentById(R.id.book_details));
    ft.commit();
  }

  /**
   * Hide list, show details and instruct the details view to show the selected book.
   */
  @Override
  public void showDetails(Book book, Drawable thumb) {
    final FragmentManager fragmentManager = getFragmentManager();
    FragmentTransaction ft = fragmentManager.beginTransaction();
    BookDetailsFragment details = (BookDetailsFragment) fragmentManager.findFragmentById(R.id.book_details);

    if (book != null) {
      // If null passed, we only configure fragment transaction here.
      details.showDetails(book, thumb);
    }

    ft.show(details);
    ft.hide(fragmentManager.findFragmentById(R.id.book_list));
    ft.addToBackStack("details"); // Use the back button to return to the search list view.

    ft.commit();
  }

  @Override
  public void onTrimMemory(int level) {
    switch (level) {
      case ComponentCallbacks2.TRIM_MEMORY_RUNNING_MODERATE:
      case ComponentCallbacks2.TRIM_MEMORY_RUNNING_LOW:
      case ComponentCallbacks2.TRIM_MEMORY_RUNNING_CRITICAL:

        Log.d(TAG, "Memory trimmed");
        FragmentManager fragmentManager = getFragmentManager();
        BookListFragment lf = (BookListFragment) fragmentManager.findFragmentById(R.id.book_list);
        lf.adapter.getModel().lowMemory();

      default:
        break;
    }
  }
}
