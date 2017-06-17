package ames.com.uncover.impl;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import ames.com.uncover.UncoverAwareAdapter;

/**
 * Performs additional fetch optimizations based on information about the currently visible aera.
 * It only has static method that must be called in onCreate or onCreateView when
 * the current contex, recycler view and adapter are all available. For instance, in onCreate:
 *
 *  FetchOptimizer.install(view.getContext(), myRecyclerView, myAdapter);
 */
public class FetchOptimizer {

  public static void install(Context ctx, RecyclerView recyclerView, final UncoverAwareAdapter adapter) {
    final LinearLayoutManager manager = new LinearLayoutManager(ctx);
    recyclerView.setLayoutManager(manager);
    adapter.setLayoutManger(manager);
    recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
      @Override
      public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        adapter.getModel().getDataFetchManager().notifyVisibleArea(manager.findFirstCompletelyVisibleItemPosition(),
           manager.findLastVisibleItemPosition() + 1);
      }
    });
  }
}
