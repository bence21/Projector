package projector.controller.biblesearch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class BibleSearchFilterSnapshot {

    private final Set<Integer> excludedBookIndices;
    private final Integer chapterFrom;
    private final Integer chapterTo;
    private final Map<Integer, Set<Integer>> chaptersByBook;

    private BibleSearchFilterSnapshot(Set<Integer> excludedBookIndices, Integer chapterFrom, Integer chapterTo,
                                      Map<Integer, Set<Integer>> chaptersByBook) {
        this.excludedBookIndices = excludedBookIndices;
        this.chapterFrom = chapterFrom;
        this.chapterTo = chapterTo;
        this.chaptersByBook = chaptersByBook;
    }

    public static BibleSearchFilterSnapshot from(BibleSearchPreferences preferences) {
        Map<Integer, Set<Integer>> chaptersCopy = new HashMap<>();
        for (Map.Entry<Integer, Set<Integer>> entry : preferences.getChaptersByBook().entrySet()) {
            if (entry.getValue() != null && !entry.getValue().isEmpty()) {
                chaptersCopy.put(entry.getKey(), new HashSet<>(entry.getValue()));
            }
        }
        return new BibleSearchFilterSnapshot(
                new HashSet<>(preferences.getExcludedBookIndices()),
                preferences.getChapterFrom(),
                preferences.getChapterTo(),
                chaptersCopy
        );
    }

    public boolean isBookIncluded(int bookIndex) {
        return !excludedBookIndices.contains(bookIndex);
    }

    public boolean isChapterIncluded(int bookIndex, int chapterNumber) {
        boolean hasRange = chapterFrom != null || chapterTo != null;
        Set<Integer> checkedChapters = chaptersByBook.get(bookIndex);
        boolean hasChecked = checkedChapters != null && !checkedChapters.isEmpty();
        if (!hasRange && !hasChecked) {
            return true;
        }
        if (hasRange && isChapterInRange(chapterNumber)) {
            return true;
        }
        return hasChecked && checkedChapters.contains(chapterNumber);
    }

    private boolean isChapterInRange(int chapterNumber) {
        int min = chapterFrom != null ? chapterFrom : 1;
        int max = chapterTo != null ? chapterTo : Integer.MAX_VALUE;
        return chapterNumber >= min && chapterNumber <= max;
    }
}
