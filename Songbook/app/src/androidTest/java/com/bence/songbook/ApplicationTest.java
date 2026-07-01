package com.bence.songbook;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.bence.songbook.assertions.RecyclerViewItemCountAssertion;
import com.bence.songbook.rules.BundledDatabaseTestRule;
import com.bence.songbook.rules.DisableAnimationsRule;
import com.bence.songbook.ui.activity.MainActivity;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class ApplicationTest {

    private final DisableAnimationsRule disableAnimationsRule = new DisableAnimationsRule();
    private final BundledDatabaseTestRule bundledDatabaseTestRule = new BundledDatabaseTestRule();
    private final ActivityScenarioRule<MainActivity> activityScenarioRule
            = new ActivityScenarioRule<>(MainActivity.class);

    @Rule
    public final RuleChain ruleChain = RuleChain
            .outerRule(disableAnimationsRule)
            .around(bundledDatabaseTestRule)
            .around(activityScenarioRule);

    @Test
    public void searchForSong367_Test() {
        int expectedItemCount = 5;
        SongbookTestActions.search(SongbookTestFixtures.SEARCH_QUERY_SONG_A);
        onView(withId(R.id.songListView))
                .check(RecyclerViewItemCountAssertion.withItemCountGreater(expectedItemCount));
        SongbookTestActions.openSongFromMainList(
                SongbookTestFixtures.SONG_A_TITLE,
                SongbookTestFixtures.SONG_A_ORDINAL_LABEL);
        onView(withId(R.id.toolbarTitle))
                .check(matches(allOf(withText(SongbookTestFixtures.SONG_A_TITLE), isDisplayed())));
    }
}
