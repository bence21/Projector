package com.bence.songbook.assertions;

import static androidx.test.espresso.matcher.ViewMatchers.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;

import android.view.View;
import android.widget.ImageView;

import androidx.recyclerview.widget.RecyclerView;
import androidx.test.espresso.NoMatchingViewException;
import androidx.test.espresso.ViewAssertion;

import com.bence.songbook.R;

public class RecyclerViewStarAtPositionAssertion implements ViewAssertion {

    private final int position;
    private final boolean expectedVisible;

    public RecyclerViewStarAtPositionAssertion(int position, boolean expectedVisible) {
        this.position = position;
        this.expectedVisible = expectedVisible;
    }

    @Override
    public void check(View view, NoMatchingViewException noViewFoundException) {
        if (noViewFoundException != null) {
            throw noViewFoundException;
        }

        RecyclerView recyclerView = (RecyclerView) view;
        RecyclerView.ViewHolder holder = recyclerView.findViewHolderForAdapterPosition(position);
        assertNotNull("No ViewHolder at position " + position, holder);
        ImageView starImageView = holder.itemView.findViewById(R.id.starImageView);
        assertNotNull(starImageView);
        int expectedVisibility = expectedVisible ? View.VISIBLE : View.INVISIBLE;
        assertThat(starImageView.getVisibility(), is(expectedVisibility));
    }

    public static RecyclerViewStarAtPositionAssertion withStarVisibleAtPosition(int position) {
        return new RecyclerViewStarAtPositionAssertion(position, true);
    }

    public static RecyclerViewStarAtPositionAssertion withStarHiddenAtPosition(int position) {
        return new RecyclerViewStarAtPositionAssertion(position, false);
    }
}
