/*
Copyright 2017 Audrius Meskauskas

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */
package com.ames.uncover.impl;

import android.os.AsyncTask;
import android.util.Log;

import java.io.PrintWriter;
import java.io.StringWriter;

import com.ames.uncover.impl.DataAvailableListener;
import com.ames.uncover.impl.DataFetcher;
import com.ames.uncover.primary.PrimaryDataProvider;
import com.ames.uncover.primary.PrimaryRequest;
import com.ames.uncover.primary.PrimaryResponse;

/**
 * This class manages the correct thread transition using Android AsyncTask. It is the DataFetcher,
 * and can take already synchronous PrimaryDataProvider.
 */
public class AsyncBridge<ITEM> implements DataFetcher<ITEM> {
  private static final String TAG = "AsyncBridge";
  private final PrimaryDataProvider provider;

  private DataAvailableListener<ITEM> listener;
  private boolean totalCountReturned = false;

  public AsyncBridge(PrimaryDataProvider provider) {
    this.provider = provider;
  }

  public String errms;

  @Override
  public void setOnDataAvailableListener(DataAvailableListener listener) {
    this.listener = listener;
  }

  @Override
  public void requestData(final PrimaryRequest request) {
    new AsyncTask<PrimaryRequest, Void, PrimaryResponse>() {

      @Override
      protected void onPostExecute(PrimaryResponse response) {
        if (response == null) {
          onCancelled(response);
        } else {
          if (response.getTotalItems() != null) {
            totalCountReturned = true;
          }
          listener.dataAvailable(request, response);
        }
      }

      @Override
      protected void onCancelled(PrimaryResponse response) {
        onCancelled();
      }

      @Override
      protected void onCancelled() {
        listener.dataUnavailable(request);
      }

      @Override
      protected PrimaryResponse doInBackground(PrimaryRequest... params) {
        try {
          PrimaryResponse r = provider.fetch(params[0]);
          return r;
        } catch (Exception e) {
          Log.e(TAG, "Failed to fetch", e);
          return null;
        }
      }
    }.execute(request);
  }

  /**
   * Create a copy of self, free from pending requests.
   */
  @Override
  public DataFetcher reset() {
    DataFetcher newFetcher = new AsyncBridge(provider);
    newFetcher.setOnDataAvailableListener(listener);
    listener = null;
    return newFetcher;
  }
}
