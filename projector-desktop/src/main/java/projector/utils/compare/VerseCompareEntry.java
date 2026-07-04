package projector.utils.compare;

public class VerseCompareEntry {

    private final int verseIndex;
    private final String leftText;
    private final String rightText;
    private final String leftLabel;
    private final String rightLabel;
    private final VerseDiffKind diffKind;
    private final int matchedLeftIndex;
    private final int matchedRightIndex;

    public VerseCompareEntry(int verseIndex, String leftText, String rightText,
                             String leftLabel, String rightLabel, VerseDiffKind diffKind,
                             int matchedLeftIndex, int matchedRightIndex) {
        this.verseIndex = verseIndex;
        this.leftText = leftText == null ? "" : leftText;
        this.rightText = rightText == null ? "" : rightText;
        this.leftLabel = leftLabel;
        this.rightLabel = rightLabel;
        this.diffKind = diffKind;
        this.matchedLeftIndex = matchedLeftIndex;
        this.matchedRightIndex = matchedRightIndex;
    }

    public int getVerseIndex() {
        return verseIndex;
    }

    public VerseDiffKind getDiffKind() {
        return diffKind;
    }

    public boolean isOrderOnly() {
        return diffKind == VerseDiffKind.ORDER_ONLY;
    }
}
