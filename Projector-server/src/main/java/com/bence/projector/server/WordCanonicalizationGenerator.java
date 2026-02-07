package com.bence.projector.server;

import com.bence.projector.server.backend.service.ReviewedWordService;
import com.bence.projector.server.backend.service.SongService;
import com.bence.projector.server.utils.WordCanonicalizationUtil;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component // uncomment this to run the generator
@SuppressWarnings({"unused"})
public class WordCanonicalizationGenerator implements CommandLineRunner {

    private final ReviewedWordService reviewedWordService;
    private final SongService songService;

    public WordCanonicalizationGenerator(ReviewedWordService reviewedWordService,
                                         SongService songService) {
        this.reviewedWordService = reviewedWordService;
        this.songService = songService;
    }

    @Override
    public void run(String... args) throws Exception {
        try {
            WordCanonicalizationUtil.canonicalizeAllWords(reviewedWordService);
            WordCanonicalizationUtil.canonicalizeAllSongs(songService);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
