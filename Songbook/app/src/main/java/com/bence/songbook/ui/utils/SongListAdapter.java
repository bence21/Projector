package com.bence.songbook.ui.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import androidx.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.bence.songbook.R;
import com.bence.songbook.models.SongList;

import java.util.List;

public class SongListAdapter extends ArrayAdapter<SongList> {

    public SongListAdapter(Context context, int textViewResourceId, List<SongList> list) {
        super(context, textViewResourceId, list);
    }

    @SuppressLint("InflateParams")
    @NonNull
    @Override
    public View getView(final int position, View view, @NonNull ViewGroup parent) {
        Context context = getContext();
        if (null == view) {
            view = LayoutInflater.from(context).inflate(R.layout.song_list_row, null);
        }
        SongList item = getItem(position);
        if (item == null) {
            return view;
        }
        TextView titleTextView = view.findViewById(R.id.titleTextView);
        titleTextView.setText(item.getTitle());
        return view;
    }

}