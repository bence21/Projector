package com.bence.songbook.ui.adapter;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bence.songbook.R;
import com.bence.songbook.models.SongVerse;

import java.util.ArrayList;
import java.util.List;

public class SectionTypeAdapter extends RecyclerView.Adapter<SectionTypeViewHolder> {

    private List<SongVerse> songVerses;
    private Context context;

    public SectionTypeAdapter(List<SongVerse> songVerses, Context context) {
        this.context = context;
        this.songVerses = new ArrayList<>();
        this.songVerses.addAll(songVerses);
    }

    @NonNull
    @Override
    public SectionTypeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // create a new view
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.content_section_type_list_row, parent, false);
        return new SectionTypeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final SectionTypeViewHolder holder, int position) {
        SongVerse songVerse = songVerses.get(position);
        holder.bind(songVerse, position);
        holder.textView.setText(songVerse.getSectionTypeStringWithCount(context));
    }

    @Override
    public int getItemCount() {
        return songVerses.size();
    }

}
