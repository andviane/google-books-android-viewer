package com.ames.books.data;

import com.google.api.services.books.model.Volume;
import com.google.api.services.books.model.Volumes;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * This is a "classic" JUnit test that normally should not use anything
 * Android specific. Such tests are great for custom library modules shared between Android
 * and server sides.
 */
public class SearchBlockTest {

  Volumes volumes;
  ArrayList<Volume> items = mock(ArrayList.class);

  @Before
  public void setup() {
    when(items.size()).thenReturn(5);
    volumes = new Volumes();
    volumes.setItems(items);
  }

  @Test
  public void testGet() {
    SearchBlock block = SearchBlockPendingTest.searchBlock(volumes, 10); // [10 .. 15[
    Volume v1 = new Volume();
    Volume v2 = new Volume();

    v1.setId("id0");
    v2.setId("id4");

    when(items.get(0)).thenReturn(v1);
    when(items.get(4)).thenReturn(v2);

    assertEquals(v1.getId(), block.get(10).getId());
    assertEquals(v2.getId(), block.get(14).getId());
  }

  @Test
  public void testInRange() {
    SearchBlock block = SearchBlockPendingTest.searchBlock(volumes, 10); // [10 .. 15[
    Volume v1 = new Volume();
    Volume v2 = new Volume();
    when(items.get(0)).thenReturn(v1);
    when(items.get(4)).thenReturn(v2);

    assertTrue(block.inRange(10));
    assertTrue(block.inRange(14));
    assertFalse(block.inRange(15));
    assertFalse(block.inRange(9));
  }
}
