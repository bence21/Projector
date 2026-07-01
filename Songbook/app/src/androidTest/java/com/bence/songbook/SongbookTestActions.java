package com.bence.songbook;

import static androidx.test.espresso.Espresso.closeSoftKeyboard;
import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static androidx.test.espresso.Espresso.pressBackUnconditionally;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.longClick;
import static androidx.test.espresso.action.ViewActions.pressKey;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition;
import static androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withContentDescription;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;

import android.content.res.Resources;
import android.view.KeyEvent;

import androidx.recyclerview.widget.RecyclerView;
import androidx.test.platform.app.InstrumentationRegistry;

import com.bence.songbook.actions.RecyclerViewSongActions;
import com.bence.songbook.assertions.RecyclerViewItemCountAssertion;
import com.bence.songbook.ui.activity.MainActivity;

import org.hamcrest.Matchers;

public final class SongbookTestActions {

    private static final long MAIN_ACTIVITY_READY_TIMEOUT_MS = 30_000;
    private static final long POLL_INTERVAL_MS = 500;

    private static volatile boolean mainActivityReadyOnce;

    private SongbookTestActions() {
    }

    public static void resetMainActivityReadyState() {
        mainActivityReadyOnce = false;
    }

    public static void ensureMainActivityReady() {
        if (mainActivityReadyOnce && isSongListReady()) {
            return;
        }

        long deadline = System.currentTimeMillis() + MAIN_ACTIVITY_READY_TIMEOUT_MS;
        while (System.currentTimeMillis() < deadline) {
            if (isSongListReady()) {
                mainActivityReadyOnce = true;
                return;
            }
            sleep(POLL_INTERVAL_MS);
        }
        throw new AssertionError(
                "MainActivity did not become ready within " + MAIN_ACTIVITY_READY_TIMEOUT_MS + "ms");
    }

    public static void waitForLanguageListReady(long timeoutMs) {
        long deadline = System.currentTimeMillis() + timeoutMs;
        while (System.currentTimeMillis() < deadline) {
            if (isLanguageListReady()) {
                return;
            }
            sleep(POLL_INTERVAL_MS);
        }
        throw new AssertionError("Language list did not become ready within " + timeoutMs + "ms");
    }

    public static void selectLanguageForDownload(String englishName) {
        onView(allOf(withId(R.id.checkBox1), withText(englishName))).perform(click());
    }

    public static void downloadSelectedLanguages() {
        onView(withId(R.id.languageActivity_downloadButton)).perform(click());
    }

    public static void waitForSongDownloadComplete(long timeoutMs) {
        waitForSongDownloadComplete(
                timeoutMs,
                SongbookTestFixtures.SONG_A_TITLE,
                SongbookTestFixtures.MIN_DOWNLOADED_HUNGARIAN_SONGS);
    }

    public static void waitForSongDownloadComplete(
            long timeoutMs,
            String expectedSongTitle,
            int minPersistedSongCount) {
        long deadline = System.currentTimeMillis() + timeoutMs;
        while (System.currentTimeMillis() < deadline) {
            if (isSongListReady()
                    && SongbookTestSetup.hasPersistedSongA()
                    && SongbookTestSetup.getPersistedSongCount() >= minPersistedSongCount
                    && getMainListItemCount() >= minPersistedSongCount) {
                mainActivityReadyOnce = true;
                return;
            }
            sleep(POLL_INTERVAL_MS);
        }
        throw new AssertionError(
                "Song download did not complete within " + timeoutMs + "ms"
                        + " (missing fixture song: " + expectedSongTitle
                        + ", persisted songs: " + SongbookTestSetup.getPersistedSongCount()
                        + ", required: " + minPersistedSongCount + ")");
    }

    public static void verifyDownloadedSongAVisibleInUi() {
        search(SongbookTestFixtures.SEARCH_QUERY_SONG_A_TITLE);
        waitForMainListSong(
                SongbookTestFixtures.SONG_A_TITLE,
                SongbookTestFixtures.SONG_A_ORDINAL_LABEL,
                30_000);
    }

    public static void search(String query) {
        ensureMainActivityReady();
        onView(withId(Resources.getSystem().getIdentifier("search_src_text", "id", "android")))
                .perform(replaceText(query), pressKey(KeyEvent.KEYCODE_ENTER));
    }

    public static void longPressSongInMainList(String titleSubstring) {
        onView(allOf(
                withText(Matchers.containsString(titleSubstring)),
                isDescendantOfA(withId(R.id.songListView))))
                .perform(longClick());
    }

    public static void longPressSongAtPositionInMainList(int position) {
        onView(withId(R.id.songListView)).perform(actionOnItemAtPosition(position, longClick()));
    }

    public static void openSongFromMainList(String title) {
        openSongFromMainList(title, null);
    }

    public static void openSongFromMainList(String title, String ordinalLabel) {
        waitForMainListSong(title, ordinalLabel, 15_000);
        onView(withId(R.id.songListView))
                .perform(RecyclerViewSongActions.clickSongWithTitle(title, ordinalLabel));
    }

    private static void waitForMainListSong(String title, String ordinalLabel, long timeoutMs) {
        long deadline = System.currentTimeMillis() + timeoutMs;
        Throwable lastFailure = null;
        while (System.currentTimeMillis() < deadline) {
            try {
                onView(withId(R.id.songListView))
                        .perform(RecyclerViewSongActions.ensureSongVisible(title, ordinalLabel));
                return;
            } catch (Throwable failure) {
                lastFailure = failure;
                sleep(POLL_INTERVAL_MS);
            }
        }
        AssertionError error = new AssertionError(
                "Song \"" + title + "\" did not appear in main list within " + timeoutMs + "ms");
        if (lastFailure != null) {
            error.initCause(lastFailure);
        }
        throw error;
    }

    public static void openSongFromMainListByTitle(String title) {
        openSongFromMainList(title);
    }

    public static void addToQueueFromSongActivity() {
        try {
            onView(withContentDescription("Add to queue")).perform(click());
        } catch (Throwable toolbarNotVisible) {
            openActionBarOverflowOrOptionsMenu(
                    InstrumentationRegistry.getInstrumentation().getTargetContext());
            onView(withText("Add to queue")).perform(click());
        }
    }

    public static void expandQueueBottomSheet() {
        closeSoftKeyboard();
        long deadline = System.currentTimeMillis() + 15_000;
        while (System.currentTimeMillis() < deadline) {
            try {
                onView(withId(R.id.buttonLayout)).check(matches(isDisplayed()));
                return;
            } catch (Throwable ignored) {
                forceExpandQueueBottomSheetOnMainActivity();
                try {
                    onView(withId(R.id.peekLayout)).perform(click());
                } catch (Throwable ignoredClick) {
                    sleep(POLL_INTERVAL_MS);
                }
                sleep(300);
            }
        }
        throw new AssertionError("Queue bottom sheet did not expand");
    }

    private static void forceExpandQueueBottomSheetOnMainActivity() {
        InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> {
            MainActivity activity = Memory.getInstance().getMainActivity();
            if (activity != null) {
                activity.testingExpandQueueBottomSheet();
            }
        });
    }

    public static void clearQueueViaUi() {
        expandQueueBottomSheet();
        onView(withId(R.id.clearAllQueueButton)).perform(click());
    }

    public static void navigateBack() {
        pressBackUnconditionally();
    }

    private static boolean isSongListReady() {
        try {
            onView(withId(R.id.songListView)).check(matches(isDisplayed()));
            onView(withId(R.id.songListView))
                    .check(RecyclerViewItemCountAssertion.withItemCountGreater(0));
            return true;
        } catch (Throwable ignored) {
            return false;
        }
    }

    private static int getMainListItemCount() {
        final int[] count = {-1};
        try {
            onView(withId(R.id.songListView)).check((view, noViewFoundException) -> {
                RecyclerView recyclerView = (RecyclerView) view;
                if (recyclerView.getAdapter() != null) {
                    count[0] = recyclerView.getAdapter().getItemCount();
                }
            });
        } catch (Throwable ignored) {
            return -1;
        }
        return count[0];
    }

    private static boolean isLanguageListReady() {
        try {
            onData(Matchers.anything())
                    .inAdapterView(withId(R.id.languageActivity_listView))
                    .atPosition(0)
                    .check(matches(isDisplayed()));
            return true;
        } catch (Throwable ignored) {
            return false;
        }
    }

    private static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }
}
