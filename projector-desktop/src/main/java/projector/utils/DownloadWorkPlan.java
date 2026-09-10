package projector.utils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class DownloadWorkPlan {

    private final int totalWork;
    private final Map<String, Integer> estimatedDownloadStepsByLanguageUuid;

    public DownloadWorkPlan(int totalWork, Map<String, Integer> estimatedDownloadStepsByLanguageUuid) {
        this.totalWork = totalWork;
        this.estimatedDownloadStepsByLanguageUuid = estimatedDownloadStepsByLanguageUuid == null
                ? Collections.emptyMap()
                : Collections.unmodifiableMap(new HashMap<>(estimatedDownloadStepsByLanguageUuid));
    }

    public int getTotalWork() {
        return totalWork;
    }

    public int getEstimatedDownloadSteps(String languageUuid) {
        if (languageUuid == null) {
            return 0;
        }
        return estimatedDownloadStepsByLanguageUuid.getOrDefault(languageUuid, 0);
    }
}
