package com.bence.projector.server.backend.service.inreview;

import com.bence.projector.common.dto.InReviewSongFilterCountsDTO;
import com.bence.projector.common.dto.PageResponse;
import com.bence.projector.common.dto.SongTitleDTO;
import com.bence.projector.server.api.assembler.SongTitleAssembler;
import com.bence.projector.server.backend.model.Language;
import com.bence.projector.server.backend.model.Song;
import com.bence.projector.server.backend.service.SongService;
import com.bence.projector.server.backend.service.SongWordValidationService;
import com.bence.projector.server.utils.SongLyricCharCounter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class InReviewSongQueryService {

    private final SongService songService;
    private final SongTitleAssembler songTitleAssembler;
    private final SongWordValidationService songWordValidationService;

    public InReviewSongQueryService(SongService songService,
                                    SongTitleAssembler songTitleAssembler,
                                    SongWordValidationService songWordValidationService) {
        this.songService = songService;
        this.songTitleAssembler = songTitleAssembler;
        this.songWordValidationService = songWordValidationService;
    }

    @Transactional(readOnly = true)
    public PageResponse<SongTitleDTO> page(InReviewSongQuery query) {
        List<Song> songs = loadFiltered(query, query.needsVerses());
        InReviewSongSorter.sort(songs, query.getSortType());
        List<Song> pageSongs = InReviewSongPaginator.page(songs, query.getPage(), query.getSize());
        return new PageResponse<>(toTitleDtos(pageSongs), songs.size());
    }

    @Transactional(readOnly = true)
    public InReviewSongFilterCountsDTO counts(Language language, String createdByEmail) {
        List<Song> songs = loadCandidates(language, createdByEmail, true);
        return InReviewSongFilterCounter.count(songs, songWordValidationService);
    }

    @Transactional(readOnly = true)
    public String exportCsv(InReviewSongQuery query) {
        List<Song> songs = loadFiltered(query, true);
        InReviewSongSorter.sort(songs, query.getSortType());
        return InReviewSongCsvExporter.export(songs, songWordValidationService);
    }

    private List<Song> loadFiltered(InReviewSongQuery query, boolean needVerses) {
        List<Song> songs = loadCandidates(query.getLanguage(), query.getCreatedByEmail(), needVerses);
        songs = InReviewSongTitleSearch.filter(songs, query.getTitleSearch());
        if (query.getMaxLyricChars() != null) {
            songs = InReviewSongShortLyricsFilter.filter(songs, query.getMaxLyricChars());
        }
        if (query.isBannedOrRejected()) {
            songs = InReviewSongBannedWordsFilter.filter(songs, songWordValidationService);
        }
        return songs;
    }

    private List<Song> loadCandidates(Language language, String createdByEmail, boolean needVerses) {
        if (language == null) {
            return new ArrayList<>();
        }
        List<Song> songs = needVerses
                ? songService.findAllInReviewByLanguageWithVerses(language)
                : songService.findAllInReviewByLanguage(language);
        if (songs == null) {
            return new ArrayList<>();
        }
        if (createdByEmail != null) {
            songs = songService.filterSongsByCreatedEmail(songs, createdByEmail);
        }
        return new ArrayList<>(songs);
    }

    private List<SongTitleDTO> toTitleDtos(List<Song> songs) {
        List<SongTitleDTO> dtos = new ArrayList<>(songs.size());
        for (Song song : songs) {
            SongTitleDTO dto = songTitleAssembler.createDto(song);
            dto.setLyricCharCount(SongLyricCharCounter.count(song));
            dtos.add(dto);
        }
        return dtos;
    }
}
