package com.bence.projector.server.backend.service.impl;

import com.bence.projector.server.backend.model.Language;
import com.bence.projector.server.backend.model.Song;
import com.bence.projector.server.backend.model.SongVerse;
import com.bence.projector.server.backend.model.Suggestion;
import com.bence.projector.server.backend.repository.SongVerseRepository;
import com.bence.projector.server.backend.repository.SuggestionRepository;
import com.bence.projector.server.backend.service.SongVerseService;
import com.bence.projector.server.backend.service.SuggestionService;
import com.bence.projector.server.utils.AppProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static com.bence.projector.server.backend.service.util.QueryUtil.getStatement;
import static com.bence.projector.server.utils.MemoryUtil.getEmptyList;

@Service
public class SuggestionServiceImpl extends BaseServiceImpl<Suggestion> implements SuggestionService {
    private final SuggestionRepository suggestionRepository;
    private final SongVerseRepository songVerseRepository;
    private final SongVerseService songVerseService;
    private final ConcurrentHashMap<String, Suggestion> suggestionHashMap;
    private long lastModifiedDateTime = 0;

    @Autowired
    public SuggestionServiceImpl(SuggestionRepository suggestionRepository, SongVerseRepository songVerseRepository, SongVerseService songVerseService) {
        this.suggestionRepository = suggestionRepository;
        this.songVerseRepository = songVerseRepository;
        this.songVerseService = songVerseService;
        suggestionHashMap = new ConcurrentHashMap<>(500);
    }

    @Override
    public List<Suggestion> findAll() {
        List<Suggestion> suggestions = new ArrayList<>(suggestionHashMap.size());
        if (!AppProperties.getInstance().useMoreMemory()) {
            Iterable<Suggestion> all = suggestionRepository.findAll();
            for (Suggestion suggestion : all) {
                suggestions.add(suggestion);
            }
        } else {
            suggestions.addAll(getSuggestions());
        }
        return suggestions;
    }

    @Override
    public Suggestion findOneByUuid(String id) {
        if (suggestionHashMap.containsKey(id)) {
            return suggestionHashMap.get(id);
        }
        Suggestion suggestion = suggestionRepository.findOneByUuid(id);
        if (AppProperties.getInstance().useMoreMemory()) {
            suggestionHashMap.put(id, suggestion);
        }
        return suggestion;
    }

    @Override
    public List<Suggestion> findAllByLanguageAndCustomFetch(Language language) {
        try {
            ResultSet resultSet = getResultSet(language);
            return getSuggestionsFromResultSet(resultSet);
        } catch (SQLException e) {
            e.printStackTrace();
            return getEmptyList();
        }
    }

    private List<Suggestion> getSuggestionsFromResultSet(ResultSet resultSet) throws SQLException {
        List<Suggestion> suggestions = new ArrayList<>();
        while (resultSet.next()) {
            Suggestion suggestion = new Suggestion();
            suggestion.setUuid(resultSet.getString("uuid"));
            suggestion.setTitle(resultSet.getString("title"));
            suggestion.setCreatedDate(getDate(resultSet, "created_date"));
            suggestion.setModifiedDate(getDate(resultSet, "modified_date"));
            suggestion.setCreatedByEmail(resultSet.getString("created_by_email"));
            suggestion.setApplied(resultSet.getBoolean("applied"));
            suggestion.setDescription(resultSet.getString("description"));
            suggestion.setReviewed(resultSet.getBoolean("reviewed"));
            suggestion.setSongUuid(resultSet.getString("song_uuid"));
            suggestion.setYoutubeUrl(resultSet.getString("youtube_url"));
            suggestions.add(suggestion);
        }
        return suggestions;
    }

    private Date getDate(ResultSet resultSet, String columnLabel) throws SQLException {
        Timestamp timestamp = resultSet.getTimestamp(columnLabel);
        if (timestamp != null) {
            return new Date(timestamp.getTime());
        }
        return null;
    }

    private ResultSet getResultSet(Language language) throws SQLException {
        Statement statement = getStatement();
        String sql = "select suggestion.uuid, " +
                "song.uuid as song_uuid, " +
                "applied, " +
                "suggestion.created_by_email, " +
                "suggestion.created_date, " +
                "description, " +
                "suggestion.modified_date, " +
                "reviewed, " +
                "suggestion.title, " +
                "suggestion.youtube_url " +
                "from suggestion";
        sql += " join song on (suggestion.song_id = song.id)";
        sql = getConditionSqlByLanguage(language, sql);
        return statement.executeQuery(sql);
    }

    private String getConditionSqlByLanguage(Language language, String sql) {
        if (language != null) {
            sql += " where song.language_id = " + language.getId();
        }
        sql += " and song.is_back_up is null";
        sql += " and ((song.reviewer_erased is null) or (song.reviewer_erased = 0))";
        return sql;
    }

    @Override
    public List<Suggestion> findAllByLanguage(Language language) {
        List<Suggestion> suggestionsByLanguage = suggestionRepository.findAllBySongLanguageId(language.getId());
        if (AppProperties.getInstance().useMoreMemory()) {
            return getSuggestionsFromMap(suggestionsByLanguage);
        }
        return suggestionsByLanguage;
    }

    private List<Suggestion> getSuggestionsFromMap(List<Suggestion> suggestionsByLanguage) {
        ArrayList<Suggestion> suggestions = new ArrayList<>();
        for (Suggestion suggestion : suggestionsByLanguage) {
            suggestions.add(findOneByUuid(suggestion.getUuid()));
        }
        return suggestions;
    }

    @Override
    public List<Suggestion> findAllBySong(Song song) {
        List<Suggestion> allBySongId = song.getSuggestions();
        List<Suggestion> suggestions = new ArrayList<>(allBySongId.size());
        for (Suggestion suggestion : allBySongId) {
            suggestions.add(findOneByUuid(suggestion.getUuid()));
        }
        return suggestions;
    }

    private Collection<Suggestion> getSuggestions() {
        if (suggestionHashMap.isEmpty()) {
            for (Suggestion suggestion : suggestionRepository.findAll()) {
                putInMapAndCheckLastModifiedDate(suggestion);
            }
        } else {
            for (Suggestion suggestion : suggestionRepository.findAllByModifiedDateGreaterThan(new Date(lastModifiedDateTime))) {
                if (!suggestionHashMap.containsKey(suggestion.getUuid())) {
                    putInMapAndCheckLastModifiedDate(suggestion);
                } else {
                    suggestionHashMap.replace(suggestion.getUuid(), suggestion);
                    checkLastModifiedDate(suggestion);
                }
            }
        }
        return suggestionHashMap.values();
    }

    private void checkLastModifiedDate(Suggestion suggestion) {
        Date modifiedDate = suggestion.getModifiedDate();
        if (modifiedDate == null) {
            return;
        }
        long time = modifiedDate.getTime();
        if (time > lastModifiedDateTime) {
            lastModifiedDateTime = time;
        }
    }

    private void putInMapAndCheckLastModifiedDate(Suggestion suggestion) {
        if (AppProperties.getInstance().useMoreMemory()) {
            suggestionHashMap.put(suggestion.getUuid(), suggestion);
        }
        checkLastModifiedDate(suggestion);
    }

    @Override
    public Suggestion save(Suggestion suggestion) {
        List<SongVerse> verses = getCopyOfVerses(suggestion.getVerses());
        suggestionRepository.save(suggestion);
        songVerseRepository.deleteAllBySuggestionId(suggestion.getId());
        songVerseService.saveAllByRepository(verses);
        return super.save(suggestion);
    }

    private List<SongVerse> getCopyOfVerses(List<SongVerse> verses) {
        if (verses == null) {
            return null;
        }
        return new ArrayList<>(verses);
    }

    @Override
    public Iterable<Suggestion> save(List<Suggestion> models) {
        for (Suggestion suggestion : models) {
            save(suggestion);
        }
        return models;
    }
}
