package projector.utils;

import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public final class CompareWindowTracker {

    private static final Set<Stage> openStages = new CopyOnWriteArraySet<>();

    private CompareWindowTracker() {
    }

    public static void register(Stage stage) {
        if (stage == null) {
            return;
        }
        openStages.add(stage);
        stage.setOnHidden(event -> openStages.remove(stage));
    }

    public static void closeAll() {
        List<Stage> stages = new ArrayList<>(openStages);
        for (Stage stage : stages) {
            if (stage.isShowing()) {
                SceneUtils.closeStage(stage);
            }
            openStages.remove(stage);
        }
    }
}
