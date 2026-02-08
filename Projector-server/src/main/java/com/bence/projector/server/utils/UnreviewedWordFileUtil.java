package com.bence.projector.server.utils;

import com.bence.projector.server.backend.model.Language;
import com.bence.projector.server.backend.model.ReviewedWord;
import com.bence.projector.server.backend.service.LanguageService;
import com.bence.projector.server.backend.service.ReviewedWordService;
import com.bence.projector.server.backend.service.SongService;
import com.bence.projector.server.utils.models.NormalizedWordBunch;
import com.bence.projector.server.utils.models.WordBunch;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.bence.projector.server.utils.SetLanguages.getNormalizedWordBunches;

public class UnreviewedWordFileUtil {

    /**
     * Generates a text file containing all unreviewed words for a specified language.
     * The file includes comprehensive details about each word to facilitate later organization by type.
     *
     * @param languageService    Service to retrieve language information
     * @param reviewedWordService Service to retrieve reviewed words
     * @param songService        Service to retrieve songs
     * @param languageUuid       UUID of the language to process
     * @param outputFilePath     Path where the output file should be written
     * @throws IOException if file writing fails
     * @throws IllegalArgumentException if language is not found
     */
    public static void generateUnreviewedWordsFile(
            LanguageService languageService,
            ReviewedWordService reviewedWordService,
            SongService songService,
            String languageUuid,
            String outputFilePath) throws IOException {
        
        // Get language
        Language language = languageService.findOneByUuid(languageUuid);
        if (language == null) {
            throw new IllegalArgumentException("Language not found with UUID: " + languageUuid);
        }

        // Get all word bunches for the language
        List<NormalizedWordBunch> allBunches = getNormalizedWordBunches(
                songService.findAllByLanguage(language.getUuid()),
                languageService.findAll(),
                language
        );

        // Get reviewed words set
        Set<String> reviewedWords = reviewedWordService.findAllByLanguage(language).stream()
                .map(ReviewedWord::getNormalizedWord)
                .collect(Collectors.toSet());

        // Filter unreviewed word bunches
        List<NormalizedWordBunch> unreviewed = new ArrayList<>();
        for (NormalizedWordBunch nwb : allBunches) {
            Set<String> bunchWords = new HashSet<>();
            for (WordBunch wb : nwb.getWordBunches()) {
                bunchWords.add(wb.getNormalizedWord());
            }
            if (Collections.disjoint(bunchWords, reviewedWords)) {
                unreviewed.add(nwb);
            }
        }

        // Calculate the best word and ratio for each unreviewed bunch
        for (NormalizedWordBunch nwb : unreviewed) {
            nwb.calculateBest();
        }

        // Write to file
        writeUnreviewedWordsToFile(unreviewed, outputFilePath);
    }

    /**
     * Writes unreviewed words to a text file with formatted output.
     *
     * @param unreviewed    List of unreviewed normalized word bunches
     * @param outputFilePath Path where the output file should be written
     * @throws IOException if file writing fails
     */
    private static void writeUnreviewedWordsToFile(List<NormalizedWordBunch> unreviewed, String outputFilePath) throws IOException {
        FileOutputStream fileOutputStream = new FileOutputStream(outputFilePath);

        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(fileOutputStream, StandardCharsets.UTF_8))) {
            for (NormalizedWordBunch nwb : unreviewed) {
                // Best word
                String bestWord = nwb.getBestWord();
                if (bestWord != null) {
                    writer.write("Best Word: " + bestWord);
                    writer.newLine();
                }

                // Variations with counts
                List<WordBunch> wordBunches = nwb.getWordBunches();
                if (!wordBunches.isEmpty()) {
                    writer.write("Variations: ");
                    boolean first = true;
                    for (WordBunch wb : wordBunches) {
                        if (!first) {
                            writer.write(", ");
                        }
                        writer.write(wb.getWord() + " (" + wb.getCount() + ")");
                        first = false;
                    }
                    writer.newLine();
                }

                // Normalized word (from first word bunch)
                if (!wordBunches.isEmpty()) {
                    String normalizedWord = wordBunches.get(0).getNormalizedWord();
                    writer.write("Normalized: " + normalizedWord);
                    writer.newLine();
                }

                // Total count
                int totalCount = wordBunches.stream()
                        .mapToInt(WordBunch::getCount)
                        .sum();
                writer.write("Total Count: " + totalCount);
                writer.newLine();

                // Ratio
                double ratio = nwb.getRatio();
                writer.write("Ratio: " + String.format("%.2f", ratio) + "%");
                writer.newLine();

                // Separator
                writer.write("---");
                writer.newLine();
                writer.newLine();
            }
        }
    }
}
