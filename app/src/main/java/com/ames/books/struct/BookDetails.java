package com.ames.books.struct;

import com.google.api.client.repackaged.com.google.common.base.Strings;
import com.google.api.services.books.model.Volume;

import java.io.Serializable;

/**
 * Extended book details, fetched upon separate request.
 */
public class BookDetails implements Serializable {

  final String subtitle;
  final String description;
  final String fullPicture;
  final String purchaseUrl;
  final String publisher;
  final String publishedDate;

  public BookDetails(String subtitle, String description, String fullPicture, String purchaseUrl, String publisher, String publishedDate) {
    this.subtitle = subtitle;
    this.description = description;
    this.fullPicture = fullPicture;
    this.purchaseUrl = purchaseUrl;
    this.publisher = publisher;
    this.publishedDate = publishedDate;

  }

  /**
   * Construct from Google Books model
   */
  public BookDetails(Volume vol) {
    Volume.VolumeInfo info = vol.getVolumeInfo();
    subtitle = info.getSubtitle();
    description = info.getDescription();

    if (info.getImageLinks() != null) {
      String full = info.getImageLinks().getLarge();
      if (Strings.isNullOrEmpty(full)) {
        full = info.getImageLinks().getSmallThumbnail();
      }
      fullPicture = full;
    } else {
      fullPicture = null;
    }
    if (vol.getSaleInfo() != null) {
      purchaseUrl = vol.getSaleInfo().getBuyLink();
    } else {
      purchaseUrl = null;
    }
    this.publisher = info.getPublisher();
    this.publishedDate = info.getPublishedDate();
  }

  public String getSubtitle() {
    return subtitle;
  }

  public String getDescription() {
    return description;
  }

  public String getFullPicture() {
    return fullPicture;
  }

  public String getPurchaseUrl() {
    return purchaseUrl;
  }

  public String getPublisher() {
    return publisher;
  }

  public String getPublishedDate() {
    return publishedDate;
  }
}
