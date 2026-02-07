package com.bence.projector.server.utils;

import com.bence.projector.server.utils.models.NormalizedWordBunch;
import com.bence.projector.server.utils.models.WordBunch;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.bence.projector.server.utils.StringUtils.normalizeAccents;

/**
 * Encapsulates a map from normalized word to {@link NormalizedWordBunch},
 * with methods to populate, retrieve, and query word counts/suggestions.
 */
public class NormalizedWordBunchMap {

    private final Map<String, NormalizedWordBunch> map;

    public NormalizedWordBunchMap() {
        this.map = new HashMap<>();
    }

    /**
     * Retrieves the NormalizedWordBunch for the given word.
     * The word will be normalized before lookup to ensure consistent matching.
     *
     * @param word word to look up (will be normalized automatically)
     * @return the bunch, or null if not found
     */
    public NormalizedWordBunch get(String word) {
        if (word == null) {
            return null;
        }
        String normalizedWord = normalizeAccents(word);
        return map.get(normalizedWord);
    }

    /**
     * Populates this map from a list of normalized word bunches.
     * Clears existing entries and rebuilds from the provided word bunches.
     * This method is useful when you already have computed word bunches (e.g., from cache).
     *
     * @param normalizedWordBunches the list of normalized word bunches to populate from
     */
    public void populateFromWordBunches(List<NormalizedWordBunch> normalizedWordBunches) {
        map.clear();
        if (normalizedWordBunches == null) {
            return;
        }
        for (NormalizedWordBunch nwb : normalizedWordBunches) {
            for (WordBunch wb : nwb.getWordBunches()) {
                String normalizedWord = wb.getNormalizedWord();
                if (normalizedWord != null) {
                    map.putIfAbsent(normalizedWord, nwb);
                }
            }
        }
    }

    /**
     * Returns the best word suggestion for the given normalized word, or null if none.
     */
    public String getBestWord(String word) {
        NormalizedWordBunch nwb = get(word);
        if (nwb != null && nwb.getBestWord() != null) {
            return nwb.getBestWord();
        }
        return null;
    }

    public boolean contains(String word) {
        return get(word) != null;
    }

    public boolean isEmpty() {
        return map.isEmpty();
    }

    public int size() {
        return map.size();
    }
}
