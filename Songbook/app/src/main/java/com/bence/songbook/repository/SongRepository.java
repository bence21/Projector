package com.bence.songbook.repository;

import android.widget.ProgressBar;

import com.bence.projector.common.dto.SongViewsDTO;
import com.bence.songbook.models.Language;
import com.bence.songbook.models.Song;

import java.util.List;

public interface SongRepository extends BaseRepository<Song> {
    void save(List<Song> newSongs, ProgressBar progressBar);

    Song findByUUID(String uuid);

    List<Song> findAllByVersionGroup(String versionGroup);

    void saveViews(List<SongViewsDTO> songViewsDTOS);

    List<Song> findAllExceptAsDeleted();

    long sumAccessedTimesByLanguage(Language language);

    long countByLanguage(Language language);
}
