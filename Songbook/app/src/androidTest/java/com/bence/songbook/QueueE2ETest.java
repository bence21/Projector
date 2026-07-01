package com.bence.songbook;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.bence.songbook.actions.QueueDragActions;
import com.bence.songbook.assertions.RecyclerViewItemCountAssertion;
import com.bence.songbook.assertions.RecyclerViewTitleAtPositionAssertion;
import com.bence.songbook.rules.BundledDatabaseTestRule;
import com.bence.songbook.rules.DisableAnimationsRule;
import com.bence.songbook.rules.QueueTestRule;
import com.bence.songbook.ui.activity.MainActivity;

import org.hamcrest.Matchers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class QueueE2ETest {

    private final DisableAnimationsRule disableAnimationsRule = new DisableAnimationsRule();
    private final BundledDatabaseTestRule bundledDatabaseTestRule = new BundledDatabaseTestRule();
    private final QueueTestRule queueTestRule = new QueueTestRule();
    private final ActivityScenarioRule<MainActivity> activityRule =
            new ActivityScenarioRule<>(MainActivity.class);

    @Rule
    public final RuleChain ruleChain = RuleChain
            .outerRule(disableAnimationsRule)
            .around(bundledDatabaseTestRule)
            .around(queueTestRule)
            .around(activityRule);

    @Test
    public void longPress_addsSong_showsQueueBottomSheet() {
        SongbookTestActions.search(SongbookTestFixtures.SEARCH_QUERY_SONG_A);
        SongbookTestActions.longPressSongInMainList(SongbookTestFixtures.SONG_A_TITLE);

        onView(withId(R.id.peekLayout)).check(matches(isDisplayed()));
        SongbookTestActions.expandQueueBottomSheet();
        onView(withId(R.id.queueRecyclerView))
                .check(RecyclerViewItemCountAssertion.withItemCount(1));
        onView(withId(R.id.queueRecyclerView))
                .check(RecyclerViewTitleAtPositionAssertion.withTitleAtPosition(
                        0, SongbookTestFixtures.SONG_A_TITLE));
    }

    @Test
    public void songActivityMenu_addsToQueue_updatesMainActivity() {
        SongbookTestActions.search(SongbookTestFixtures.SEARCH_QUERY_SONG_A);
        SongbookTestActions.openSongFromMainListByTitle(SongbookTestFixtures.SONG_A_TITLE);
        SongbookTestActions.addToQueueFromSongActivity();
        SongbookTestActions.navigateBack();
        SongbookTestActions.ensureMainActivityReady();

        onView(withId(R.id.peekLayout)).check(matches(isDisplayed()));
        SongbookTestActions.expandQueueBottomSheet();
        onView(withId(R.id.queueRecyclerView))
                .check(RecyclerViewItemCountAssertion.withItemCount(1));
        onView(withId(R.id.queueRecyclerView))
                .check(RecyclerViewTitleAtPositionAssertion.withTitleAtPosition(
                        0, SongbookTestFixtures.SONG_A_TITLE));
    }

    @Test
    public void addTwoSongs_expandSheet_showsClearButton() {
        SongbookTestActions.search(SongbookTestFixtures.SEARCH_QUERY_SONG_A);
        SongbookTestActions.longPressSongAtPositionInMainList(0);
        SongbookTestActions.longPressSongAtPositionInMainList(1);

        SongbookTestActions.expandQueueBottomSheet();
        onView(withId(R.id.clearAllQueueButton)).check(matches(isDisplayed()));
        onView(withId(R.id.queueRecyclerView))
                .check(RecyclerViewItemCountAssertion.withItemCount(2));
    }

    @Test
    public void clearAll_emptiesQueue_hidesBottomSheet() {
        SongbookTestActions.search(SongbookTestFixtures.SEARCH_QUERY_SONG_A);
        SongbookTestActions.longPressSongInMainList(SongbookTestFixtures.SONG_A_TITLE);

        SongbookTestActions.clearQueueViaUi();

        onView(withId(R.id.peekLayout)).check(matches(Matchers.not(isDisplayed())));
        onView(withId(R.id.queueRecyclerView))
                .check(RecyclerViewItemCountAssertion.withItemCount(0));
    }

    @Test
    public void dragReorder_changesQueueOrder() {
        SongbookTestActions.search(SongbookTestFixtures.SEARCH_QUERY_SONG_A);
        SongbookTestActions.longPressSongAtPositionInMainList(0);
        SongbookTestActions.longPressSongAtPositionInMainList(1);

        SongbookTestActions.expandQueueBottomSheet();
        String titleAt0 = QueueTestReaders.readQueueTitleAtPosition(0);
        String titleAt1 = QueueTestReaders.readQueueTitleAtPosition(1);

        onView(withId(R.id.queueRecyclerView))
                .perform(QueueDragActions.reorderItem(0, 1));

        onView(withId(R.id.queueRecyclerView))
                .check(RecyclerViewTitleAtPositionAssertion.withTitleAtPosition(0, titleAt1));
        onView(withId(R.id.queueRecyclerView))
                .check(RecyclerViewTitleAtPositionAssertion.withTitleAtPosition(1, titleAt0));
    }

    @Test
    public void horizontalDrag_deletesQueueItem() {
        SongbookTestActions.search(SongbookTestFixtures.SEARCH_QUERY_SONG_A);
        SongbookTestActions.longPressSongAtPositionInMainList(0);
        SongbookTestActions.longPressSongAtPositionInMainList(1);

        SongbookTestActions.expandQueueBottomSheet();
        String remainingTitle = QueueTestReaders.readQueueTitleAtPosition(1);

        onView(withId(R.id.queueRecyclerView))
                .perform(QueueDragActions.deleteItemByHorizontalDrag(0));

        onView(withId(R.id.queueRecyclerView))
                .check(RecyclerViewItemCountAssertion.withItemCount(1));
        onView(withId(R.id.queueRecyclerView))
                .check(RecyclerViewTitleAtPositionAssertion.withTitleAtPosition(0, remainingTitle));
    }
}
