package com.ames.books.presenter;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.ames.books.R;
import com.ames.books.struct.Book;
import com.google.api.client.repackaged.com.google.common.base.Joiner;
import com.google.api.services.books.model.Volume;
import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.squareup.picasso.Picasso.with;
import static java.lang.System.load;

/**
 * Represents a singe record of the single book.
 */
public class BookViewHolder extends RecyclerView.ViewHolder {
  private static final String TAG = "ames.books.bvh";

  private static final ExecutorService executors = Executors.newSingleThreadExecutor();

  /**
   * The book currently being displayed
   */
  private Book book;

  private final TextView title;
  private final TextView authors;
  private final TextView pages;
  private final ImageView picture;

  private Picasso picasso;

  public BookViewHolder(View itemView, final ShowDetailsListener showDetailsListener) {
    super(itemView);

    title = (TextView) itemView.findViewById(R.id.title);
    authors = (TextView) itemView.findViewById(R.id.authors);
    pages = (TextView) itemView.findViewById(R.id.pages);

    picture = (ImageView) itemView.findViewById(R.id.picture);
    itemView.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        if (book != null) {
          showDetailsListener.showDetails(book, picture.getDrawable());
          Log.d(TAG, "Image clicked " + title.getText());
        }
      }
    });

    Picasso.Builder builder = new Picasso.Builder(picture.getContext());
    builder.executor(executors);
    picasso = builder.build();
  }

  public void setBook(Book book) {
    this.book = book;

    if (book != null) {
      title.setText(book.getTitle());
      setAuthors(book.getAuthors());
      setPageCount(book.getPageCount());
      setPicture(book.getSmallThumbnail());
    } else {
      title.setText(null);
      setAuthors(null);
      setPageCount(null);
      picture.setVisibility(View.INVISIBLE);
    }
  }

  private void setPicture(String thumb) {
    boolean loading = false;
    if (thumb != null && !thumb.isEmpty()) {
      picasso.load(thumb)
         .placeholder(R.drawable.user_placeholder)
         .error(R.drawable.user_placeholder_error)
         .into(picture);

      loading = true;
    }
    picture.setVisibility(loading ? View.VISIBLE : View.INVISIBLE);
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
