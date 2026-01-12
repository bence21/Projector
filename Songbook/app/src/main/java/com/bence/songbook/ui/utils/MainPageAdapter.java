package com.bence.songbook.ui.utils;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.bence.songbook.models.Song;
import com.bence.songbook.ui.fragment.MainSongFragment;

import java.util.List;

public class MainPageAdapter extends FragmentStatePagerAdapter {

    private final List<Song> songs;

    public MainPageAdapter(FragmentManager fm, List<Song> songs) {
        super(fm);
        this.songs = songs;
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        MainSongFragment mainSongFragment = new MainSongFragment();
        if (0 <= position && position < songs.size()) {
            return mainSongFragment.setSong(songs.get(position));
        }
        return mainSongFragment;
    }

    @Override
    public int getCount() {
        return songs.size();
    }

    @Override
    public int getItemPosition(@NonNull Object object) {
        return POSITION_NONE;
    }

    /**
     * Safely update the songs list and notify the adapter of the change.
     * This method ensures atomic updates to prevent ViewPager crashes.
     *
     * @param newSongs The new list of songs to use
     */
    public void updateSongs(List<Song> newSongs) {
        this.songs.clear();
        this.songs.addAll(newSongs);
        notifyDataSetChanged();
    }
}
