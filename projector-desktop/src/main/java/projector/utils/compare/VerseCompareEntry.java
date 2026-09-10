package projector.utils.compare;

public class VerseCompareEntry {

    private final int verseIndex;
    private final VerseDiffKind diffKind;

    public VerseCompareEntry(int verseIndex, VerseDiffKind diffKind) {
        this.verseIndex = verseIndex;
        this.diffKind = diffKind;
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
