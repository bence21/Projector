package com.bence.songbook.assertions;

import static androidx.test.espresso.matcher.ViewMatchers.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;

import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;
import androidx.test.espresso.NoMatchingViewException;
import androidx.test.espresso.ViewAssertion;

import com.bence.songbook.R;

public class RecyclerViewTitleAtPositionAssertion implements ViewAssertion {

    private final int position;
    private final String expectedTitle;

    public RecyclerViewTitleAtPositionAssertion(int position, String expectedTitle) {
        this.position = position;
        this.expectedTitle = expectedTitle;
    }

    @Override
    public void check(View view, NoMatchingViewException noViewFoundException) {
        if (noViewFoundException != null) {
            throw noViewFoundException;
        }

        RecyclerView recyclerView = (RecyclerView) view;
        RecyclerView.ViewHolder holder = recyclerView.findViewHolderForAdapterPosition(position);
        assertNotNull("No ViewHolder at position " + position, holder);
        TextView titleTextView = holder.itemView.findViewById(R.id.titleTextView);
        assertNotNull(titleTextView);
        assertThat(titleTextView.getText().toString(), is(expectedTitle));
    }

    public static RecyclerViewTitleAtPositionAssertion withTitleAtPosition(int position, String expectedTitle) {
        return new RecyclerViewTitleAtPositionAssertion(position, expectedTitle);
    }
}
