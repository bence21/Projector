package projector.utils;

import projector.model.Language;
import projector.model.Song;
import projector.service.SongService;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class ForkMirrorMigrationState {

    private static final String MIGRATION_FILE_NAME = "fork-mirror-migration.properties";

    private ForkMirrorMigrationState() {
    }

    public static boolean isMigrated(String languageUuid) {
        if (languageUuid == null || languageUuid.trim().isEmpty()) {
            return false;
        }
        return loadMigratedUuids().contains(languageUuid);
    }

    public static void markMigrated(String languageUuid) {
        if (languageUuid == null || languageUuid.trim().isEmpty()) {
            return;
        }
        Set<String> migrated = loadMigratedUuids();
        if (migrated.add(languageUuid)) {
            saveMigratedUuids(migrated);
        }
    }

    public static boolean needsMigration(Language language, SongService songService) {
        if (language == null || songService == null) {
            return false;
        }
        String languageUuid = language.getUuid();
        if (languageUuid == null || isMigrated(languageUuid)) {
            return false;
        }
        List<Song> songs = language.getSongs();
        for (Song song : songs) {
            if (songService.isLegacyMigrationCandidate(song)) {
                return true;
            }
        }
        return false;
    }

    private static File getMigrationFile() {
        return new File(AppProperties.getInstance().getDatabaseFolder(), MIGRATION_FILE_NAME);
    }

    private static Set<String> loadMigratedUuids() {
        Set<String> migrated = new HashSet<>();
        File file = getMigrationFile();
        if (!file.exists()) {
            return migrated;
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
                if (Boolean.parseBoolean(value)) {
                    migrated.add(key);
                }
            }
        } catch (Exception ignored) {
            return new HashSet<>();
        }
        return migrated;
    }

    private static void saveMigratedUuids(Set<String> migratedUuids) {
        File file = getMigrationFile();
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
            for (String uuid : migratedUuids) {
                writer.write(uuid);
                writer.write('=');
                writer.write("true");
                writer.newLine();
            }
        } catch (Exception ignored) {
        }
    }
}
