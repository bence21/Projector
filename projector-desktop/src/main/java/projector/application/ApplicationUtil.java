package projector.application;


import com.google.gson.Gson;
import javafx.application.Platform;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import projector.controller.MyController;
import projector.controller.ProjectionScreenController;
import projector.controller.song.SongController;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static projector.controller.util.FileUtil.getGson;
import static projector.controller.util.FileUtil.getLinesFromFile;

public class ApplicationUtil {

    private static final Logger LOG = LoggerFactory.getLogger(ApplicationUtil.class);

    private static ApplicationUtil instance;
    private final List<Stage> stages = new ArrayList<>();
    private final String PROJECTOR_STATE_FILE = "projectorState.json";
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

    public void saveProjectorState() {
        ProjectorState projectorState = new ProjectorState();
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
            FileOutputStream ofStream = new FileOutputStream(PROJECTOR_STATE_FILE);
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(ofStream, StandardCharsets.UTF_8));
            Gson gson = getGson();
            String json = gson.toJson(projectorState, ProjectorState.class);
            bw.write(json);
            bw.close();
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
        }
    }

    public void checkForProjectorState() {
        try {
            File file = new File(PROJECTOR_STATE_FILE);
            if (!file.exists()) {
                return;
            }
            String s = getLinesFromFile(PROJECTOR_STATE_FILE);
            if (s == null) {
                return;
            }
            Gson gson = getGson();
            ProjectorState projectorState = gson.fromJson(s, ProjectorState.class);
            if (projectorState == null) {
                return;
            }
            if (projectorState.isLoaded()) {
                return;
            }
            MyController myController = MyController.getInstance();
            if (myController != null) {
                myController.setByProjectorState(projectorState);
                ProjectionScreenController projectionScreenController = myController.getProjectionScreenController();
                if (projectionScreenController != null) {
                    projectionScreenController.setByProjectorState(projectorState);
                }
                SongController songController = myController.getSongController();
                if (songController != null) {
                    songController.setByProjectorState(projectorState);
                }
            }
            projectorState.setLoaded(true);
            saveProjectorStateToFile(projectorState);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    public interface Listener {
        void onApplicationClose();
    }
}
