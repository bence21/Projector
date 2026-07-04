package projector.utils;

import projector.model.Language;
import projector.model.Song;
import projector.service.SongService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class DownloadWorkEstimator {

    private DownloadWorkEstimator() {
    }

    public static DownloadWorkPlan estimate(List<Language> languages, SongService songService) {
        int totalWork = 0;
        Map<String, Integer> estimatedDownloadSteps = new HashMap<>();
        if (languages == null || songService == null) {
            return new DownloadWorkPlan(0, estimatedDownloadSteps);
        }
        for (Language language : languages) {
            if (language == null || !language.isSelected()) {
                continue;
            }
            String languageUuid = language.getUuid();
            if (ForkMirrorMigrationState.needsMigration(language, songService)) {
                totalWork += countLegacyMigrationCandidates(language, songService);
            }
            int downloadSteps = estimateDownloadSteps(language, songService);
            if (languageUuid != null) {
                estimatedDownloadSteps.put(languageUuid, downloadSteps);
            }
            totalWork += downloadSteps;
        }
        return new DownloadWorkPlan(totalWork, estimatedDownloadSteps);
    }

    public static int countLegacyMigrationCandidates(Language language, SongService songService) {
        int count = 0;
        for (Song song : language.getSongs()) {
            if (songService.isLegacyMigrationCandidate(song)) {
                ++count;
            }
        }
        return count;
    }

    public static int estimateDownloadSteps(Language language, SongService songService) {
        long serverSize = language.getCountedSongsSize();
        if (!language.isSectionTypeDownloadedCorrectly()) {
            return toInt(serverSize);
        }
        int localCount = countLocalSyncedSongs(language);
        return toInt(Math.max(0, serverSize - localCount));
    }

    private static int countLocalSyncedSongs(Language language) {
        int count = 0;
        for (Song song : language.getSongs()) {
            if (song.isFork()) {
                continue;
            }
            if (song.getUuid() != null && !song.getUuid().trim().isEmpty()) {
                ++count;
            }
        }
        return count;
    }

    private static int toInt(long value) {
        if (value <= 0) {
            return 0;
        }
        return (int) Math.min(value, Integer.MAX_VALUE);
    }
}
