package com.bence.projector.server;

import com.bence.projector.server.backend.service.LanguageService;
import com.bence.projector.server.backend.service.ReviewedWordService;
import com.bence.projector.server.backend.service.SongService;
import com.bence.projector.server.utils.UnreviewedWordFileUtil;
import org.springframework.boot.CommandLineRunner;

// @Component // uncomment this to run the generator
@SuppressWarnings({"ClassCanBeRecord", "unused"})
public class UnreviewedWordFileGenerator implements CommandLineRunner {

    private final LanguageService languageService;
    private final ReviewedWordService reviewedWordService;
    private final SongService songService;

    public UnreviewedWordFileGenerator(LanguageService languageService,
                                       ReviewedWordService reviewedWordService,
                                       SongService songService) {
        this.languageService = languageService;
        this.reviewedWordService = reviewedWordService;
        this.songService = songService;
    }

    @Override
    public void run(String... args) throws Exception {
        try {
            UnreviewedWordFileUtil.generateUnreviewedWordsFile(
                    languageService,
                    reviewedWordService,
                    songService,
                    "5a2d253b8c270b37345af0c3", // Hungarian language UUID
                    "unreviewed_words.txt"
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
