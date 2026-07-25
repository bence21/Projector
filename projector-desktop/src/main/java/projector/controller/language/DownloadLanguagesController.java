package projector.controller.language;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import projector.MainDesktop;
import projector.api.LanguageApiBean;
import projector.api.RemoteFetchFailureKind;
import projector.api.RemoteFetchResult;
import projector.application.Settings;
import projector.controller.song.DownloadSongsController;
import projector.controller.song.SongController;
import projector.controller.util.ControllerUtil;
import projector.model.Language;
import projector.service.LanguageService;
import projector.service.ServiceManager;
import projector.service.SongService;
import projector.utils.ConnectionErrorMessages;
import projector.utils.DownloadWorkEstimator;
import projector.utils.DownloadWorkPlan;
import projector.utils.ForkMirrorMigrationState;
import projector.utils.SceneUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;

public class DownloadLanguagesController {
    private static final Logger LOG = LoggerFactory.getLogger(DownloadLanguagesController.class);
    private final Settings settings = Settings.getInstance();
    private final ResourceBundle resourceBundle = settings.getResourceBundle();
    @FXML
    private Label label;
    @FXML
    private ProgressBar languageListProgressBar;
    @FXML
    private Label migrationWarningLabel;
    @FXML
    private Button selectButton;
    @FXML
    private VBox listView;
    private List<Language> languages;
    private List<CheckBox> checkBoxes;
    private SongController songController;
    private LanguageService languageService;
    private Stage stage;

    public void initialize() {
        languageService = ServiceManager.getLanguageService();
        languages = languageService.findAll();
        languageService.sortLanguages(languages);
        checkBoxes = new ArrayList<>(languages.size());
        for (Language language : languages) {
            addLanguageToVBox(language);
        }
        updateMigrationWarningLabel();
        updateSelectButtonState();
        LanguageApiBean languageApiBean = new LanguageApiBean();
        checkOnlineLanguages(languageApiBean);
        setSelectButtonAction();
    }

    private void checkOnlineLanguages(LanguageApiBean languageApiBean) {
        showCheckingLanguagesStatus();
        Thread thread = new Thread(() -> {
            RemoteFetchResult<List<Language>> languagesResult = languageApiBean.getLanguagesResult();
            if (languagesResult.isFailure()) {
                Platform.runLater(() -> {
                    hideLanguageListProgress();
                    showRemoteFetchFailure(languagesResult.getFailureKind());
                });
                return;
            }
            List<Language> onlineLanguages = languagesResult.getData();
            HashMap<String, Language> languagesByUuid = new HashMap<>();
            for (Language language : languages) {
                languagesByUuid.put(language.getUuid(), language);
            }
            for (Language onlineLanguage : onlineLanguages) {
                Language existing = languagesByUuid.get(onlineLanguage.getUuid());
                if (existing != null) {
                    if (onlineLanguage.getCountedSongsSize() > 0) {
                        existing.setSongsSize(onlineLanguage.getCountedSongsSize());
                    }
                } else {
                    languages.add(onlineLanguage);
                    languagesByUuid.put(onlineLanguage.getUuid(), onlineLanguage);
                    Platform.runLater(() -> addLanguageToVBox(onlineLanguage));
                }
            }
            Set<String> softDeletedUuids = pruneSoftDeletedLanguages(languageApiBean);
            Platform.runLater(() -> {
                removeSoftDeletedFromDownloadList(softDeletedUuids);
                clearLanguageListStatus();
                updateSelectButtonState();
                updateMigrationWarningLabel();
            });
        });
        thread.start();
    }

    private void setSelectButtonAction() {
        selectButton.setOnAction(event -> {
            try {
                for (int i = 0; i < languages.size(); ++i) {
                    languages.get(i).setSelected(checkBoxes.get(i).isSelected());
                }
                languageService.create(languages);
                SongService songService = ServiceManager.getSongService();
                DownloadWorkPlan downloadWorkPlan = DownloadWorkEstimator.estimate(languages, songService);
                FXMLLoader loader = new FXMLLoader();
                loader.setLocation(MainDesktop.class.getResource("/view/song/DownloadSongs.fxml"));
                loader.setResources(settings.getResourceBundle());
                Pane root = loader.load();
                DownloadSongsController downloadSongsController = loader.getController();
                downloadSongsController.startDownload(downloadWorkPlan);
                Stage stage = ControllerUtil.getStageWithRoot(getClass(), root);
                stage.setTitle(Settings.getInstance().getResourceBundle().getString("Download songs"));
                stage.show();
                stage.setOnCloseRequest(event1 -> songController.reloadInitialSongs());
                this.stage.close();
            } catch (Exception e) {
                LOG.error(e.getMessage(), e);
            }
        });
    }

    /**
     * Soft-deleted server languages must leave the download UI. Local rows with no songs are
     * removed from storage; rows that still have songs stay so local data is not orphaned.
     */
    private Set<String> pruneSoftDeletedLanguages(LanguageApiBean languageApiBean) {
        Set<String> softDeletedUuids = new HashSet<>();
        try {
            List<Language> deletedLanguages = languageApiBean.getDeletedLanguages();
            if (deletedLanguages == null) {
                return softDeletedUuids;
            }
            LanguageService languageService = ServiceManager.getLanguageService();
            for (Language language : deletedLanguages) {
                String uuid = language.getUuid();
                if (uuid == null) {
                    continue;
                }
                softDeletedUuids.add(uuid);
                deleteLocalSoftDeletedLanguageIfEmpty(languageService, uuid);
            }
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
        return softDeletedUuids;
    }

    private void deleteLocalSoftDeletedLanguageIfEmpty(LanguageService languageService, String uuid) {
        Language byUuid = languageService.findByUuid(uuid);
        if (byUuid == null) {
            return;
        }
        if (byUuid.getCountedSongsSize() == 0) {
            languageService.delete(byUuid);
            return;
        }
        if (byUuid.isSelected()) {
            byUuid.setSelected(false);
            languageService.update(byUuid);
        }
    }

    private void removeSoftDeletedFromDownloadList(Set<String> softDeletedUuids) {
        if (softDeletedUuids == null || softDeletedUuids.isEmpty()) {
            return;
        }
        for (int i = languages.size() - 1; i >= 0; --i) {
            Language language = languages.get(i);
            String uuid = language.getUuid();
            if (uuid == null || !softDeletedUuids.contains(uuid)) {
                continue;
            }
            languages.remove(i);
            if (i < checkBoxes.size()) {
                CheckBox checkBox = checkBoxes.remove(i);
                listView.getChildren().remove(checkBox);
            }
        }
    }

    private void addLanguageToVBox(Language language) {
        CheckBox checkBox = new CheckBox(language.getEnglishName() + " - " + language.getNativeName());
        checkBox.setSelected(language.isSelected());
        checkBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
            updateSelectButtonState();
            updateMigrationWarningLabel();
        });
        if (Platform.isFxApplicationThread()) {
            listView.getChildren().add(checkBox);
        } else {
            Platform.runLater(() -> listView.getChildren().add(checkBox));
        }
        checkBoxes.add(checkBox);
    }

    private void showCheckingLanguagesStatus() {
        Platform.runLater(() -> {
            label.setText(resourceBundle.getString("Checking for languages"));
            SceneUtils.setVisibleAndManaged(label, true);
            if (languageListProgressBar != null) {
                languageListProgressBar.setProgress(ProgressBar.INDETERMINATE_PROGRESS);
                SceneUtils.setVisibleAndManaged(languageListProgressBar, true);
            }
        });
    }

    private void hideLanguageListProgress() {
        if (languageListProgressBar != null) {
            SceneUtils.setVisibleAndManaged(languageListProgressBar, false);
        }
    }

    private void clearLanguageListStatus() {
        label.setText("");
        SceneUtils.setVisibleAndManaged(label, false);
        hideLanguageListProgress();
    }

    private void updateSelectButtonState() {
        if (selectButton == null) {
            return;
        }
        int selectedCount = getSelectedCheckboxCount();
        selectButton.setText(String.format(resourceBundle.getString("Download selected count"), selectedCount));
        selectButton.setDisable(selectedCount == 0);
    }

    private int getSelectedCheckboxCount() {
        int count = 0;
        for (CheckBox checkBox : checkBoxes) {
            if (checkBox.isSelected()) {
                ++count;
            }
        }
        return count;
    }

    private void updateMigrationWarningLabel() {
        if (migrationWarningLabel == null) {
            return;
        }
        SongService songService = ServiceManager.getSongService();
        boolean show = anyCheckedNeedsMigration(songService);
        migrationWarningLabel.setText(resourceBundle.getString("Fork mirror migration warning short"));
        migrationWarningLabel.setTooltip(new Tooltip(resourceBundle.getString("Fork mirror migration warning")));
        SceneUtils.setVisibleAndManaged(migrationWarningLabel, show);
    }

    private boolean anyCheckedNeedsMigration(SongService songService) {
        for (int i = 0; i < languages.size() && i < checkBoxes.size(); ++i) {
            if (checkBoxes.get(i).isSelected() && ForkMirrorMigrationState.needsMigration(languages.get(i), songService)) {
                return true;
            }
        }
        return false;
    }

    private void showRemoteFetchFailure(RemoteFetchFailureKind failureKind) {
        label.setText(ConnectionErrorMessages.getMessage(resourceBundle, failureKind));
        SceneUtils.setVisibleAndManaged(label, true);
    }

    public void setSongController(SongController songController) {
        this.songController = songController;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }
}
