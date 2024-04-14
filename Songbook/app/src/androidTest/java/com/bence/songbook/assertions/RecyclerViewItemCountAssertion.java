package com.bence.songbook.assertions;

import static androidx.test.espresso.matcher.ViewMatchers.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertNotNull;

import android.view.View;

import androidx.recyclerview.widget.RecyclerView;
import androidx.test.espresso.NoMatchingViewException;
import androidx.test.espresso.ViewAssertion;

import com.bence.songbook.ui.activity.MainActivity;

public class RecyclerViewItemCountAssertion implements ViewAssertion {

    private final int expectedCount;
    private final RecyclerViewItemCountType type;

    public RecyclerViewItemCountAssertion(int expectedCount, RecyclerViewItemCountType type) {
        this.expectedCount = expectedCount;
        this.type = type;
    }

    @Override
    public void check(View view, NoMatchingViewException noViewFoundException) {
        if (noViewFoundException != null) {
            throw noViewFoundException;
        }

        RecyclerView recyclerView = (RecyclerView) view;
        MainActivity.SongAdapter adapter = (MainActivity.SongAdapter) recyclerView.getAdapter();
        assertNotNull(adapter);
        int itemCount = adapter.getItemCount();
        if (type == RecyclerViewItemCountType.EXACT) {
            assertThat(itemCount, is(expectedCount));
        } else if (type == RecyclerViewItemCountType.GREATER_THAN) {
            assertThat(itemCount, greaterThan(expectedCount));
        }
    }

    public static RecyclerViewItemCountAssertion withItemCount(int expectedCount) {
        return new RecyclerViewItemCountAssertion(expectedCount, RecyclerViewItemCountType.EXACT);
    }

    public static RecyclerViewItemCountAssertion withItemCountGreater(int expectedCount) {
        return new RecyclerViewItemCountAssertion(expectedCount, RecyclerViewItemCountType.GREATER_THAN);
    }
}
