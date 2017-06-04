package com.ames.books;

import android.graphics.drawable.Drawable;
import android.support.test.filters.SmallTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.widget.ImageView;

import com.ames.books.accessor.DetailsLoader;
import com.ames.books.struct.Book;
import com.ames.books.struct.BookDetails;
import com.google.api.services.books.model.Volume;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.same;

/**
 * This is more serious rule-based test of our fragment. To isolate fragment from the normally attached
 * services, we use the defined reconfigureSerivices method.
 */
@RunWith(AndroidJUnit4.class)
@SmallTest
public class BookDetailsFragmentTest {
  /**
   * Fragment under testing
   */
  BookDetailsFragment details;
  BookListActivity activity;

  /**
   * Mocks the disconnect the fragment from the real world Internet
   */
  DetailsLoader detailsLoader = Mockito.mock(DetailsLoader.class);
  PicassoService picassoService = Mockito.mock(PicassoService.class);

  @Rule
  public ActivityTestRule activityRule = new ActivityTestRule<>(
     BookListActivity.class);

  @Before
  public void setup() {
    details = (BookDetailsFragment) activityRule.getActivity().getFragmentManager().findFragmentById(R.id.book_details);
    activity = (BookListActivity) activityRule.getActivity();

    // replace interfacing classes. This approach does not require interfering with the app startup or configuration
    details.reconfigureServices(detailsLoader, picassoService);
  }

  @Test
  public void testFieldsPopulated() {
    Drawable thumb = activity.getDrawable(R.drawable.user_placeholder);
    Volume book = new Volume();

    Volume.VolumeInfo info = new Volume.VolumeInfo();
    Volume.VolumeInfo.ImageLinks links = new Volume.VolumeInfo.ImageLinks();
    links.setLarge("http://large/large.png");

    info.setImageLinks(links); // no links so will be no outgoing downloads.
    info.setTitle("The title of my book");
    info.setSubtitle("The subtitle of my book");

    book.setVolumeInfo(info);

    Book rb = new Book(book);
    rb.setDetails(new BookDetails(book));

    activity.showDetails(rb, thumb);

    // Verify main title changed
    onView(withId(R.id.title)).check(matches(withText("The title of my book")));
    onView(withId(R.id.subtitle)).check(matches(withText("The subtitle of my book")));

    // Verify fetching large image
    Mockito.verify(picassoService).setCover(eq("http://large/large.png"), (ImageView) any(), same(thumb));
  }
}