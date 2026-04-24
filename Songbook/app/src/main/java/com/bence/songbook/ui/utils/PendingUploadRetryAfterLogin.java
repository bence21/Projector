package com.bence.songbook.ui.utils;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import com.bence.songbook.Memory;
import com.bence.songbook.R;
import com.bence.songbook.api.SongApiBean;
import com.bence.songbook.models.Song;
import com.bence.songbook.repository.impl.ormLite.SongRepositoryImpl;

/**
 * Runs a single upload retry after manual login when the user left a screen
 * (e.g. NewSongActivity) before login completed. Uses {@link Memory#consumePendingUploadRetrySong()}.
 */
public final class PendingUploadRetryAfterLogin {

    private PendingUploadRetryAfterLogin() {
    }

    /**
     * If a pending song was stored (new-song upload auth failure), upload it once, then show success
     * or failure feedback. Pending song is always consumed first so this does not loop.
     */
    public static void runIfPending(Context anyContext) {
        Song pending = Memory.getInstance().consumePendingUploadRetrySong();
        if (pending == null) {
            return;
        }
        final Context appContext = anyContext.getApplicationContext();
        final Song song = pending;
        new Thread(() -> {
            SongRepositoryImpl songRepository = new SongRepositoryImpl(appContext);
            SongApiBean songApiBean = new SongApiBean();
            SongApiBean.SongUploadResult uploadResult = songApiBean.uploadSong(song, appContext);
            new Handler(Looper.getMainLooper()).post(() -> {
                Song uploadedSong = uploadResult.getSong();
                if (uploadedSong != null && uploadedSong.getUuid() != null
                        && !uploadedSong.getUuid().trim().isEmpty()) {
                    song.setUuid(uploadedSong.getUuid());
                    song.setModifiedDate(uploadedSong.getModifiedDate());
                    songRepository.save(song);
                    Toast.makeText(appContext, R.string.successfully_uploaded, Toast.LENGTH_SHORT).show();
                } else {
                    SongUploadFailureUi.showBackgroundUploadFailureAfterRetry(appContext, uploadResult.getFailureKind());
                }
            });
        }).start();
    }
}
