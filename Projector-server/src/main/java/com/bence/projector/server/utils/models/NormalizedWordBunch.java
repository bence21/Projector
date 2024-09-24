package com.bence.projector.server.utils.models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NormalizedWordBunch {
    private List<WordBunch> wordBunches;
    private double ratio = 1.0;
    private String ratioS;
    private String bestWord;
    private WordBunch maxBunch;

    public NormalizedWordBunch() {
    }

    public void add(WordBunch wordBunch) {
        getWordBunches().add(wordBunch);
    }

    public List<WordBunch> getWordBunches() {
        if (wordBunches == null) {
            wordBunches = new ArrayList<>();
        }
        return wordBunches;
    }

    private WordBunch getMaxWordBunch() {
        int maxCount = 0;
        WordBunch maxBunch = null;
        Map<String, WordBunch> normalizedWordBunchMap = new HashMap<>();
        for (WordBunch wordBunch : wordBunches) {
            String key = wordBunch.getWord().toLowerCase();
            WordBunch normalizedWordBunch = normalizedWordBunchMap.computeIfAbsent(key, k -> {
                WordBunch wordBunchMap = new WordBunch();
                wordBunchMap.setWord(wordBunch.getWord());
                return wordBunchMap;
            });
            normalizedWordBunch.setCount(normalizedWordBunch.getCount() + wordBunch.getCount());
        }
        for (WordBunch wordBunch : normalizedWordBunchMap.values()) {
            int count = wordBunch.getCount();
            if (count > maxCount) {
                maxCount = count;
                maxBunch = wordBunch;
            }
        }
        return maxBunch;
    }

    public void calculateBest() {
        maxBunch = getMaxWordBunch();
        if (maxBunch != null) {
            int sum = 0;
            int totalSum = 0;
            this.bestWord = maxBunch.getWord();
            String bestWordLowerCase = bestWord.toLowerCase();
            for (WordBunch wordBunch : wordBunches) {
                int count = wordBunch.getCount();
                wordBunch.setProblematic(!bestWordLowerCase.equals(wordBunch.getWord().toLowerCase()));
                if (!wordBunch.isProblematic()) {
                    sum += count;
                }
                totalSum += count;
            }
            ratio = sum * 100.0;
            ratio /= totalSum;
            ratioS = (int) ratio + "";
        }
    }

    public WordBunch getMaxBunch() {
        return maxBunch;
    }

    public String getBestWord() {
        return bestWord;
    }

    public double getRatio() {
        return ratio;
    }

    public double getRatioForCompare() {
        if (ratio >= 100) {
            return 0;
        }
        return ratio;
    }

    public String getRatioS() {
        return ratioS;
    }
}
