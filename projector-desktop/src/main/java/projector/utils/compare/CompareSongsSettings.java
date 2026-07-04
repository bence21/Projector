package projector.utils.compare;

import projector.utils.AppProperties;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

public class CompareSongsSettings {

    private boolean strictDiff = true;
    private boolean repeatChorus = true;
    private boolean ignoreCase = true;
    private boolean ignoreAccents = true;
    private boolean ignorePunctuation = false;
    private boolean ignoreSlashPipeBackslash = false;
    private boolean ignoreAnnotations = false;
    private boolean ignoreNumbers = false;
    private boolean normalizeWhitespace = true;
    private boolean normalizeQuotes = true;

    private enum SettingKey {
        STRICT_DIFF("strictDiff"),
        REPEAT_CHORUS("repeatChorus"),
        IGNORE_CASE("ignoreCase"),
        IGNORE_ACCENTS("ignoreAccents"),
        IGNORE_PUNCTUATION("ignorePunctuation"),
        IGNORE_SLASH_PIPE_BACKSLASH("ignoreSlashPipeBackslash"),
        IGNORE_ANNOTATIONS("ignoreAnnotations"),
        IGNORE_NUMBERS("ignoreNumbers"),
        NORMALIZE_WHITESPACE("normalizeWhitespace"),
        NORMALIZE_QUOTES("normalizeQuotes");

        private final String key;

        SettingKey(String key) {
            this.key = key;
        }

        static SettingKey fromKey(String key) {
            for (SettingKey settingKey : values()) {
                if (settingKey.key.equals(key)) {
                    return settingKey;
                }
            }
            return null;
        }

        boolean get(CompareSongsSettings settings) {
            return switch (this) {
                case STRICT_DIFF -> settings.strictDiff;
                case REPEAT_CHORUS -> settings.repeatChorus;
                case IGNORE_CASE -> settings.ignoreCase;
                case IGNORE_ACCENTS -> settings.ignoreAccents;
                case IGNORE_PUNCTUATION -> settings.ignorePunctuation;
                case IGNORE_SLASH_PIPE_BACKSLASH -> settings.ignoreSlashPipeBackslash;
                case IGNORE_ANNOTATIONS -> settings.ignoreAnnotations;
                case IGNORE_NUMBERS -> settings.ignoreNumbers;
                case NORMALIZE_WHITESPACE -> settings.normalizeWhitespace;
                case NORMALIZE_QUOTES -> settings.normalizeQuotes;
            };
        }

        void set(CompareSongsSettings settings, boolean value) {
            switch (this) {
                case STRICT_DIFF -> settings.strictDiff = value;
                case REPEAT_CHORUS -> settings.repeatChorus = value;
                case IGNORE_CASE -> settings.ignoreCase = value;
                case IGNORE_ACCENTS -> settings.ignoreAccents = value;
                case IGNORE_PUNCTUATION -> settings.ignorePunctuation = value;
                case IGNORE_SLASH_PIPE_BACKSLASH -> settings.ignoreSlashPipeBackslash = value;
                case IGNORE_ANNOTATIONS -> settings.ignoreAnnotations = value;
                case IGNORE_NUMBERS -> settings.ignoreNumbers = value;
                case NORMALIZE_WHITESPACE -> settings.normalizeWhitespace = value;
                case NORMALIZE_QUOTES -> settings.normalizeQuotes = value;
            }
        }
    }

    public static CompareSongsSettings load() {
        CompareSongsSettings settings = new CompareSongsSettings();
        File file = getSettingsFile();
        if (!file.exists()) {
            return settings;
        }
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                int idx = line.indexOf('=');
                if (idx <= 0) {
                    continue;
                }
                String key = line.substring(0, idx).trim();
                String value = line.substring(idx + 1).trim();
                apply(settings, key, Boolean.parseBoolean(value));
            }
        } catch (Exception ignored) {
            return new CompareSongsSettings();
        }
        return settings;
    }

    public void save() {
        File file = getSettingsFile();
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
            for (SettingKey settingKey : SettingKey.values()) {
                write(writer, settingKey.key, settingKey.get(this));
            }
        } catch (Exception ignored) {
        }
    }

    private static void write(BufferedWriter writer, String key, boolean value) throws java.io.IOException {
        writer.write(key);
        writer.write('=');
        writer.write(Boolean.toString(value));
        writer.newLine();
    }

    private static void apply(CompareSongsSettings settings, String key, boolean value) {
        SettingKey settingKey = SettingKey.fromKey(key);
        if (settingKey != null) {
            settingKey.set(settings, value);
        }
    }

    private static File getSettingsFile() {
        return new File(AppProperties.getInstance().getDatabaseFolder(), "compare-songs-settings.properties");
    }

    public CompareNormalizeOptions getEffectiveNormalizeOptions() {
        if (strictDiff) {
            return new CompareNormalizeOptions(false, false, false, false, false, false, false, false);
        }
        return new CompareNormalizeOptions(ignoreCase, ignoreAccents, ignorePunctuation, ignoreSlashPipeBackslash,
                ignoreAnnotations, ignoreNumbers, normalizeWhitespace, normalizeQuotes);
    }

    public boolean isStrictDiff() {
        return strictDiff;
    }

    public void setStrictDiff(boolean strictDiff) {
        this.strictDiff = strictDiff;
    }

    public boolean isRepeatChorus() {
        return repeatChorus;
    }

    public void setRepeatChorus(boolean repeatChorus) {
        this.repeatChorus = repeatChorus;
    }

    public boolean isIgnoreCase() {
        return ignoreCase;
    }

    public void setIgnoreCase(boolean ignoreCase) {
        this.ignoreCase = ignoreCase;
    }

    public boolean isIgnoreAccents() {
        return ignoreAccents;
    }

    public void setIgnoreAccents(boolean ignoreAccents) {
        this.ignoreAccents = ignoreAccents;
    }

    public boolean isIgnorePunctuation() {
        return ignorePunctuation;
    }

    public void setIgnorePunctuation(boolean ignorePunctuation) {
        this.ignorePunctuation = ignorePunctuation;
    }

    public boolean isIgnoreSlashPipeBackslash() {
        return ignoreSlashPipeBackslash;
    }

    public void setIgnoreSlashPipeBackslash(boolean ignoreSlashPipeBackslash) {
        this.ignoreSlashPipeBackslash = ignoreSlashPipeBackslash;
    }

    public boolean isIgnoreAnnotations() {
        return ignoreAnnotations;
    }

    public void setIgnoreAnnotations(boolean ignoreAnnotations) {
        this.ignoreAnnotations = ignoreAnnotations;
    }

    public boolean isIgnoreNumbers() {
        return ignoreNumbers;
    }

    public void setIgnoreNumbers(boolean ignoreNumbers) {
        this.ignoreNumbers = ignoreNumbers;
    }

    public boolean isNormalizeWhitespace() {
        return normalizeWhitespace;
    }

    public void setNormalizeWhitespace(boolean normalizeWhitespace) {
        this.normalizeWhitespace = normalizeWhitespace;
    }

    public boolean isNormalizeQuotes() {
        return normalizeQuotes;
    }

    public void setNormalizeQuotes(boolean normalizeQuotes) {
        this.normalizeQuotes = normalizeQuotes;
    }
}
