# Android infinite scrolling lists are now both easy and efficient

This project is a proof of concept demonstrator of Uncover library that provides easy bridging between [RecyclerView](https://developer.android.com/reference/android/support/v7/widget/RecyclerView.html) or similar data model (synchronous access to single item by its index) and the typical web or database request that strongly favours asynchronous access in chunks (pages of the fixed size). It performs multiple management optimizations on how these pages should be fetched and prioritized. The library is available in the Uncover folder under Apache 2.0 license. 

To demonstrate the library capabilities, the project provides Android application to demonstrate the "infinite scroll" over Google Books, displaying cover images and titles. This application is available under GPL v3.0.

# Description of the demo app

The app uses Google Books API to scroll over the list of content that is returned as search result. It consists of two screens (fragments). The list screen one allows to scroll over search results, showing only cover thumb image, header, author and page count. The details screen that opens after tapping anywhere on the book item reveals more information about the particular book. Use the back button for returning back to the list.

If you just want quick preview, the Android app is available at [PlayStore](https://play.google.com/store/apps/details?id=com.ames.books&rdid=com.ames.books)

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

See the [BookListAdapter](app/src/main/java/com/ames/books/presenter/BookListAdapter.java) class for the finished, runnable code.

The model itself fetches requests using your class that must implement the [PrimaryDataProvider](uncover/src/main/java/ames/com/uncover/primary/PrimaryDataProvider.java). It requests data asynchronuosly, in non-overapping chunks of the fixed (configurable) size, and prioritizes recent requests over older ones. While parallel requests are possible, they are under control: the maximal number is configurable (at most two are allowed by default).

To show the new data, simply call setQuery on the [UncoveringDataModel](uncover/src/main/java/ames/com/uncover/UncoveringDataModel.java). This method accepts the [Query](uncover/src/main/java/ames/com/uncover/primary/Query.java) that is passed to your [PrimaryDataProvider](uncover/src/main/java/ames/com/uncover/primary/PrimaryDataProvider.java). To be notified about the new data that the view must display, register the data listener as it is seen in [BookListAdapter](app/src/main/java/com/ames/books/presenter/BookListAdapter.java) class.

Model also provides methods to get and set the state as Serializable. This is for saving instance state in cases like device reorientation; not for long term storage. Memory trimming is supported, see [BookListActivity](master/app/src/main/java/com/ames/books/BookListActivity.java).   

The source code of this library is located under the uncover folder. Use ../gradlew assembleRelease in this folder to build the .aar file if required. 

# Note

See also [the licensing conditions](https://developers.google.com/books/terms) of Google Books API. 


