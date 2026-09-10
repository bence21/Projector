package com.bence.songbook;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.matcher.ViewMatchers;

import org.hamcrest.Matcher;

public final class QueueTestReaders {

    private QueueTestReaders() {
    }

    public static String readQueueTitleAtPosition(int position) {
        final String[] result = new String[1];
        onView(withId(R.id.queueRecyclerView)).perform(new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return ViewMatchers.isAssignableFrom(RecyclerView.class);
            }

            @Override
            public String getDescription() {
                return "Read queue title at position " + position;
            }

            @Override
            public void perform(UiController uiController, View view) {
                RecyclerView recyclerView = (RecyclerView) view;
                RecyclerView.ViewHolder holder =
                        recyclerView.findViewHolderForAdapterPosition(position);
                if (holder == null) {
                    throw new IllegalStateException("No ViewHolder at position " + position);
                }
                TextView titleTextView = holder.itemView.findViewById(R.id.titleTextView);
                if (titleTextView == null) {
                    throw new IllegalStateException("titleTextView not found at position " + position);
                }
                result[0] = titleTextView.getText().toString();
            }
        });
        return result[0];
    }
}
