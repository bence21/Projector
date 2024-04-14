package projector.controller;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.stage.DirectoryChooser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import projector.config.Log4j2Config;
import projector.repository.ormLite.DatabaseHelper;
import projector.utils.AppProperties;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static projector.application.Settings.getSettingFilePath;
import static projector.application.SongVerseTimeService.getSongVersTimesFilePath;
import static projector.controller.RecentController.getRecentFilePath;
import static projector.utils.AlertUtil.getAppAlert;

public class FirstSetupController {

    private static final Logger LOG = LoggerFactory.getLogger(FirstSetupController.class);
    public Button copyDataButton;
    public Button startAsNewButton;
    private Listener listener;

    private static String replaceDirectorySeparator(String s) {
        return s.replace("/", "\\");
    }

    private static void logErrorStream(int result, Process process) throws IOException {
        if (result != 0) {
            // Handle the error appropriately
            System.out.println("The copy command failed. Checking for errors...");
            InputStream errorStream = process.getErrorStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(errorStream));
            String line;
            while ((line = reader.readLine()) != null) {
                LOG.info(line); // Print any error messages from the command
            }
        }
    }

    public void onStartAsNew() {
        disableButtons();
        if (listener != null) {
            listener.onStartAsNew();
        }
    }

    private void disableButtons_(boolean disabled) {
        startAsNewButton.setDisable(disabled);
        copyDataButton.setDisable(disabled);
    }

    private void disableButtons() {
        disableButtons_(true);
    }

    private void enableButtons() {
        disableButtons_(false);
    }

    public void onCopyDataFromPreviousVersion() {
        try {
            disableButtons();
            DirectoryChooser directoryChooser = new DirectoryChooser();
            directoryChooser.setTitle("Select previously installed directory!");
            File file = directoryChooser.showDialog(null);
            if (file == null) {
                return;
            }
            System.out.println(file.getAbsolutePath());
            List<WantedFile> wantedFiles = new ArrayList<>();
            String databaseFolder = AppProperties.getInstance().getDatabaseFolder();
            ensureDirectory(databaseFolder);
            addWantedFile(wantedFiles, databaseFolder + "/projector.mv.db");
            addWantedFile(wantedFiles, databaseFolder + "/projector.trace.db");
            addWantedFile(wantedFiles, DatabaseHelper.getDataBaseVersionPath());
            addWantedFile(wantedFiles, getSettingFilePath());
            addWantedFile(wantedFiles, getRecentFilePath());
            addWantedFile(wantedFiles, Log4j2Config.getInstance().getLogFilePath());
            addWantedFile(wantedFiles, getSongVersTimesFilePath());
            tryToFindWantedFiles(wantedFiles, file.getAbsolutePath());
        } finally {
            enableButtons();
        }
    }

    private void ensureDirectory(String folder) {
        try {
            Path path = Paths.get(folder);
            if (!Files.exists(path)) {
                Files.createDirectories(path);
            }
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    private void tryToFindWantedFiles(List<WantedFile> wantedFiles, String directory) {
        for (WantedFile wantedFile : wantedFiles) {
            File file = new File(directory + "/" + wantedFile.getFilePath());
            wantedFile.setFound(file.exists());
        }
        if (allFilesFound(wantedFiles)) {
            copyFoundFiles(wantedFiles, directory);
        } else if (someFilesFound(wantedFiles)) {
            String notFoundFiles = gatherNotFoundFiles(wantedFiles);
            Platform.runLater(() -> {
                Alert alert = getAppAlert(Alert.AlertType.WARNING, getClass());
                alert.setTitle("Could not find all files!");
                alert.setHeaderText("The following files were not found:");
                alert.setContentText(notFoundFiles);
                ButtonType cancelButtonType = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
                alert.getDialogPane().getButtonTypes().add(cancelButtonType);
                Optional<ButtonType> result = alert.showAndWait();
                if (result.isPresent() && result.get() == ButtonType.OK) {
                    copyFoundFiles(wantedFiles, directory);
                }
            });
        } else {
            Platform.runLater(() -> {
                Alert alert = getAppAlert(Alert.AlertType.WARNING, getClass());
                alert.setTitle("Wrong folder!");
                alert.setHeaderText("Could not find files from previous installation!");
                alert.setContentText("Select a different folder");
                alert.show();
            });
        }
    }

    private String gatherNotFoundFiles(List<WantedFile> wantedFiles) {
        StringBuilder s = new StringBuilder();
        for (WantedFile wantedFile : wantedFiles) {
            if (!wantedFile.isFound()) {
                s.append(wantedFile.getFilePath()).append("\n");
            }
        }
        return s.toString();
    }

    private void copyFoundFiles(List<WantedFile> wantedFiles, String directory) {
        for (WantedFile wantedFile : wantedFiles) {
            if (!wantedFile.isFound()) {
                continue;
            }
            String fromPath = directory + "/" + wantedFile.getFilePath();
            String toPath = wantedFile.getFilePath();
            fromPath = replaceDirectorySeparator(fromPath);
            toPath = replaceDirectorySeparator(toPath);
            String command = "cmd /c copy /Y " + fromPath + " " + toPath;
            try {
                // with ProcessBuilder it was not good
                Process process = Runtime.getRuntime().exec(command);
                int result = process.waitFor();
                logErrorStream(result, process);
                wantedFile.setCopiedSuccessFully(result == 0);
            } catch (IOException | InterruptedException e) {
                LOG.error(e.getMessage(), e);
            }
        }
        if (!warnIfNotAllFilesWasCopied(wantedFiles)) {
            onStartAsNew();
        }
    }

    private boolean warnIfNotAllFilesWasCopied(List<WantedFile> wantedFiles) {
        StringBuilder message = new StringBuilder();
        for (WantedFile wantedFile : wantedFiles) {
            if (wantedFile.isFound() && !wantedFile.isCopiedSuccessFully()) {
                message.append(wantedFile.getFilePath()).append("\n");
            }
        }
        String s = message.toString();
        if (s.isEmpty()) {
            return false;
        }
        Platform.runLater(() -> {
            Alert alert = getAppAlert(Alert.AlertType.ERROR, getClass());
            alert.setTitle("Could not copy all files!");
            alert.setHeaderText("The following files were not copied:");
            alert.setContentText(s);
            alert.show();
        });
        return true;
    }

    private boolean someFilesFound(List<WantedFile> wantedFiles) {
        for (WantedFile wantedFile : wantedFiles) {
            if (wantedFile.isFound()) {
                return true;
            }
        }
        return false;
    }

    private boolean allFilesFound(List<WantedFile> wantedFiles) {
        for (WantedFile wantedFile : wantedFiles) {
            if (!wantedFile.isFound()) {
                return false;
            }
        }
        return true;
    }

    private void addWantedFile(List<WantedFile> wantedFiles, String filePath) {
        wantedFiles.add(new WantedFile(filePath));
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public interface Listener {
        void onStartAsNew();
    }

    private static class WantedFile {
        private final String filePath;
        private boolean found = false;
        private boolean copiedSuccessFully = false;

        public WantedFile(String filePath) {
            this.filePath = filePath;
        }

        public String getFilePath() {
            return filePath;
        }

        public boolean isFound() {
            return found;
        }

        public void setFound(boolean found) {
            this.found = found;
        }

        public boolean isCopiedSuccessFully() {
            return copiedSuccessFully;
        }

        public void setCopiedSuccessFully(boolean copiedSuccessFully) {
            this.copiedSuccessFully = copiedSuccessFully;
        }
    }
}
