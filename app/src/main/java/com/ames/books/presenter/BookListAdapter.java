package com.ames.books.presenter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ames.books.R;
import com.ames.books.data.BookData;
import com.ames.books.data.DataChangeListener;
import com.ames.books.data.SearchBlock;

import java.io.Serializable;

/**
 * Adapter to wrap the list of book record.
 */
public class BookListAdapter extends RecyclerView.Adapter<BookViewHolder> implements DataChangeListener {
  private BookData data;
  private ShowDetailsListener showDetailsListener;

  public BookListAdapter(ShowDetailsListener showDetailsListener) {
    this.showDetailsListener = showDetailsListener;
    data = new BookData(this);
  }

  @Override
  public BookViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    View thisItemsView = LayoutInflater.from(parent.getContext()).inflate(R.layout.book_item_layout,
       parent, false);
    return new BookViewHolder(thisItemsView, showDetailsListener);
  }

  @Override
  public void onBindViewHolder(BookViewHolder holder, int position) {
    holder.setBook(data.get(position));
  }

  @Override
  public int getItemCount() {
    return data.size();
  }

  /**
   * Set the new book list that replaces the current book list.
   */
  public void setBookList(SearchBlock bookList, String query) {
    this.data.setBookList(bookList, query);
    notifyDataSetChanged();
  }

  @Override
  public void notifyDataChanged() {
    notifyDataSetChanged();
  }

  @Override
  public void notifyRegionChanged(int from, int to) {
    notifyItemRangeChanged(from, to - from);
  }

  public void setState(Serializable state) {
    data.setState(state);
    notifyDataChanged();
  }

  public Serializable getState() {
    return data.getState();
  }
}
