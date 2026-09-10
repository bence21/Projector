package com.bence.songbook.actions;

import static androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom;

import android.view.View;

import androidx.recyclerview.widget.RecyclerView;
import androidx.test.espresso.PerformException;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;

import com.bence.songbook.assertions.RecyclerViewSongInspector;

import org.hamcrest.Matcher;

public final class RecyclerViewSongActions {

    private RecyclerViewSongActions() {
    }

    public static ViewAction ensureSongVisible(String title, String ordinalLabel) {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return isAssignableFrom(RecyclerView.class);
            }

            @Override
            public String getDescription() {
                return "ensure song is visible: " + title;
            }

            @Override
            public void perform(UiController uiController, View view) {
                RecyclerView recyclerView = (RecyclerView) view;
                int position = RecyclerViewSongInspector.findPositionByTitle(
                        recyclerView, title, ordinalLabel, uiController);
                if (position < 0) {
                    int itemCount = recyclerView.getAdapter() != null
                            ? recyclerView.getAdapter().getItemCount() : 0;
                    throw new PerformException.Builder()
                            .withActionDescription(getDescription())
                            .withViewDescription(recyclerView.toString())
                            .withCause(new AssertionError(
                                    "Song not found with title \"" + title + "\" among "
                                            + itemCount + " list items"))
                            .build();
                }
                recyclerView.scrollToPosition(position);
                uiController.loopMainThreadUntilIdle();
            }
        };
    }

    public static ViewAction clickSongWithTitle(String title, String ordinalLabel) {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return isAssignableFrom(RecyclerView.class);
            }

            @Override
            public String getDescription() {
                return "click song with title: " + title;
            }

            @Override
            public void perform(UiController uiController, View view) {
                RecyclerView recyclerView = (RecyclerView) view;
                int position = RecyclerViewSongInspector.findPositionByTitle(
                        recyclerView, title, ordinalLabel, uiController);
                if (position < 0) {
                    int itemCount = recyclerView.getAdapter() != null
                            ? recyclerView.getAdapter().getItemCount() : 0;
                    throw new PerformException.Builder()
                            .withActionDescription(getDescription())
                            .withViewDescription(recyclerView.toString())
                            .withCause(new AssertionError(
                                    "Song not found with title \"" + title + "\" among "
                                            + itemCount + " list items"))
                            .build();
                }
                recyclerView.scrollToPosition(position);
                uiController.loopMainThreadUntilIdle();
                RecyclerView.ViewHolder holder =
                        recyclerView.findViewHolderForAdapterPosition(position);
                if (holder == null) {
                    throw new PerformException.Builder()
                            .withActionDescription(getDescription())
                            .withViewDescription(recyclerView.toString())
                            .withCause(new IllegalStateException(
                                    "No ViewHolder at position " + position))
                            .build();
                }
                holder.itemView.performClick();
            }
        };
    }
}
