package projector.controller.song.util;

public class ContainsResult {
    private Integer count;
    private Integer wordCount;

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public Integer getWordCount() {
        return wordCount;
    }

    public void setWordCount(Integer wordCount) {
        this.wordCount = wordCount;
    }

    public double getRatio() {
        if (wordCount == null || wordCount == 0) {
            return 0;
        }
        return count.doubleValue() / wordCount;
    }
}
