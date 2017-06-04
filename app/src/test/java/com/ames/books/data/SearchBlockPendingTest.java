package com.ames.books.data;

import com.ames.books.struct.Book;
import com.ames.books.struct.Books;
import com.google.api.services.books.model.Volume;
import com.google.api.services.books.model.Volumes;

import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * This is a "classic" JUnit test that normally should not use anything
 * Android specific. Such tests are great for custom library modules shared between Android
 * and server sides.
 */
public class SearchBlockPendingTest {

  @Test
  public void testRangeCheck() {
    SearchBlockPending block = new SearchBlockPending(2, 10);

    assertTrue(block.inRange(2));
    assertTrue(block.inRange(5));
    assertTrue(block.inRange(9));
    assertFalse(block.inRange(10));
  }

  @Test
  public void testBlockCheck() {
    Volumes volumes = new Volumes();

    ArrayList items = mock(ArrayList.class);
    when(items.size()).thenReturn(5); // make 5 items size
    volumes.setItems(items);

    SearchBlock block = searchBlock(volumes, 10); // [10 .. 15[
    SearchBlockPending low = new SearchBlockPending(1, 12);
    SearchBlockPending exact = new SearchBlockPending(10, 15);
    SearchBlockPending high = new SearchBlockPending(12, 20);

    assertFalse(low.isCoveredBy(block));
    assertTrue(exact.isCoveredBy(block));
    assertFalse(high.isCoveredBy(block));
  }

  public static SearchBlock searchBlock(Volumes v, int ofs) {
    Books books = new Books(v.getItems().size());
    for (int i = 0; i < v.getItems().size(); i++) {
      Volume from = v.getItems().get(i);
      String id = "id" + i;

      if (from != null && from.getId() != null) {
        id = from.getId();
      }
      Book book = new Book(id, "t", null, 0, "");
      books.add(book);
    }
    return new SearchBlock(books, v.getTotalItems(), ofs);
  }
}
