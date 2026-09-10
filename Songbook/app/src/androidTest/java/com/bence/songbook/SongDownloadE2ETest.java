package com.bence.songbook;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.bence.songbook.rules.DisableAnimationsRule;
import com.bence.songbook.rules.FreshDatabaseTestRule;
import com.bence.songbook.rules.RequiresNetworkRule;
import com.bence.songbook.ui.activity.MainActivity;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class SongDownloadE2ETest {

    private static final long LANGUAGE_LIST_TIMEOUT_MS = 60_000;
    private static final long DOWNLOAD_TIMEOUT_MS = 180_000;

    private final DisableAnimationsRule disableAnimationsRule = new DisableAnimationsRule();
    private final FreshDatabaseTestRule freshDatabaseTestRule = new FreshDatabaseTestRule();
    private final RequiresNetworkRule requiresNetworkRule = new RequiresNetworkRule();
    private final ActivityScenarioRule<MainActivity> activityRule =
            new ActivityScenarioRule<>(MainActivity.class);

    @Rule
    public final RuleChain ruleChain = RuleChain
            .outerRule(disableAnimationsRule)
            .around(freshDatabaseTestRule)
            .around(requiresNetworkRule)
            .around(activityRule);

    @Test
    public void freshInstall_downloadHungarian_persistsAndShowsSongs() {
        SongbookTestActions.resetMainActivityReadyState();
        SongbookTestActions.waitForLanguageListReady(LANGUAGE_LIST_TIMEOUT_MS);
        SongbookTestActions.selectLanguageForDownload(SongbookTestFixtures.HUNGARIAN_LANGUAGE_NAME);
        SongbookTestActions.downloadSelectedLanguages();
        SongbookTestActions.waitForSongDownloadComplete(DOWNLOAD_TIMEOUT_MS);
        SongbookTestActions.verifyDownloadedSongAVisibleInUi();
    }
}
