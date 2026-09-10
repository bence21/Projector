package com.bence.songbook.ui.utils;

import static com.bence.songbook.ui.activity.MainActivity.getOrdinalNumberText;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bence.songbook.R;
import com.bence.songbook.models.QueueSong;
import com.bence.songbook.models.Song;

public class QueueSongAdapter extends ListAdapter<QueueSong, QueueSongAdapter.ViewHolder> {

    public interface DragStartListener {
        void onStartDrag(QueueSongAdapter.ViewHolder holder);
    }

    public interface ItemClickListener {
        void onItemClick(int position);
    }

    private final DragStartListener dragStartListener;
    private final ItemClickListener itemClickListener;
    private final boolean shortCollectionName;

    public QueueSongAdapter(DragStartListener dragStartListener,
                            ItemClickListener itemClickListener,
                            boolean shortCollectionName) {
        super(new QueueSongDiffCallback());
        this.dragStartListener = dragStartListener;
        this.itemClickListener = itemClickListener;
        this.shortCollectionName = shortCollectionName;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    @SuppressLint("ClickableViewAccessibility")
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        QueueSong item = getItem(position);
        if (item == null) {
            return;
        }
        Song song = item.getSong();
        if (song == null) {
            return;
        }
        holder.ordinalNumberTextView.setText(getOrdinalNumberText(song, shortCollectionName));
        holder.titleTextView.setText(song.getTitle());
        holder.starImageView.setVisibility(song.isFavourite() ? View.VISIBLE : View.INVISIBLE);

        holder.grabImageView.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                dragStartListener.onStartDrag(holder);
                return false;
            }
            return true;
        });

        holder.rowLayout.setOnClickListener(v -> {
            int adapterPosition = holder.getBindingAdapterPosition();
            if (adapterPosition != RecyclerView.NO_POSITION) {
                itemClickListener.onItemClick(adapterPosition);
            }
        });
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final LinearLayout rowLayout;
        final TextView ordinalNumberTextView;
        final TextView titleTextView;
        final ImageView starImageView;
        final View grabImageView;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            rowLayout = itemView.findViewById(R.id.lytPattern);
            ordinalNumberTextView = itemView.findViewById(R.id.ordinalNumberTextView);
            titleTextView = itemView.findViewById(R.id.titleTextView);
            starImageView = itemView.findViewById(R.id.starImageView);
            grabImageView = itemView.findViewById(R.id.imageViewGrab);
        }
    }
}
