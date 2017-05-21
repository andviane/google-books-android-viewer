package com.ames.books.presenter;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.ames.books.R;
import com.google.api.client.repackaged.com.google.common.base.Joiner;
import com.google.api.services.books.model.Volume;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Represents a singe record of the single book.
 */
public class BookViewHolder extends RecyclerView.ViewHolder {
  private static final String TAG = "ames.books.bvh";
  /**
   * The book currently being displayed
   */
  private Volume book;

  private final TextView title;
  private final TextView authors;
  private final TextView pages;
  private final ImageView picture;

  public BookViewHolder(View itemView, final ShowDetailsListener showDetailsListener) {
    super(itemView);

    title = (TextView) itemView.findViewById(R.id.title);
    authors = (TextView) itemView.findViewById(R.id.authors);
    pages = (TextView) itemView.findViewById(R.id.pages);

    picture = (ImageView) itemView.findViewById(R.id.picture);

    // We could attach the listener on the whole item, but this may trigger
    // false responses while flinging for the more clumsy users. Restricting
    // click area by the book picture only solves the navigation problem.
    itemView.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        if (book != null) {
          showDetailsListener.showDetails(book, picture.getDrawable());
          Log.d(TAG, "Image clicked " + title.getText());
        }
      }
    });
  }

  public void setBook(Volume book) {
    this.book = book;

    if (book != null) {
      final Volume.VolumeInfo volume = book.getVolumeInfo();
      if (volume != null) {
        setTitle(volume);
        setAuthors(volume.getAuthors());
        setPageCount(volume.getPageCount());
        setPicture(volume.getImageLinks());
        return;
      }
    }

    title.setText(null);
    setAuthors(null);
    setPageCount(null);
    picture.setVisibility(View.INVISIBLE);
  }

  private void setPicture(Volume.VolumeInfo.ImageLinks links) {
    boolean loading = false;
    if (links != null) {
      String thumb = links.getSmallThumbnail();
      if (thumb != null && !thumb.isEmpty()) {

        Picasso.with(picture.getContext())
           .load(thumb)
           .placeholder(R.drawable.user_placeholder)
           .error(R.drawable.user_placeholder_error)
           .into(picture);

        loading = true;
      }
    }
    picture.setVisibility(loading ? View.VISIBLE : View.INVISIBLE);
  }

  private void setTitle(Volume.VolumeInfo volume) {
    title.setText(volume.getTitle());
  }

  private void setAuthors(List<String> authorList) {
    int visibility;
    if (authorList != null && !authorList.isEmpty()) {
      String join = Joiner.on(", ").join(authorList);
      authors.setText(join);
      visibility = View.VISIBLE;
    } else {
      visibility = View.GONE;
    }
    authors.setVisibility(visibility);
  }

  private void setPageCount(Integer count) {
    int visibility;
    if (count != null) {
      String pageString = pages.getResources().getString(R.string.pages, count);
      pages.setText(pageString);
      visibility = View.VISIBLE;
    } else {
      visibility = View.GONE;
    }
    pages.setVisibility(visibility);
  }
}
