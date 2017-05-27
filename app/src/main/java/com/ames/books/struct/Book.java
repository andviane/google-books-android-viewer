package com.ames.books.struct;

import com.google.api.services.books.model.Volume;

import java.io.Serializable;
import java.util.List;

/**
 * A book structure that is used internally. Being Serializable allows to support onSaveInstanceState easily
 * (Google Books model seems unable)
 */
public class Book implements Serializable {

  //"totalItems,items(volumeInfo(title,authors,pageCount,imageLinks/smallThumbnail),selfLink,id)")
  final String id;
  final String title;
  final List<String> authors;
  final Integer pageCount; // null if the source does not provide the page count
  final String smallThumbnail;

  BookDetails details;

  public Book(String id, String title, List<String> authors, int pageCount, String smallThumbnail) {
    this.id = id;
    this.title = title;
    this.authors = authors;
    this.pageCount = pageCount;
    this.smallThumbnail = smallThumbnail;
  }

  /**
   * Construct from the Google Books Model
   */
  public Book(Volume vol) {
    id = vol.getId();
    Volume.VolumeInfo info = vol.getVolumeInfo();
    title = info.getTitle();
    authors = info.getAuthors();
    pageCount = info.getPageCount();

    Volume.VolumeInfo.ImageLinks links = info.getImageLinks();
    if (links != null) {
      smallThumbnail = links.getSmallThumbnail();
    } else {
      smallThumbnail = null;
    }
  }

  public String getId() {
    return id;
  }

  public String getTitle() {
    return title;
  }

  public Integer getPageCount() {
    return pageCount;
  }

  public String getSmallThumbnail() {
    return smallThumbnail;
  }

  public Book setDetails(BookDetails details) {
    this.details = details;
    return this;
  }

  public BookDetails getDetails() {
    return details;
  }

  public List<String> getAuthors() {
    return authors;
  }
}
