package com.bence.songbook.repository;

import com.bence.songbook.models.Language;

import java.util.List;

public interface LanguageRepository extends BaseRepository<Language> {
    List<Language> findAllSelectedForDownload();
}
