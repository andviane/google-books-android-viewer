[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.github.andviane/uncover/badge.svg)](https://mvnrepository.com/artifact/io.github.andviane/uncover) [![Build Status](https://travis-ci.org/andviane/google-books-android-viewer.svg?branch=master)](https://travis-ci.org/andviane/google-books-android-viewer) [![Javadoc](http://javadoc-badge.appspot.com/io.github.andviane/uncover.svg?label=Javadoc)](https://andviane.github.io/google-books-android-viewer/javadoc/index.html)

# Infinite scrolling lists are both easy and efficient

![Screen shot](https://raw.githubusercontent.com/andviane/google-books-android-viewer/master/info/sc1_sm.png "Our proof of concept app")

This project is a proof of concept demonstrator of Uncover library for Android. This library provides easy bridging between [RecyclerView](https://developer.android.com/reference/android/support/v7/widget/RecyclerView.html) or similar data model (synchronous access to single item by its index) and the typical web or database request that strongly favours asynchronous access in chunks (pages of the fixed size). It performs multiple management optimizations on how these pages should be fetched and prioritized: 

* Fetch in non overlapping pages rather than item by item.
* Fetch most recently requested pages first, not last.
* If the user swipes quickly, drop from the queue pages that, while not fetched, have already been scrolled out of the visible area.
 
The size of the items (hence the number of items per page) need not be constant, as it is often the case for descriptions of variable length or other similar texts. 

The library sources are available in the Uncover folder under Apache 2.0 license. This project contains the source code. To fetch from the pgp-signed build of this library from [Maven Central](https://search.maven.org/#search%7Cgav%7C1%7Cg%3A%22io.github.andviane%22%20AND%20a%3A%22uncover%22) as your dependency, simply include

```java

dependencies {
    compile ('io.github.andviane:uncover:2.0.1@aar')
    ..
}    
```    
into your Gradle build script. 

# Uncover library

The proposed library is centered around the data model [UncoveringDataModel](uncover/src/main/java/ames/com/uncover/UncoveringDataModel.java) that can be relatively easily tied to the data model of any UI list. This model provides item values by position, as well as the total number of items:
```java
    // Construction
    UncoveringDataModel<Book> model = new UncoveringDataModel<Book>();
    data.setPrimaryDataProvider(new BookDataProvider()); // Implement your own
    data.setDataAvailableListener(this); // receive notifications when first results of the query arrive

    // Serve as data model
    Book book = data.getItem(position);
    int itemCount = data.size();

    // Set the query to show
    data.setQuery(new Query(query));
```    

The model can be tied to the existing recycler view and its adapter via method:
```java
    model.install(recyclerView, adapter);
```    

Yes, yes, we are aware that in MVC the model should not know much about adapter, leave alone the view. However if the model must notify the view when it has data ready (so the region should be repainted), it needs the handle where to sent the event. And if the model needs to know which data are in more priority to provide, it must ask for the view about the currently visible region. This is not a typical one way MVC bus, so sorry about that.

As about adapter, it must be a [RecyclerView.Adapter](https://developer.android.com/reference/android/support/v7/widget/RecyclerView.Adapter.html), nothing special is required. Its main task is to create views for complex items. See the [BookListAdapter](app/src/main/java/com/ames/books/presenter/BookListAdapter.java) class for our version. In our case adapter also creates model and attaches the primary data provider, but for sure you may design this differently.

The model itself fetches requests using your class that must implement the [PrimaryDataProvider](uncover/src/main/java/ames/com/uncover/primary/PrimaryDataProvider.java). It requests data asynchronuosly, in non-overapping chunks of the fixed (configurable) size, and prioritizes recent requests over older ones. While parallel requests are possible, they are under control: the maximal number is configurable (at most two are allowed by default).

To show the new data, simply call setQuery on the [UncoveringDataModel](uncover/src/main/java/ames/com/uncover/UncoveringDataModel.java). This method accepts the [Query](uncover/src/main/java/ames/com/uncover/primary/Query.java) that is passed to your [PrimaryDataProvider](uncover/src/main/java/ames/com/uncover/primary/PrimaryDataProvider.java). To be notified about the new data that the view must display, register the data listener as it is seen in [BookListAdapter](app/src/main/java/com/ames/books/presenter/BookListAdapter.java) class.

Model also provides methods to get and set the state as Serializable. This is for saving instance state in cases like device reorientation; not for long term storage. Memory trimming is supported, see [BookListActivity](app/src/main/java/com/ames/books/BookListActivity.java).   

The source code of this library is located under the uncover folder. Use ../gradlew assembleRelease in this folder to build the .aar file if required. 

# Still too complex?

What about the single Java class that does it all? Here is the class:

```java
package com.ames.uncoverguide;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ames.uncover.UncoveringDataModel;
import com.ames.uncover.primary.PrimaryDataProvider;
import com.ames.uncover.primary.PrimaryRequest;
import com.ames.uncover.primary.PrimaryResponse;
import com.ames.uncover.primary.Query;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    final RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler);
    final UncoveringDataModel<String> model = new UncoveringDataModel<>();

    model.setPrimaryDataProvider(new PrimaryDataProvider<String>() {

      @Override
      public PrimaryResponse fetch(PrimaryRequest primaryRequest) {
        // Observe logs so see the fetching
        Log.i("Fetch", "Service call to fetch items" + 
          primaryRequest.getFrom() + "- " + primaryRequest.getTo());

        // Simulate pause. We are on the background thread now.
        try { Thread.sleep(300); } catch (InterruptedException e) {}

        ArrayList<String> data = new ArrayList<String>();
        for (int p = primaryRequest.getFrom(); p < primaryRequest.getTo(); p++) {
          data.add("Item " + p + " by " + primaryRequest.getQuery());
        }
        // Integer.MAX_VALUE items in total, enjoy scrolling
        return new PrimaryResponse<String>(data, Integer.MAX_VALUE);
      }
    });

    RecyclerView.Adapter adapter = new RecyclerView.Adapter() {

      @Override
      public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new RecyclerView.ViewHolder(new TextView(MainActivity.this)) { };
      }

      @Override
      public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        String content = model.getItem(position);
        ((TextView) holder.itemView).setText(content);
      }

      @Override
      public int getItemCount() {
        return model.size();
      }
    };

    model.install(recyclerView, adapter);

    // Done, now just set the query to show. If we do not set the query, all we see is empty list.
    // Add the button and set the query from its listener to observe the output change.
    model.setQuery(new Query("abc"));
  }
}
```

This is off course the totally "hello world" demo: the primary data provider just bakes the data locally, the adapter works with TextView and, unlike in production code, the general style and design are optimized for quick and easy reading. We included a short delay on getting the data, to demonstrate that while looks simple, this is still a call on another thread, fetching is still chunked, and requests to get these chunks are still optimized. 

You also need a layout to make this compile:

```java
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    >

    <android.support.v7.widget.RecyclerView
        android:id="@+id/recycler"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        app:layoutManager="android.support.v7.widget.LinearLayoutManager"/>

</LinearLayout>

```

With these two files, it is easy to build the app that is ready to run.

# Description of the demo app

To demonstrate the library capabilities, the project provides Android application to demonstrate the "infinite scroll" over Google Books, displaying cover images and titles. This application is available under GPL v3.0.

The app uses Google Books API to scroll over the list of content that is returned as search result. It consists of two screens (fragments). The list screen one allows to scroll over search results, showing only cover thumb image, header, author and page count. The details screen that opens after tapping anywhere on the book item reveals more information about the particular book. Use the back button for returning back to the list.

If you just want quick preview, the Android app is available on [F-Droid](https://f-droid.org/packages/com.ames.books/) and [Google Play](https://play.google.com/store/apps/details?id=com.ames.books)

The app also fetches the book cover images that take much longer to appear. Image downloading and management is implemented with [Picasso](http://square.github.io/picasso/) and not directly related to the demonstration of Uncover library capabilities.


See also [the licensing conditions](https://developers.google.com/books/terms) of Google Books API. 
