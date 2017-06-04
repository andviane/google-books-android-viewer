package com.ames.books.data;

import android.support.test.runner.AndroidJUnit4;

import com.ames.books.accessor.AsyncSearcher;
import com.ames.books.struct.Book;
import com.ames.books.struct.Books;
import com.google.api.services.books.model.Volume;
import com.google.api.services.books.model.Volumes;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

/**
 * This is example of simple instrumentation test that uses some Android classes
 * not mocked for JUnit.
 */
@RunWith(AndroidJUnit4.class)
public class BookDataTest {
  Book a1 = new Book("a1", "ta1", null, 0, "");
  Book a2 = new Book("a2", "ta2", null, 0, "");
  Book b1 = new Book("b1", "tb1", null, 0, "");

  Books v1;
  Books v2;

  SearchBlock s1;
  SearchBlock s2;

  DataChangeListener listener = mock(DataChangeListener.class);

  AsyncSearcher searcher = mock(AsyncSearcher.class);

  @Before
  public void setup() {

    // Two data sets, size 5
    v1 = new Books(5);
    v1.add(a1);
    v1.add(a2);

    while (v1.size() < 5) {
      v1.add(null);
    }

    v2 = new Books(5);
    v2.add(b1);
    while (v2.size() < 5) {
      v2.add(null);
    }

    // Make the two blocks lying one next another in intervals 0 .. 5 and 5 .. 10
    s1 = searchBlock(v1, 100, 0);
    s2 = searchBlock(v2, 4, 5); // Less, and 100 must be returned.
  }

  public static SearchBlock searchBlock(Books books, int total, int ofs) {
    return new SearchBlock(books, total, ofs);
  }

  @Test
  public void testGet() {
    BookData data = new BookData(listener);
    data.setBookList(s1, "query");
    data.extendBookList(s2);

    assertSame(a1, data.get(0));
    assertSame(a2, data.get(1));
    assertSame(b1, data.get(5));
  }

  @Test
  public void testOnDemandSearcherCalls() {
    BookData data = new BookData(listener, searcher);

    when(searcher.getItemsPerRequest()).thenReturn(5);

    data.setBookList(s1, "query");

    // Item 7 not available, requresting
    assertNull(data.get(7));
    verify(searcher).doSearch("query", 7);

    // Item 8 also not available, but request must be suppressed as the
    // previous call will provide the value.
    assertNull(data.get(8));
    verify(searcher, atLeastOnce()).getItemsPerRequest();
    verifyNoMoreInteractions(searcher);

    // Item 100 is outside the expected result range and also will be requested
    assertNull(data.get(100));
    verify(searcher, atLeastOnce()).getItemsPerRequest();
    verify(searcher).doSearch("query", 100);
    verifyNoMoreInteractions(searcher);
  }

}
