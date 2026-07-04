package projector.utils.compare;

public class CompareNormalizeOptions {

    private final boolean ignoreCase;
    private final boolean ignoreAccents;
    private final boolean ignorePunctuation;
    private final boolean ignoreSlashPipeBackslash;
    private final boolean ignoreAnnotations;
    private final boolean ignoreNumbers;
    private final boolean normalizeWhitespace;
    private final boolean normalizeQuotes;

    public CompareNormalizeOptions(boolean ignoreCase, boolean ignoreAccents, boolean ignorePunctuation,
                                   boolean ignoreSlashPipeBackslash, boolean ignoreAnnotations, boolean ignoreNumbers,
                                   boolean normalizeWhitespace, boolean normalizeQuotes) {
        this.ignoreCase = ignoreCase;
        this.ignoreAccents = ignoreAccents;
        this.ignorePunctuation = ignorePunctuation;
        this.ignoreSlashPipeBackslash = ignoreSlashPipeBackslash;
        this.ignoreAnnotations = ignoreAnnotations;
        this.ignoreNumbers = ignoreNumbers;
        this.normalizeWhitespace = normalizeWhitespace;
        this.normalizeQuotes = normalizeQuotes;
    }

    public boolean isIgnoreCase() {
        return ignoreCase;
    }

    public boolean isIgnoreAccents() {
        return ignoreAccents;
    }

    public boolean isIgnorePunctuation() {
        return ignorePunctuation;
    }

    public boolean isIgnoreSlashPipeBackslash() {
        return ignoreSlashPipeBackslash;
    }

    public boolean isIgnoreAnnotations() {
        return ignoreAnnotations;
    }

    public boolean isIgnoreNumbers() {
        return ignoreNumbers;
    }

    public boolean isNormalizeWhitespace() {
        return normalizeWhitespace;
    }

    public boolean isNormalizeQuotes() {
        return normalizeQuotes;
    }
}
