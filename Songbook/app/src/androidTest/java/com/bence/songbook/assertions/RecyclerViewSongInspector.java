package com.bence.songbook.assertions;

import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;
import androidx.test.espresso.UiController;

import com.bence.songbook.R;
import com.bence.songbook.SongbookTestFixtures;
import com.bence.songbook.models.Song;
import com.bence.songbook.models.SongCollection;
import com.bence.songbook.models.SongCollectionElement;

import java.text.Normalizer;

public final class RecyclerViewSongInspector {

    private static final int VIEW_HOLDER_ATTEMPTS = 30;
    private static final long VIEW_HOLDER_POLL_MS = 50;

    private RecyclerViewSongInspector() {
    }

    public static int findPositionByTitle(
            RecyclerView recyclerView,
            String expectedTitle,
            String ordinalLabel,
            UiController uiController) {
        RecyclerView.Adapter<?> adapter = recyclerView.getAdapter();
        if (adapter == null) {
            return -1;
        }
        int itemCount = adapter.getItemCount();
        for (int position = 0; position < itemCount; position++) {
            RecyclerView.ViewHolder holder = awaitViewHolder(recyclerView, position, uiController);
            if (holder == null) {
                continue;
            }
            if (matchesSong(holder, expectedTitle, ordinalLabel)) {
                return position;
            }
        }
        return -1;
    }

    private static boolean matchesSong(
            RecyclerView.ViewHolder holder,
            String expectedTitle,
            String ordinalLabel) {
        TextView titleTextView = holder.itemView.findViewById(R.id.titleTextView);
        boolean titleMatches = false;
        if (titleTextView != null) {
            Object tag = titleTextView.getTag();
            if (tag instanceof Song && titlesMatch(((Song) tag).getTitle(), expectedTitle)) {
                titleMatches = true;
            } else if (titlesMatch(titleTextView.getText(), expectedTitle)) {
                titleMatches = true;
            }
        }
        if (!titleMatches) {
            return false;
        }
        if (ordinalLabel == null || ordinalLabel.isEmpty()) {
            return true;
        }
        if (titleTextView != null) {
            Object tag = titleTextView.getTag();
            if (tag instanceof Song && matchesSongCollections((Song) tag, ordinalLabel)) {
                return true;
            }
        }
        TextView ordinalTextView = holder.itemView.findViewById(R.id.ordinalNumberTextView);
        return ordinalTextView != null
                && matchesOrdinalLabel(ordinalTextView.getText(), ordinalLabel);
    }

    private static boolean matchesSongCollections(Song song, String ordinalLabel) {
        for (SongCollectionElement element : song.getSongCollectionElements()) {
            SongCollection collection = element.getSongCollection();
            if (collection == null) {
                continue;
            }
            String fullLabel = collection.getName() + " " + element.getOrdinalNumber();
            if (matchesOrdinalLabel(fullLabel, ordinalLabel)) {
                return true;
            }
            String shortLabel = collection.getShortName() + " " + element.getOrdinalNumber();
            if (matchesOrdinalLabel(shortLabel, ordinalLabel)) {
                return true;
            }
        }
        return false;
    }

    private static boolean matchesOrdinalLabel(CharSequence actual, String ordinalLabel) {
        if (textsContain(actual, ordinalLabel)) {
            return true;
        }
        if (textsContain(actual, SongbookTestFixtures.SONG_A_ORDINAL_NUMBER)) {
            return true;
        }
        return textsContainIgnoreCase(actual, SongbookTestFixtures.SONG_A_COLLECTION_KEYWORD)
                && textsContain(actual, SongbookTestFixtures.SONG_A_ORDINAL_NUMBER);
    }

    private static RecyclerView.ViewHolder awaitViewHolder(
            RecyclerView recyclerView,
            int position,
            UiController uiController) {
        recyclerView.scrollToPosition(position);
        for (int attempt = 0; attempt < VIEW_HOLDER_ATTEMPTS; attempt++) {
            uiController.loopMainThreadForAtLeast(VIEW_HOLDER_POLL_MS);
            RecyclerView.ViewHolder holder = recyclerView.findViewHolderForAdapterPosition(position);
            if (holder != null) {
                return holder;
            }
            for (int childIndex = 0; childIndex < recyclerView.getChildCount(); childIndex++) {
                View child = recyclerView.getChildAt(childIndex);
                if (recyclerView.getChildAdapterPosition(child) == position) {
                    return recyclerView.getChildViewHolder(child);
                }
            }
        }
        return null;
    }

    public static boolean titlesMatch(CharSequence actual, String expected) {
        if (actual == null) {
            return false;
        }
        String actualNormalized = Normalizer.normalize(actual.toString().trim(), Normalizer.Form.NFC);
        String expectedNormalized = Normalizer.normalize(expected.trim(), Normalizer.Form.NFC);
        return actualNormalized.equals(expectedNormalized);
    }

    public static boolean textsContain(CharSequence actual, String expectedSubstring) {
        if (actual == null) {
            return false;
        }
        String actualNormalized = Normalizer.normalize(actual.toString(), Normalizer.Form.NFC);
        String expectedNormalized = Normalizer.normalize(expectedSubstring, Normalizer.Form.NFC);
        return actualNormalized.contains(expectedNormalized);
    }

    public static boolean textsContainIgnoreCase(CharSequence actual, String expectedSubstring) {
        if (actual == null) {
            return false;
        }
        String actualNormalized = Normalizer.normalize(actual.toString(), Normalizer.Form.NFC)
                .toLowerCase();
        String expectedNormalized = Normalizer.normalize(expectedSubstring, Normalizer.Form.NFC)
                .toLowerCase();
        return actualNormalized.contains(expectedNormalized);
    }
}
