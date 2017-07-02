package com.ames.books;

import android.app.Fragment;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.ames.books.accessor.DetailsLoader;
import com.ames.books.accessor.DetailsLoadingResultListener;
import com.ames.books.presenter.ShowDetailsListener;
import com.ames.books.struct.Book;
import com.ames.books.struct.BookDetails;
import com.google.api.client.repackaged.com.google.common.base.Joiner;
import com.google.api.client.repackaged.com.google.common.base.Strings;

/**
 * Fragment that represents the details of the book.
 */
public class BookDetailsFragment extends Fragment implements ShowDetailsListener, DetailsLoadingResultListener {
  private static final String TAG = "ames.books.details";

  // Main data that appears immediately
  protected TextView title;
  protected TextView authors;
  protected TextView pages;

  // Other data
  protected TextView subtitle;
  protected TextView bottomLine;

  protected TextView description;

  protected ImageView picture;

  protected TextView purchaseLink;
  protected String purchaseUrl;

  protected TextView previewLink;
  protected String previewUrl;

  protected DetailsLoader detailsLoader = new DetailsLoader(this);
  protected PicassoService picassoService = new PicassoService();

  protected Drawable preloadedDrawable;

  protected Book current;

  protected ScrollView scrollView;

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    // Inflate the layout for this fragment
    View view = inflater.inflate(R.layout.fragment_book_details, container, false);

    // Immediately available
    title = (TextView) view.findViewById(R.id.title);
    authors = (TextView) view.findViewById(R.id.authors);
    pages = (TextView) view.findViewById(R.id.pages);

    // Available after update
    description = (TextView) view.findViewById(R.id.description);
    subtitle = (TextView) view.findViewById(R.id.subtitle);
    bottomLine = (TextView) view.findViewById(R.id.bottom_line);
    pages = (TextView) view.findViewById(R.id.pages);

    picture = (ImageView) view.findViewById(R.id.picture);
    scrollView = (ScrollView) view.findViewById(R.id.scroll_view);

    purchaseLink = (TextView) view.findViewById(R.id.purchase_link);

    purchaseLink.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        try {
          if (!Strings.isNullOrEmpty(purchaseUrl)) {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(purchaseUrl));
            startActivity(browserIntent);
          }
        } catch (Exception e) {
          Log.d(TAG, "Broken purchase URL " + purchaseLink);
        }
      }
    });

    previewLink = (TextView) view.findViewById(R.id.preview_link);

    previewLink.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        try {
          Log.i(TAG, "Preview "+previewUrl);
          if (!Strings.isNullOrEmpty(previewUrl)) {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(previewUrl));
            startActivity(browserIntent);
          }
        } catch (Exception e) {
          Log.d(TAG, "Broken preview URL " + previewUrl);
        }
      }
    });

    view.findViewById(R.id.search_about).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        String terms = title.getText().toString().replace(' ', '-') +
           "+" + authors.getText().toString().replace(' ', '+');
        Log.d(TAG, "Searching for [" + terms + "]");
        String url = "https://www.google.com/search?q=" + terms;
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(browserIntent);
      }
    });

    acceptState(savedInstanceState);
    return view;
  }

  private void acceptState(Bundle savedInstanceState) {
    if (savedInstanceState != null) {
      current = (Book) savedInstanceState.getSerializable("current_details");
      if (current != null) {
        // Picasa needs the placeholder.
        showDetails(current, getActivity().getDrawable(R.drawable.user_placeholder));
        // Try to set the thumb first. Full size image is sometimes not available.
        setCover(current.getSmallThumbnail());
      }
    }
  }

  @Override
  public void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    if (current != null) {
      outState.putSerializable("current_details", current);
    }
  }

  public void showDetails(Book book, Drawable thumb) {
    Log.d(TAG, "Show details on " + book.getId());
    // Reset the scroll view if this is a different from previous book
    if (current != null && !current.getId().equals(book.getId())) {
      scrollView.scrollTo(0,0);
    }

    // Show mandatory info
    current = book;
    title.setText(book.getTitle());
    if (book.getAuthors() != null) {
      authors.setText(Joiner.on(", ").join(book.getAuthors()));
    } else {
      authors.setText(null);
    }
    Integer count = book.getPageCount();
    if (count != null) {
      String pageString = pages.getResources().getString(R.string.pages, count);
      pages.setText(pageString);
    } else {
      pages.setText(null);
    }

    preloadedDrawable = thumb;
    setSaleInfo(current);

    if (book.getDetails() == null) {
      if (thumb != null) {
        picture.setImageDrawable(thumb.getCurrent());
      } else {
        picture.setImageResource(R.drawable.user_placeholder);
      }
      detailsLoader.doSearch(book);
    } else {
      onDetailsLoaded(book);
    }
  }

  @Override
  public void onDetailsLoaded(final Book bookWithDetails) {
    if (Looper.getMainLooper().getThread() != Thread.currentThread()) {
      getActivity().runOnUiThread(new Runnable() {
        @Override
        public void run() {
          doOnDetailsLoaded(bookWithDetails);
        }
      });
    } else {
      doOnDetailsLoaded(bookWithDetails);
    }
  }

  private void doOnDetailsLoaded(Book book) {
    current = book;
    BookDetails details = book.getDetails();
    if (details == null) {
      return; // error on loading
    }

    try {
      setSaleInfo(current);
      setPreviewInfo(current);
      String subtitleText = details.getSubtitle();

      String desc = details.getDescription();
      // Description is in HTML
      if (desc != null && !desc.isEmpty()) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
          description.setText(Html.fromHtml(desc, Html.FROM_HTML_MODE_COMPACT));
        } else {
          //noinspection deprecation
          description.setText(Html.fromHtml(desc));
        }
      } else {
        if (subtitleText == null) {
          subtitleText = getString(R.string.no_further_description);
        }
      }

      if (subtitleText != null) {
        subtitle.setText(subtitleText);
        subtitle.setVisibility(View.VISIBLE);
      } else {
        subtitle.setVisibility(View.GONE);
      }

      if (details.getPublisher() != null && details.getPublishedDate() != null) {
        bottomLine.setText(getString(R.string.bottom_line, details.getPublisher(), details.getPublishedDate()));
      } else {
        bottomLine.setText(null);
      }

      String cover = details.getFullPicture();

      setCover(cover);
    } catch (Exception e) {
      Log.e(TAG, "loading failed", e);
    }
  }

  private void setSaleInfo(Book current) {
    if (current.getDetails() != null &&
       current.getDetails().getPurchaseUrl() != null) {
      //purchaseLink.setText(current.getSaleInfo().getBuyLink());
      purchaseUrl = current.getDetails().getPurchaseUrl();
      purchaseLink.setVisibility(View.VISIBLE);
    } else {
      purchaseLink.setVisibility(View.GONE);
      purchaseUrl = null;
    }
  }

  private void setCover(String cover) {
    picassoService.setCover(cover, picture, preloadedDrawable);
  }

  /**
   * Reconfigure services in use. This method is for instrumentation testing.
   */
  public void reconfigureServices(DetailsLoader detailsLoader, PicassoService picassoService) {
    this.detailsLoader = detailsLoader;
    this.picassoService = picassoService;
  }

  public void setPreviewInfo(Book current) {
    if (current.getDetails() != null && current.getDetails().getPreviewUrl() != null) {
      previewLink.setVisibility(View.VISIBLE);
      previewUrl = current.getDetails().getPreviewUrl();
    } else {
      previewLink.setVisibility(View.GONE);
      previewUrl = null;
    }
  }
}
