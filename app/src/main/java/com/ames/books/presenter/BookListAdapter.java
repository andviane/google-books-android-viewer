package com.ames.books.presenter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ames.books.R;
import com.ames.books.accessor.BookDataProvider;
import com.ames.books.struct.Book;

import java.io.Serializable;

import com.ames.uncover.UncoveringDataModel;
import com.ames.uncover.primary.Query;

/**
 * Adapter to wrap the list of book record.
 */
public class BookListAdapter extends RecyclerView.Adapter<BookViewHolder>  {
  private UncoveringDataModel<Book> data;
  private ShowDetailsListener showDetailsListener;

  public BookListAdapter(ShowDetailsListener showDetailsListener) {
    this.showDetailsListener = showDetailsListener;
    data = new UncoveringDataModel<>();
    data.setPrimaryDataProvider(new BookDataProvider());
  }

  @Override
  public BookViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    View thisItemsView = LayoutInflater.from(parent.getContext()).inflate(R.layout.book_item_layout,
       parent, false);
    return new BookViewHolder(thisItemsView, showDetailsListener);
  }

  @Override
  public void onBindViewHolder(BookViewHolder holder, int position) {
    holder.setBook(data.getItem(position));
  }

  @Override
  public int getItemCount() {
    return data.size();
  }

  /**
   * Set the new book list that replaces the current book list.
   */
  public void setQuery(String query) {
    this.data.setQuery(new Query(query));
    notifyDataSetChanged();
  }

  public void setState(Serializable state) {
    data.setState(state);
    notifyDataSetChanged();
  }

  public Serializable getState() {
    return data.getState();
  }

  public UncoveringDataModel<Book> getModel() {
    return data;
  }
}
