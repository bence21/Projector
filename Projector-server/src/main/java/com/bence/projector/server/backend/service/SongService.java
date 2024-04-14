package com.bence.projector.server.backend.service;

import com.bence.projector.server.backend.model.Language;
import com.bence.projector.server.backend.model.Song;
import com.bence.projector.server.backend.model.User;

import java.util.Collection;
import java.util.Date;
import java.util.List;

public interface SongService extends BaseService<Song> {
    List<Song> findAllAfterModifiedDate(Date lastModifiedDate);

    List<Song> findAllByLanguage(String languageId);

    List<Song> findAllByLanguageAndModifiedDate(String languageId, Date date);

    List<Song> findAllByLanguageAndUser(String languageId, User user);

    List<Song> findAllByUploadedTrueAndDeletedTrueAndNotBackup();

    List<Song> findAllSimilar(Song song);

    Collection<Song> getSongsByLanguageForSimilar(Language language);

    boolean matches(Song song, Song song2);

    List<Song> findAllByVersionGroup(String versionGroup);

    Song getRandomSong(Language language);

    List<Song> findAllContainingYoutubeUrl();

    List<Song> findAllByLanguageContainingViews(String languageId);

    List<Song> findAllByLanguageContainingFavourites(String languageId);

    List<Song> findAllSongsLazy();

    void deleteByUuid(String uuid);

    @SuppressWarnings("unused")
    boolean isLanguageIsGood(Song song, Language language);

    @SuppressWarnings("unused")
    Language bestLanguage(Song song, List<Language> languages);

    List<Song> findAllSimilar(Song song, boolean checkDeleted);

    List<Song> findAllSimilar(Song song, boolean checkDeleted, Collection<Song> songs);

    @SuppressWarnings("unused")
    void enrollSongInMap(Song song);

    List<Song> findAllInReviewByLanguage(Language language);

    List<Song> findAllReviewedByUser(User user);

    Song findOneByUuid(String uuid);

    void startThreadFindForSong(String uuid);

    Song reloadSong(Song song);

    List<Song> filterSongsByCreatedEmail(List<Song> songs, String createdByEmail);
}
