package com.ames.books;

import android.app.Fragment;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ames.books.accessor.DetailsLoader;
import com.ames.books.accessor.DetailsLoadingResultListener;
import com.ames.books.presenter.ShowDetailsListener;
import com.google.api.client.repackaged.com.google.common.base.Joiner;
import com.google.api.services.books.model.Volume;

import java.util.ArrayList;

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

  protected DetailsLoader detailsLoader = new DetailsLoader(this);
  protected PicassoService picassoService = new PicassoService();

  protected Drawable preloadedDrawable;

  protected Volume current;

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

    acceptState(savedInstanceState);
    return view;
  }

  private void acceptState(Bundle savedInstanceState) {
    if (savedInstanceState != null) {
      Volume vol = new Volume();
      vol.setId(savedInstanceState.getString("d.id", null));
      Log.d(TAG, "Resumed id " + vol.getId());
      if (vol.getId() != null) {
        Volume.VolumeInfo info = new Volume.VolumeInfo();

        info.setTitle(savedInstanceState.getString("d.title"));
        info.setSubtitle(savedInstanceState.getString("d.subtitle"));
        info.setDescription(savedInstanceState.getString("d.description"));
        info.setAuthors(savedInstanceState.getStringArrayList("d.authors"));
        info.setPublishedDate(savedInstanceState.getString("d.pubdate"));
        info.setPublisher(savedInstanceState.getString("d.publisher"));
        info.setPageCount(savedInstanceState.getInt("d.pages"));

        Volume.VolumeInfo.ImageLinks links = new Volume.VolumeInfo.ImageLinks();
        links.setLarge(savedInstanceState.getString("d.cover", null));
        links.setSmallThumbnail(savedInstanceState.getString("d.thumb", null));
        info.setImageLinks(links);
        vol.setVolumeInfo(info);

        // Picasa needs the placeholder.
        showDetails(vol, getActivity().getDrawable(R.drawable.user_placeholder));
        // Try to set the thumb first. Full size image is sometimes not available.
        setCover(links.getSmallThumbnail());
      }
    }
  }

  @Override
  public void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    if (current != null) {
      outState.putString("d.id", current.getId());
      Log.d(TAG, "Saved id " + current.getId());
      Volume.VolumeInfo info = current.getVolumeInfo();
      outState.putString("d.title", info.getTitle());
      outState.putString("d.subtitle", info.getSubtitle());
      outState.putString("d.description", info.getDescription());

      if (info.getImageLinks() != null) {
        outState.putString("d.cover", info.getImageLinks().getLarge());
        outState.putString("d.thumb", info.getImageLinks().getSmallThumbnail());
      }

      if (info.getAuthors() != null) {
        outState.putStringArrayList("d.authors", new ArrayList<>(info.getAuthors()));
      } else {
        outState.putStringArrayList("d.authors", new ArrayList<String>());
      }

      outState.putString("d.pubdate", info.getPublishedDate());
      outState.putString("d.publisher", info.getPublisher());
      if (info.getPageCount() != null) {
        outState.putInt("d.pages", info.getPageCount());
      }
    }
  }

  public void showDetails(Volume book, Drawable thumb) {
    // Show mandatory info
    Log.d(TAG, "Show details on " + book.getId());
    current = book;

    final Volume.VolumeInfo volume = book.getVolumeInfo();
    title.setText(volume.getTitle());
    if (volume.getAuthors() != null) {
      authors.setText(Joiner.on(", ").join(volume.getAuthors()));
    } else {
      authors.setText(null);
    }
    Integer count = volume.getPageCount();
    if (count != null) {
      String pageString = pages.getResources().getString(R.string.pages, count);
      pages.setText(pageString);
    } else {
      pages.setText(null);
    }

    preloadedDrawable = thumb;

    // Large image URL is only available when details are leaded.
    if (volume.getImageLinks().getLarge() == null) {
      picture.setImageDrawable(preloadedDrawable);
      subtitle.setText(R.string.loading);

      description.setText(null);
      bottomLine.setText(null);

      Log.d(TAG, "No cache, reloading...");
      detailsLoader.doSearch(book);
    } else {
      // otherwise the information is already populated
      onDetailsLoaded(book);
    }
  }

  @Override
  public void onDetailsLoaded(final Volume details) {
    if (Looper.getMainLooper().getThread() != Thread.currentThread()) {
      getActivity().runOnUiThread(new Runnable() {
        @Override
        public void run() {
          doOnDetailsLoaded(details);
        }
      });
    } else {
      doOnDetailsLoaded(details);
    }
  }

  private void doOnDetailsLoaded(Volume details) {
    current = details;
    try {
      Volume.VolumeInfo volume = details.getVolumeInfo();
      String subtitleText = volume.getSubtitle();

      String desc = volume.getDescription();
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

      if (volume.getPublisher() != null && volume.getPublishedDate() != null) {
        bottomLine.setText(getString(R.string.bottom_line, volume.getPublisher(), volume.getPublishedDate()));
      } else {
        bottomLine.setText(null);
      }

      String cover = null;
      if (volume.getImageLinks() != null) {
        cover = volume.getImageLinks().getLarge();
      }

      setCover(cover);
    } catch (Exception e) {
      Log.e(TAG, "loading failed", e);
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
}
