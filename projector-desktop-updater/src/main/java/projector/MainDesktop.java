package projector;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import projector.controller.MainController;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static projector.utils.ProcessUtil.killOtherProcesses;

public class MainDesktop extends Application {

    private static final Logger LOG = LoggerFactory.getLogger(MainDesktop.class);

    public static void main(String[] args) {
        launch(args);
    }

    public static File newFile(File destinationDir, ZipEntry zipEntry) throws IOException {
        File destFile = new File(destinationDir, zipEntry.getName());

        String destDirPath = destinationDir.getCanonicalPath();
        String destFilePath = destFile.getCanonicalPath();

        if (!destFilePath.startsWith(destDirPath + File.separator)) {
            throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
        }

        return destFile;
    }

    @Override
    public void start(Stage primaryStage) {
        try {
            LOG.info("Starting");
            killOtherProcesses();
            Stage stage = new Stage();
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/view/MainView.fxml"));
            BorderPane borderPane = loader.load();
            MainController mainController = loader.getController();
            Scene scene = new Scene(borderPane, borderPane.getPrefWidth(), borderPane.getPrefHeight());
            stage.setScene(scene);
            stage.setTitle("Projector - update");
            stage.show();
            if (unzipUpdate(mainController)) {
                stage.close();
                boolean isWindows = System.getProperty("os.name").toLowerCase().contains("win");
    
                if (isWindows) {
                    Runtime.getRuntime().exec("cmd /c Projector.exe");
                } else {
                    File projectorFile = new File("Projector");
                    if (projectorFile.exists()) {
                        // Ensure Linux binary is executable
                        projectorFile.setExecutable(true);
                        Runtime.getRuntime().exec("./Projector");
                    } else {
                        LOG.error("Projector binary not found at: " + projectorFile.getAbsolutePath());
                    }
                }
            }
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    private boolean unzipUpdate(MainController mainController) {
        try {
            File destDir = new File("./");
            byte[] buffer = new byte[1024];
            File updateFile = new File("data/update.zip");
            if (!updateFile.exists()) {
                mainController.statusLabel.setText("data/update.zip not found");
                return false;
            }
            ZipInputStream zis = new ZipInputStream(new FileInputStream(updateFile));
            ZipEntry zipEntry = zis.getNextEntry();
            while (zipEntry != null) {
                File newFile = newFile(destDir, zipEntry);
                if (zipEntry.isDirectory()) {
                    if (!newFile.isDirectory() && !newFile.mkdirs()) {
                        throw new IOException("Failed to create directory " + newFile);
                    }
                } else {
                    File parent = newFile.getParentFile();
                    if (!parent.isDirectory() && !parent.mkdirs()) {
                        throw new IOException("Failed to create directory " + parent);
                    }
                    FileOutputStream fos = new FileOutputStream(newFile);
                    int len;
                    while ((len = zis.read(buffer)) > 0) {
                        fos.write(buffer, 0, len);
                    }
                    fos.close();
                }
                zipEntry = zis.getNextEntry();
            }
            zis.closeEntry();
            zis.close();
            //noinspection ResultOfMethodCallIgnored
            updateFile.delete();
            return true;
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return false;
        }
    }
}
