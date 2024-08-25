package projector.application;

import com.bence.projector.common.dto.ProjectorVersionDTO;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import projector.Credentials;
import projector.MainDesktop;
import projector.api.ProjectorVersionApiBean;
import projector.controller.MessageDialogController;
import projector.controller.UpdateController;
import projector.utils.AlertUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static projector.controller.BibleController.setSceneStyleFile;
import static projector.utils.SceneUtils.getAStage;

public class Updater {

    private static final Logger LOG = LoggerFactory.getLogger(Updater.class);
    private static Updater instance;
    @SuppressWarnings("FieldCanBeLocal")
    private final int projectorVersionNumber = 82;
    private final Settings settings = Settings.getInstance();
    private final String updaterPath = "data\\updater.zip";

    private Updater() {
    }

    public static Updater getInstance() {
        if (instance == null) {
            instance = new Updater();
        }
        return instance;
    }

    public static File newFile(File destinationDir, ZipEntry zipEntry) throws IOException {
        File destinationFile = new File(destinationDir, zipEntry.getName());

        String destinationDirPath = destinationDir.getCanonicalPath();
        String destinationFilePath = destinationFile.getCanonicalPath();

        if (!destinationFilePath.startsWith(destinationDirPath + File.separator)) {
            throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
        }

        return destinationFile;
    }

    public void checkForUpdate() {
        Thread thread = new Thread(() -> {
            try {
                ProjectorVersionApiBean projectorVersionApiBean = new ProjectorVersionApiBean();
                List<ProjectorVersionDTO> projectorVersionsAfterNr = projectorVersionApiBean.getProjectorVersionsAfterNr(projectorVersionNumber);
                if (projectorVersionsAfterNr != null && !projectorVersionsAfterNr.isEmpty()) {
                    FXMLLoader loader = new FXMLLoader();
                    loader.setLocation(MainDesktop.class.getResource("/view/UpdateAvailable.fxml"));
                    loader.setResources(settings.getResourceBundle());
                    Pane root = loader.load();
                    UpdateController updateController = loader.getController();
                    updateController.setProjectorVersions(projectorVersionsAfterNr);
                    Scene scene = new Scene(root);
                    setSceneStyleFile(scene);
                    Platform.runLater(() -> {
                        Stage stage = getAStage(getClass());
                        stage.setTitle("Update available");
                        stage.setScene(scene);
                        stage.show();
                        ApplicationUtil.getInstance().addCloseNeededStage(stage);
                    });
                }
            } catch (Exception e) {
                LOG.error(e.getMessage(), e);
            }

        });
        thread.start();
    }

    public void updateExe(List<ProjectorVersionDTO> projectorVersions) {
        Thread thread = new Thread() {
            final int maxVersion = getMaxProjectorVersion(projectorVersions);
            MessageDialogController alert2 = null;

            @Override
            public void run() {
                Platform.runLater(() -> {
                    alert2 = MessageDialogController.getMessageDialog(getClass(), "Update");
                    if (alert2 == null) {
                        return;
                    }
                    alert2.setHeaderText("Update will start to download!");
                    alert2.addHeaderText("\nYou need to wait to complete the download");
                    alert2.addOkButton();
                    alert2.show();
                });
                // alert.showAndWait();
                URL website;
                try {
                    website = new URL(getUrl(maxVersion));
                    ReadableByteChannel rbc = Channels.newChannel(website.openStream());
                    File dir = new File("data");
                    if (!dir.isDirectory()) {
                        Platform.runLater(() -> {
                            alert2.close();
                            Alert alert = new Alert(AlertType.INFORMATION);
                            alert.setTitle("Error");
                            alert.setHeaderText("Try to create data directory in the application folder.");
                            alert.setContentText("If you see this message several times, then you should report it!");
                            alert.showAndWait();
                        });
                        return;
                    }
                    FileOutputStream fos = new FileOutputStream("data\\update.zip");
                    fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
                    fos.close();
                    if (downloadAndUnzipUpdater()) {
                        Platform.runLater(() -> {
                            alert2.close();
                            try {
                                MessageDialogController messageDialog = MessageDialogController.getMessageDialog(getClass(), "Update downloaded!");
                                if (messageDialog == null) {
                                    return;
                                }
                                messageDialog.setHeaderText("The application needs to be closed before installation!");
                                messageDialog.addCancelButton();
                                Button confirmButton = new Button("Close & update");
                                messageDialog.addButton(confirmButton);
                                confirmButton.setOnAction(event -> {
                                    messageDialog.close();
                                    ApplicationUtil.getInstance().closeApplication();
                                    new Thread(() -> {
                                        try {
                                            sleep(1000);
                                            String command = "cmd /c updater.exe";
                                            Runtime.getRuntime().exec(command);
                                        } catch (Exception e) {
                                            LOG.error(e.getMessage(), e);
                                        }
                                    }).start();
                                });
                                messageDialog.show();
                            } catch (Exception e) {
                                LOG.error(e.getMessage(), e);
                            }
                        });
                    }
                } catch (MalformedURLException | FileNotFoundException e) {
                    LOG.error(e.getMessage(), e);
                } catch (IOException e) {
                    Platform.runLater(() -> {
                        alert2.close();
                        Alert alert = AlertUtil.getAppAlert(AlertType.INFORMATION, getClass());
                        alert.setTitle("Couldn't download update!");
                        alert.setHeaderText("No connection with server!");
                        alert.setContentText("Try again later!");
                        alert.showAndWait();
                    });
                    System.out.println("3");
                    LOG.error(e.getMessage(), e);
                }
            }
        };
        thread.start();
    }

    private boolean downloadAndUnzipUpdater() {
        URL website;
        try {
            website = new URL(getUpdaterUrl());
            ReadableByteChannel rbc = Channels.newChannel(website.openStream());
            File dir = new File("data");
            if (!dir.isDirectory()) {
                return false;
            }
            FileOutputStream fos = new FileOutputStream(updaterPath);
            fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
            fos.close();
            return unzipUpdater();
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
        return false;
    }

    private boolean unzipUpdater() {
        try {
            File destinationDir = new File("./");
            byte[] buffer = new byte[1024];
            File updateFile = new File(updaterPath);
            if (!updateFile.exists()) {
                return false;
            }
            ZipInputStream zis = new ZipInputStream(new FileInputStream(updateFile));
            ZipEntry zipEntry = zis.getNextEntry();
            while (zipEntry != null) {
                File newFile = newFile(destinationDir, zipEntry);
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

    private int getMaxProjectorVersion(List<ProjectorVersionDTO> projectorVersions) {
        int maxVersion = 0;
        for (ProjectorVersionDTO projectorVersion : projectorVersions) {
            if (projectorVersion.getVersionId() > maxVersion) {
                maxVersion = projectorVersion.getVersionId();
            }
        }
        return maxVersion;
    }

    String getUrl(int version) {
        return Credentials.BASE_URL + "/api/files/projectorUpdate" + version + ".zip";
    }

    private String getUpdaterUrl() {
        return Credentials.BASE_URL + "/api/files/projectorUpdater.zip";
    }

    public void saveApplicationStartedWithVersion() {
        try {
            ApplicationVersion applicationVersion = ApplicationVersion.getInstance();
            applicationVersion.setVersion(projectorVersionNumber);
            applicationVersion.save();
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    public void updateExe2() {
        Thread thread = new Thread(() -> {
            try {
                ProjectorVersionApiBean projectorVersionApiBean = new ProjectorVersionApiBean();
                List<ProjectorVersionDTO> projectorVersionsAfterNr = projectorVersionApiBean.getProjectorVersionsAfterNr(projectorVersionNumber);
                if (projectorVersionsAfterNr != null && !projectorVersionsAfterNr.isEmpty()) {
                    Updater updater = Updater.getInstance();
                    updater.updateExe(projectorVersionsAfterNr);
                }
            } catch (Exception e) {
                LOG.error(e.getMessage(), e);
            }
        });
        thread.start();
    }
}
