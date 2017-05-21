package com.ames.books.data;

import android.util.Log;

import com.google.api.services.books.model.Volume;
import com.google.api.services.books.model.Volumes;
import com.google.common.base.Strings;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Represents a single search result. Volumes data structure, required by Google, does not contain the query range.
 */
public class SearchBlock {
  private static final String TAG = "books.sblock";

  /**
   * The query result
   */
  private Volumes volumes;

  /**
   * From, inclusive
   */
  private int from;

  /**
   * To, exclusive
   */
  private int to;

  public int getFrom() {
    return from;
  }

  public int getTo() {
    return to;
  }

  public SearchBlock(Volumes volumes, int offset) {
    this.volumes = volumes;
    this.from = offset;
    this.to = volumes.getItems().size() + offset;
  }

  public SearchBlock() {
    // For serialization
  }

  /**
   * Get item at the absolute position, or null if this block does not cover the location.
   */
  public Volume get(int position) {
    if (inRange(position)) {
      return volumes.getItems().get(position - from);
    }
    return null;
  }

  public boolean inRange(int position) {
    return position >= from && position < to;
  }

  /**
   * Get the total number of items as represented by this volume
   *
   * @return total number of items. If not reported by Google for some
   * reason, return the safe lower boundary.
   */
  public int getTotalItems() {
    Integer count = volumes.getTotalItems();
    int n;
    if (count == null) {
      n = 0;
    } else {
      n = count.intValue();
    }

    if (n == 0) {
      // Safe boundary, that much really must be.
      n = from + volumes.getItems().size();
    }
    return n;
  }

  public void writeState(DataOutputStream dout) throws IOException {
    dout.writeInt(from);
    dout.writeInt(to);
    dout.writeInt(volumes.getTotalItems());
    dout.writeInt(volumes.getItems().size());

    // Only store initial query fields.
    for (Volume vol : volumes.getItems()) {
      //"totalItems,items(volumeInfo(title,authors,pageCount,imageLinks/smallThumbnail),selfLink,id)")
      final Volume.VolumeInfo volInfo = vol.getVolumeInfo();
      dout.writeUTF(Strings.nullToEmpty(volInfo.getTitle()));
      if (volInfo.getAuthors() == null) {
        dout.writeInt(0); // no authors
      } else {
        dout.writeInt(volInfo.getAuthors().size());
        for (int n = 0; n < volInfo.getAuthors().size(); n++) {
          dout.writeUTF(volInfo.getAuthors().get(n));
        }
      }
      dout.writeInt(volInfo.getPageCount() == null ? 0 : volInfo.getPageCount());
      dout.writeUTF(volInfo.getImageLinks() == null ? "" : Strings.nullToEmpty(volInfo.getImageLinks().getSmallThumbnail()));
      dout.writeUTF(Strings.nullToEmpty(vol.getSelfLink()));
      dout.writeUTF(Strings.nullToEmpty(vol.getId()));
    }
  }

  public void readState(DataInputStream din) throws IOException {
    volumes = new Volumes();

    from = din.readInt();
    to = din.readInt();
    volumes.setTotalItems(din.readInt());
    int size = din.readInt();

    ArrayList<Volume> list = new ArrayList<>(size);
    for (int i = 0; i < size; i++) {
      Volume volume = new Volume();

      Volume.VolumeInfo info = new Volume.VolumeInfo();
      info.setTitle(din.readUTF());
      int na = din.readInt();
      ArrayList<String> authors = new ArrayList<>(na);
      for (int ia = 0; ia < na; ia++) {
        authors.add(din.readUTF());
      }
      info.setAuthors(authors);
      info.setPageCount(din.readInt());

      Volume.VolumeInfo.ImageLinks links = new Volume.VolumeInfo.ImageLinks();
      links.setSmallThumbnail(din.readUTF());
      info.setImageLinks(links);

      volume.setVolumeInfo(info);
      volume.setSelfLink(din.readUTF());
      volume.setId(din.readUTF());

      list.add(volume);
    }
    volumes.setItems(list);
  }
}
