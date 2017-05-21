package com.ames.books;

import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

/**
 * Picasso interfacing class, exists due the need abstract static calls during tests.
 */
public class PicassoService {

  protected void setCover(String cover, ImageView picture, Drawable preloadedDrawable) {
    if (cover != null) {
      Picasso.with(picture.getContext())
         .load(cover)
         .placeholder(preloadedDrawable)
         .error(preloadedDrawable)
         .into(picture);
    }
  }
}
