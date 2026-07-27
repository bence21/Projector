package projector.controller.biblesearch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import projector.utils.AppProperties;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static java.lang.Boolean.parseBoolean;

public class BibleSearchPreferences {

    private static final Logger LOG = LoggerFactory.getLogger(BibleSearchPreferences.class);

    private double splitDividerPosition = 0.72;
    private boolean caseSensitive;
    private boolean wholeWord;
    private Boolean accentsOverride;
    private final Set<Long> includedBibleIds = new LinkedHashSet<>();
    private final Set<Integer> excludedBookIndices = new HashSet<>();
    private Integer chapterFrom;
    private Integer chapterTo;
    private final Map<Integer, Set<Integer>> chaptersByBook = new HashMap<>();
    private boolean biblesPaneExpanded = true;
    private boolean optionsPaneExpanded;
    private boolean rangePaneExpanded;

    public static BibleSearchPreferences load() {
        BibleSearchPreferences preferences = new BibleSearchPreferences();
        Path path = preferences.preferencesPath();
        if (!Files.isRegularFile(path)) {
            return preferences;
        }
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                new FileInputStream(path.toFile()), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank() || !line.contains("=")) {
                    continue;
                }
                int separator = line.indexOf('=');
                String key = line.substring(0, separator).trim();
                String value = line.substring(separator + 1).trim();
                preferences.apply(key, value);
            }
        } catch (IOException e) {
            LOG.warn("Could not load bible search preferences", e);
        }
        return preferences;
    }

    public synchronized void save() {
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(preferencesPath().toFile()), StandardCharsets.UTF_8))) {
            writer.write("splitDivider=" + splitDividerPosition);
            writer.newLine();
            writer.write("caseSensitive=" + caseSensitive);
            writer.newLine();
            writer.write("wholeWord=" + wholeWord);
            writer.newLine();
            writer.write("accentsOverride=" + (accentsOverride == null ? "" : accentsOverride));
            writer.newLine();
            writer.write("includedBibleIds=" + includedBibleIds.stream()
                    .map(String::valueOf)
                    .collect(Collectors.joining(",")));
            writer.newLine();
            writer.write("excludedBookIndices=" + excludedBookIndices.stream()
                    .map(String::valueOf)
                    .collect(Collectors.joining(",")));
            writer.newLine();
            writer.write("chapterFrom=" + (chapterFrom == null ? "" : chapterFrom));
            writer.newLine();
            writer.write("chapterTo=" + (chapterTo == null ? "" : chapterTo));
            writer.newLine();
            writer.write("bookChapters=" + encodeBookChapters());
            writer.newLine();
            writer.write("biblesPaneExpanded=" + biblesPaneExpanded);
            writer.newLine();
            writer.write("optionsPaneExpanded=" + optionsPaneExpanded);
            writer.newLine();
            writer.write("rangePaneExpanded=" + rangePaneExpanded);
            writer.newLine();
        } catch (IOException e) {
            LOG.warn("Could not save bible search preferences", e);
        }
    }

    private void apply(String key, String value) {
        switch (key) {
            case "splitDivider" -> splitDividerPosition = parseDouble(value, splitDividerPosition);
            case "caseSensitive" -> caseSensitive = parseBoolean(value);
            case "wholeWord" -> wholeWord = parseBoolean(value);
            case "accentsOverride" -> accentsOverride = value.isEmpty() ? null : parseBoolean(value);
            case "includedBibleIds" -> parseLongSet(value, includedBibleIds);
            case "excludedBookIndices" -> parseIntSet(value, excludedBookIndices);
            case "chapterFrom" -> chapterFrom = value.isEmpty() ? null : parseInt(value);
            case "chapterTo" -> chapterTo = value.isEmpty() ? null : parseInt(value);
            case "bookChapters" -> decodeBookChapters(value);
            case "biblesPaneExpanded" -> biblesPaneExpanded = parseBoolean(value);
            case "optionsPaneExpanded" -> optionsPaneExpanded = parseBoolean(value);
            case "rangePaneExpanded" -> rangePaneExpanded = parseBoolean(value);
            default -> {
            }
        }
    }

    private String encodeBookChapters() {
        if (chaptersByBook.isEmpty()) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<Integer, Set<Integer>> entry : chaptersByBook.entrySet()) {
            if (entry.getValue() == null || entry.getValue().isEmpty()) {
                continue;
            }
            if (!builder.isEmpty()) {
                builder.append(';');
            }
            builder.append(entry.getKey()).append(':');
            builder.append(entry.getValue().stream().sorted().map(String::valueOf).collect(Collectors.joining(",")));
        }
        return builder.toString();
    }

    private void decodeBookChapters(String value) {
        chaptersByBook.clear();
        if (value == null || value.isBlank()) {
            return;
        }
        for (String bookPart : value.split(";")) {
            if (!bookPart.contains(":")) {
                continue;
            }
            String[] parts = bookPart.split(":", 2);
            int bookIndex = parseInt(parts[0]);
            Set<Integer> chapters = new HashSet<>();
            parseIntSet(parts[1], chapters);
            if (!chapters.isEmpty()) {
                chaptersByBook.put(bookIndex, chapters);
            }
        }
    }

    private static void parseLongSet(String value, Set<Long> target) {
        target.clear();
        if (value == null || value.isBlank()) {
            return;
        }
        for (String part : value.split(",")) {
            if (!part.isBlank()) {
                target.add(Long.parseLong(part.trim()));
            }
        }
    }

    private static void parseIntSet(String value, Set<Integer> target) {
        target.clear();
        if (value == null || value.isBlank()) {
            return;
        }
        for (String part : value.split(",")) {
            if (!part.isBlank()) {
                target.add(Integer.parseInt(part.trim()));
            }
        }
    }

    private static int parseInt(String value) {
        return Integer.parseInt(value.trim());
    }

    private static double parseDouble(String value, double fallback) {
        try {
            return Double.parseDouble(value.trim());
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    private Path preferencesPath() {
        return Path.of(AppProperties.getInstance().getWorkDirectory(), "bible-search.ini");
    }

    public double getSplitDividerPosition() {
        return splitDividerPosition;
    }

    public void setSplitDividerPosition(double splitDividerPosition) {
        this.splitDividerPosition = splitDividerPosition;
    }

    public boolean isCaseSensitive() {
        return caseSensitive;
    }

    public void setCaseSensitive(boolean caseSensitive) {
        this.caseSensitive = caseSensitive;
    }

    public boolean isWholeWord() {
        return wholeWord;
    }

    public void setWholeWord(boolean wholeWord) {
        this.wholeWord = wholeWord;
    }

    public Boolean getAccentsOverride() {
        return accentsOverride;
    }

    public void setAccentsOverride(Boolean accentsOverride) {
        this.accentsOverride = accentsOverride;
    }

    public Set<Long> getIncludedBibleIds() {
        return Collections.unmodifiableSet(includedBibleIds);
    }

    public void setIncludedBibleIds(Set<Long> ids) {
        includedBibleIds.clear();
        if (ids != null) {
            includedBibleIds.addAll(ids);
        }
    }

    public Set<Integer> getExcludedBookIndices() {
        return Collections.unmodifiableSet(excludedBookIndices);
    }

    public void setExcludedBookIndices(Set<Integer> indices) {
        excludedBookIndices.clear();
        if (indices != null) {
            excludedBookIndices.addAll(indices);
        }
    }

    public Integer getChapterFrom() {
        return chapterFrom;
    }

    public void setChapterFrom(Integer chapterFrom) {
        this.chapterFrom = chapterFrom;
    }

    public Integer getChapterTo() {
        return chapterTo;
    }

    public void setChapterTo(Integer chapterTo) {
        this.chapterTo = chapterTo;
    }

    public Map<Integer, Set<Integer>> getChaptersByBook() {
        return chaptersByBook;
    }

    public boolean isBiblesPaneExpanded() {
        return biblesPaneExpanded;
    }

    public void setBiblesPaneExpanded(boolean biblesPaneExpanded) {
        this.biblesPaneExpanded = biblesPaneExpanded;
    }

    public boolean isOptionsPaneExpanded() {
        return optionsPaneExpanded;
    }

    public void setOptionsPaneExpanded(boolean optionsPaneExpanded) {
        this.optionsPaneExpanded = optionsPaneExpanded;
    }

    public boolean isRangePaneExpanded() {
        return rangePaneExpanded;
    }

    public void setRangePaneExpanded(boolean rangePaneExpanded) {
        this.rangePaneExpanded = rangePaneExpanded;
    }

    public void clearRangeFilters() {
        excludedBookIndices.clear();
        chapterFrom = null;
        chapterTo = null;
        chaptersByBook.clear();
    }

    public void resetToDefaults() {
        caseSensitive = false;
        wholeWord = false;
        accentsOverride = null;
        clearRangeFilters();
        optionsPaneExpanded = false;
        rangePaneExpanded = false;
    }
}
