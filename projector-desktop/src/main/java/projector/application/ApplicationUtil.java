package projector.application;

import com.google.gson.Gson;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import projector.MainDesktop;
import projector.controller.BibleController;
import projector.controller.GalleryController;
import projector.controller.MyController;
import projector.controller.ProjectionScreenController;
import projector.controller.song.SongController;
import projector.utils.AlertUtil;
import projector.utils.AppProperties;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import static projector.controller.util.FileUtil.getGson;
import static projector.controller.util.FileUtil.getLinesFromFile;

public class ApplicationUtil {

    private static final Logger LOG = LoggerFactory.getLogger(ApplicationUtil.class);

    private static ApplicationUtil instance;
    private final List<Stage> stages = new ArrayList<>();
    private static final String PROJECTOR_STATE_FILE = "projectorState.json";
    private Listener listener;
    private Stage primaryStage;

    private ApplicationUtil() {
    }

    public static ApplicationUtil getInstance() {
        if (instance == null) {
            instance = new ApplicationUtil();
        }
        return instance;
    }

    public static String getProjectorStateFilePath() {
        return AppProperties.getInstance().getWorkDirectory() + PROJECTOR_STATE_FILE;
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public void closeApplication() {
        Platform.runLater(() -> {
            if (listener != null) {
                listener.onApplicationClose();
            }
            closeStages();
        });
    }

    private void closeStages() {
        primaryStage.close();
        for (Stage stage : stages) {
            stage.close();
        }
    }

    public void addCloseNeededStage(Stage stage) {
        stages.add(stage);
        stage.setOnCloseRequest(event -> stages.remove(stage));
    }

    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }

    public void saveProjectorState() {
        ProjectorState projectorState = new ProjectorState();
        projectorState.setLoaded(false);
        MyController myController = MyController.getInstance();
        if (myController != null) {
            myController.updateProjectorState(projectorState);
            ProjectionScreenController projectionScreenController = myController.getProjectionScreenController();
            if (projectionScreenController != null) {
                projectionScreenController.updateProjectorState(projectorState);
            }
            SongController songController = myController.getSongController();
            if (songController != null) {
                songController.updateProjectorState(projectorState);
            }
        }
        saveProjectorStateToFile(projectorState);
    }

    private void saveProjectorStateToFile(ProjectorState projectorState) {
        try {
            FileOutputStream ofStream = new FileOutputStream(getProjectorStateFilePath());
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(ofStream, StandardCharsets.UTF_8));
            Gson gson = getGson();
            String json = gson.toJson(projectorState, ProjectorState.class);
            bw.write(json);
            bw.close();
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
        }
    }

    /**
     * Called from {@link MainDesktop#start2} after the default UI is loaded.
     * If the last exit was unclean, asks whether to restore the saved session.
     */
    public void checkForProjectorState() {
        if (!SessionAutosave.getInstance().isRestorePending()) {
            return;
        }
        try {
            ProjectorState projectorState = loadPendingProjectorState();
            if (projectorState == null) {
                return;
            }
            if (!confirmRestoreSession()) {
                markSessionDeclined(projectorState);
                return;
            }
            applyProjectorState(projectorState);
            projectorState.setLoaded(true);
            saveProjectorStateToFile(projectorState);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    private ProjectorState loadPendingProjectorState() {
        File file = new File(getProjectorStateFilePath());
        if (!file.exists()) {
            return null;
        }
        String s = getLinesFromFile(getProjectorStateFilePath());
        if (s == null) {
            return null;
        }
        Gson gson = getGson();
        ProjectorState projectorState = gson.fromJson(s, ProjectorState.class);
        if (projectorState == null || projectorState.isLoaded()) {
            return null;
        }
        return projectorState;
    }

    private boolean confirmRestoreSession() {
        ResourceBundle resourceBundle = Settings.getInstance().getResourceBundle();
        Alert alert = AlertUtil.getAppAlert(Alert.AlertType.CONFIRMATION, MainDesktop.class);
        alert.setTitle(resourceBundle.getString("Restore previous session?"));
        alert.setHeaderText(null);
        alert.setContentText(resourceBundle.getString(
                "The application did not shut down cleanly. Restore your previous session?"));
        alert.getButtonTypes().setAll(
                new ButtonType(resourceBundle.getString("Yes")),
                new ButtonType(resourceBundle.getString("No")));
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent()
                && result.get().getText().equals(resourceBundle.getString("Yes"));
    }

    private void applyProjectorState(ProjectorState projectorState) {
        MyController myController = MyController.getInstance();
        if (myController == null) {
            return;
        }
        SessionAutosave.getInstance().runWhileRestoring(() -> {
            SongController songController = myController.getSongController();
            if (songController != null) {
                songController.setByProjectorState(projectorState);
            }
            BibleController bibleController = myController.getBibleController();
            if (bibleController != null) {
                bibleController.setByProjectorState(projectorState);
            }
            myController.restoreGalleryFromProjectorState(projectorState);
            myController.restoreScheduleFromProjectorState(projectorState);
            myController.setByProjectorState(projectorState);
        });
        reapplySavedProjection(projectorState);
    }

    /**
     * Reapplies the saved projection snapshot (type, text, data, or image path) after UI restore.
     * UI controllers restore selection only; this is the single authoritative projection update.
     */
    public void reapplySavedProjection(ProjectorState projectorState) {
        MyController myController = MyController.getInstance();
        if (myController == null || projectorState == null) {
            return;
        }
        ProjectionScreenController projectionScreenController = myController.getProjectionScreenController();
        if (projectionScreenController == null) {
            return;
        }
        Runnable apply = () -> {
            if (GalleryController.shouldProjectSelectedImageOnRestore(
                    projectorState, projectorState.getGallerySelectedImagePath())) {
                return;
            }
            ProjectionType savedType = projectorState.getProjectionType();
            if (savedType == ProjectionType.SONG) {
                SongController songController = myController.getSongController();
                if (songController != null && songController.reprojectSelectedVerseAfterRestore()) {
                    projectionScreenController.repaint();
                    return;
                }
            }
            if (savedType == ProjectionType.BIBLE || savedType == ProjectionType.REFERENCE) {
                BibleController bibleController = myController.getBibleController();
                if (bibleController != null && bibleController.reprojectSelectedVersesAfterRestore(savedType)) {
                    projectionScreenController.repaint();
                    return;
                }
            }
            projectionScreenController.setByProjectorState(projectorState);
            projectionScreenController.repaint();
        };
        Platform.runLater(() -> Platform.runLater(apply));
    }

    private void markSessionDeclined(ProjectorState projectorState) {
        projectorState.setLoaded(true);
        saveProjectorStateToFile(projectorState);
    }

    public void consumeSessionOnCleanClose() {
        try {
            SessionAutosave.getInstance().saveNow();
            File file = new File(getProjectorStateFilePath());
            if (!file.exists()) {
                return;
            }
            String s = getLinesFromFile(getProjectorStateFilePath());
            if (s == null) {
                return;
            }
            Gson gson = getGson();
            ProjectorState projectorState = gson.fromJson(s, ProjectorState.class);
            if (projectorState != null) {
                projectorState.setLoaded(true);
                saveProjectorStateToFile(projectorState);
            }
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    public interface Listener {
        void onApplicationClose();
    }
}
