package com.bence.projector.common.dto;

import java.util.List;

public class NormalizedWordBunchDTO {

    private String bestWord;
    private double ratio;
    private List<WordBunchDTO> wordBunches;
    private WordBunchDTO maxBunch;

    public void setBestWord(String bestWord) {
        this.bestWord = bestWord;
    }

    public String getBestWord() {
        return bestWord;
    }

    public void setRatio(double ratio) {
        this.ratio = ratio;
    }

    public double getRatio() {
        return ratio;
    }

    public void setWordBunches(List<WordBunchDTO> wordBunches) {
        this.wordBunches = wordBunches;
    }

    public List<WordBunchDTO> getWordBunches() {
        return wordBunches;
    }

    public void setMaxBunch(WordBunchDTO maxBunch) {
        this.maxBunch = maxBunch;
    }

    public WordBunchDTO getMaxBunch() {
        return maxBunch;
    }
}
