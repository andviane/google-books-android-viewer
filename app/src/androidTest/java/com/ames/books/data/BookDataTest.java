package com.ames.books.data;

import android.support.test.runner.AndroidJUnit4;

import com.ames.books.accessor.AsyncSearcher;
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
  Volume a1 = new Volume();
  Volume a2 = new Volume();
  Volume b1 = new Volume();

  Volumes v1;
  Volumes v2;

  ArrayList<Volume> it1 = mock(ArrayList.class);
  ArrayList<Volume> it2 = mock(ArrayList.class);

  SearchBlock s1;
  SearchBlock s2;

  DataChangeListener listener = mock(DataChangeListener.class);

  AsyncSearcher searcher = mock(AsyncSearcher.class);

  @Before
  public void setup() {
    // Two data sets, size 5
    when(it1.size()).thenReturn(5);
    when(it2.size()).thenReturn(5);

    when(it1.get(0)).thenReturn(a1);
    when(it1.get(1)).thenReturn(a2);
    when(it2.get(0)).thenReturn(b1);

    v1 = new Volumes();
    v1.setItems(it1);

    v2 = new Volumes();
    v2.setItems(it2);

    v1.setTotalItems(100);
    v2.setTotalItems(4); // Less, and 100 must be returned.

    // Make the two blocks lying one next another in intervals 0 .. 5 and 5 .. 10
    s1 = new SearchBlock(v1, 0);
    s2 = new SearchBlock(v2, 5);
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
