package com.bence.songbook.ui.utils;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;

import com.bence.songbook.models.QueueSong;
import com.bence.songbook.models.Song;

import java.util.Objects;

public class QueueSongDiffCallback extends DiffUtil.ItemCallback<QueueSong> {

    @Override
    public boolean areItemsTheSame(@NonNull QueueSong oldItem, @NonNull QueueSong newItem) {
        Long oldId = oldItem.getId();
        Long newId = newItem.getId();
        if (oldId != null && newId != null) {
            return oldId.equals(newId);
        }
        if (oldItem == newItem) {
            return true;
        }
        Song oldSong = oldItem.getSong();
        Song newSong = newItem.getSong();
        if (oldSong != null && newSong != null) {
            if (oldSong.getId() != null && newSong.getId() != null) {
                return oldSong.getId().equals(newSong.getId());
            }
            if (oldSong.getUuid() != null && newSong.getUuid() != null) {
                return oldSong.getUuid().equals(newSong.getUuid());
            }
        }
        return false;
    }

    @Override
    public boolean areContentsTheSame(@NonNull QueueSong oldItem, @NonNull QueueSong newItem) {
        return oldItem.getQueueNumber() == newItem.getQueueNumber()
                && songsEqual(oldItem.getSong(), newItem.getSong());
    }

    private static boolean songsEqual(Song a, Song b) {
        if (a == b) {
            return true;
        }
        if (a == null || b == null) {
            return false;
        }
        return Objects.equals(a.getUuid(), b.getUuid())
                && a.isFavourite() == b.isFavourite();
    }
}
