package com.bence.songbook;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.clearText;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.pressKey;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import android.content.res.Resources;
import android.view.KeyEvent;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.bence.songbook.assertions.RecyclerViewItemCountAssertion;
import com.bence.songbook.ui.activity.MainActivity;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class ApplicationTest {

    @Rule
    public ActivityScenarioRule<MainActivity> activityScenarioRule
            = new ActivityScenarioRule<>(MainActivity.class);

    @Test
    public void searchForSong367_Test() {
        int expectedItemCount = 5;
        onView(withId(Resources.getSystem().getIdentifier("search_src_text", "id", "android")))
                .perform(clearText(), typeText("367"))
                .perform(pressKey(KeyEvent.KEYCODE_ENTER));
        sleep(400);
        onView(withId(R.id.songListView))
                .check(RecyclerViewItemCountAssertion.withItemCountGreater(expectedItemCount));
        onView(withText("Hit hangjai 367\nBaptista Gyülekezeti Énekeskönyv 297")).perform(click());
        sleep(100);
        onView(withText("Hit hangjai 367\nBaptista Gyülekezeti Énekeskönyv 297")).perform(click());
        // sleep(4000);
    }

    private void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}