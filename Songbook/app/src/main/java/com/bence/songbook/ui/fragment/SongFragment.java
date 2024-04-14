package com.bence.songbook.ui.fragment;

import android.content.Intent;
import android.widget.TextView;

import com.bence.songbook.models.Song;
import com.bence.songbook.models.SongVerse;
import com.bence.songbook.ui.activity.FullscreenActivity;

public class SongFragment extends BaseSongFragment {

    @Override
    protected void onSongVerseClick(int position) {
        final Intent fullScreenIntent = new Intent(getActivity(), FullscreenActivity.class);
        fullScreenIntent.putExtra("verseIndex", position);
        startActivity(fullScreenIntent);
    }

    public Song getSong() {
        return song;
    }

    @Override
    public SongFragment setSong(Song song) {
        return (SongFragment) super.setSong(song);
    }

    @Override
    protected void setText(SongVerse songVerse, TextView textView) {
        textView.setText(songVerse.getText());
    }
}
