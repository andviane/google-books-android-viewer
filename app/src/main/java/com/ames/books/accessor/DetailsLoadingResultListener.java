package com.ames.books.accessor;

import com.google.api.services.books.model.Volume;

/**
 * Accepts notification that some result has been loaded.
 */
public interface DetailsLoadingResultListener {

  void onDetailsLoaded(Volume details);

}
