package com.bence.songbook.ui.utils;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.bence.songbook.models.Song;
import com.bence.songbook.ui.fragment.SongFragment;

import java.util.ArrayList;
import java.util.List;

public class PageAdapter extends FragmentStatePagerAdapter {

    private final List<Song> songs;
    private final List<SongFragment> fragments;

    public PageAdapter(FragmentManager fm, List<Song> songs) {
        super(fm);
        this.songs = songs;
        fragments = new ArrayList<>(songs.size());
        for (int i = 0; i < songs.size(); ++i) {
            fragments.add(null);
        }
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        SongFragment songFragment = new SongFragment();
        if (0 <= position && position < songs.size()) {
            SongFragment fragment = songFragment.setSong(songs.get(position));
            fragments.set(position, fragment);
            return fragment;
        }
        return songFragment;
    }

    @Override
    public int getCount() {
        return songs.size();
    }

    @Override
    public int getItemPosition(@NonNull Object object) {
        return POSITION_NONE;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        SongFragment ret = (SongFragment) super.instantiateItem(container, position);
        if (ret.getSong() == null) {
            SongFragment songFragment = fragments.get(position);
            if (songFragment != null) {
                return songFragment;
            } else {
                return getItem(position);
            }
        }
        return ret;
    }
}
