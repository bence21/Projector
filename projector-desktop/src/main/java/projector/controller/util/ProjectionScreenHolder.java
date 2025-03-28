package projector.controller.util;

import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.stage.Popup;
import javafx.stage.Stage;
import projector.application.ProjectionScreenSettings;
import projector.application.ProjectionType;
import projector.controller.ProjectionScreenController;
import projector.controller.ProjectionScreensController;
import projector.controller.listener.OnMainPaneSizeChangeListener;
import projector.controller.listener.PopupCreatedListener;
import projector.model.Song;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ProjectionScreenHolder {
    private final ProjectionScreenSettings projectionScreenSettings;
    private ProjectionScreenController projectionScreenController;
    private String name;
    private PopupCreatedListener popupCreatedListener;
    private OnMainPaneSizeChangeListener onMainPaneSizeChangeListener = null;
    private double lastWidth = 0.0;
    private double lastHeight = 0.0;
    private Integer screenIndex = null;
    private boolean openedAutomatically;
    private javafx.scene.layout.HBox HBox;
    private int doubleIndex;
    private final List<OnResultListener> onNameChangeListeners = new ArrayList<>();
    private boolean preview;
    private ProjectionScreensController.Bunch bunch;
    private ProjectionScreenSettings.Listener onProjectionToggleListener;

    public ProjectionScreenHolder(ProjectionScreenController projectionScreenController, String name) {
        this.projectionScreenController = projectionScreenController;
        this.name = name;
        this.projectionScreenSettings = new ProjectionScreenSettings(this);
    }

    public ProjectionScreenController getProjectionScreenController() {
        return projectionScreenController;
    }

    public void setProjectionScreenController(ProjectionScreenController projectionScreenController) {
        this.projectionScreenController = projectionScreenController;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        onNameChanged();
    }

    public void onNameChanged() {
        for (OnResultListener onNameChangeListener : onNameChangeListeners) {
            onNameChangeListener.onResult();
        }
    }

    public void addOnNameChangeListener(OnResultListener onNameChangeListener) {
        onNameChangeListeners.add(onNameChangeListener);
    }

    public ProjectionScreenSettings getProjectionScreenSettings() {
        return projectionScreenSettings;
    }

    public void setOnPopupCreatedListener(PopupCreatedListener popupCreatedListener) {
        this.popupCreatedListener = popupCreatedListener;
    }

    public void popupCreated() {
        if (popupCreatedListener != null) {
            popupCreatedListener.popupCreated();
        }
    }

    public void setOnMainPaneSizeChangeListener(OnMainPaneSizeChangeListener onMainPaneSizeChangeListener) {
        this.onMainPaneSizeChangeListener = onMainPaneSizeChangeListener;
        onMainPaneSizeChangeListener.onMainPaneSizeChange(lastWidth, lastHeight);
    }

    public void onSizeChanged(double width, double height) {
        this.lastWidth = width;
        this.lastHeight = height;
        if (onMainPaneSizeChangeListener != null) {
            onMainPaneSizeChangeListener.onMainPaneSizeChange(width, height);
        }
    }

    public Integer getScreenIndex() {
        return screenIndex;
    }

    public void setScreenIndex(Integer index) {
        this.screenIndex = index;
    }

    public boolean isOpenedAutomatically() {
        return openedAutomatically;
    }

    public void setOpenedAutomatically(boolean openedAutomatically) {
        this.openedAutomatically = openedAutomatically;
    }

    public void close() {
        projectionScreenController.close();
        projectionScreenController = null;
    }

    public HBox getHBox() {
        return HBox;
    }

    public void setHBox(HBox hBox) {
        this.HBox = hBox;
    }

    public int getDoubleIndex() {
        return doubleIndex;
    }

    public void setDoubleIndex(int doubleIndex) {
        this.doubleIndex = doubleIndex;
    }

    public void setStage(Stage stage) {
        if (stage == null) {
            return;
        }
        stage.titleProperty().addListener((observable, oldValue, newValue) -> {
            projectionScreenSettings.renameSettingsFile(newValue);
            setName(newValue);
        });
    }

    public boolean isNotWithScreen() {
        return projectionScreenController == null || projectionScreenController.getScreen() == null;
    }

    public void reload() {
        projectionScreenSettings.reload();
        onSettingsChanged();
        onNameChanged();
    }

    public void clearMonitorCache() {
        projectionScreenSettings.clearMonitorCache();
    }

    public void setLock(boolean lock) {
        projectionScreenController.setLock(lock);
    }

    public void setBlank(boolean blank) {
        projectionScreenController.setBlank(blank);
    }

    public void setText(String text, ProjectionType projectionType, ProjectionData projectionData) {
        projectionScreenController.setText(text, projectionType, projectionData);
    }

    public void drawImage(Image image) {
        projectionScreenController.drawImage(image);
    }

    public void stopCountDownTimer() {
        projectionScreenController.stopCountDownTimer();
    }

    public void clearAll() {
        projectionScreenController.clearAll();
    }

    public void onClose() {
        projectionScreenController.onClose();
    }

    public void setCountDownTimer(Date finishDate, AutomaticAction selectedAction, boolean showFinishTime) {
        projectionScreenController.setCountDownTimer(finishDate, selectedAction, showFinishTime);
    }

    public void setImage(String fileImagePath, ProjectionType projectionType, String nextFileImagePath) {
        projectionScreenController.setImage(fileImagePath, projectionType, nextFileImagePath);
    }

    public void setNextScheduledSong(Song nextScheduledSong) {
        projectionScreenController.setNextScheduledSong(nextScheduledSong);
    }

    public void songEnding() {
        projectionScreenController.songEnding();
    }

    public void setProgress(double progress) {
        projectionScreenController.setProgress(progress);
    }

    public void setPreview(boolean preview) {
        this.preview = preview;
    }

    public boolean isNotPreview() {
        return !preview;
    }

    public void onSettingsChanged() {
        projectionScreenController.onSettingsChanged();
    }

    public void setBackGroundColor() {
        projectionScreenController.setBackGroundColor();
    }

    public String getNameWithDefault() {
        String s = getProjectionScreenSettings().getName();
        String s2 = getName();
        String result = s;
        if (!s.equals(s2)) {
            String s1 = " (" + s2 + ")";
            result += s1;
        }
        return result;
    }

    public void hidePopups() {
        projectionScreenController.hidePopups();
    }

    public void setBunch(ProjectionScreensController.Bunch bunch) {
        this.bunch = bunch;
    }

    public ProjectionScreensController.Bunch getBunch() {
        return bunch;
    }

    public boolean isPopupShowing() {
        ProjectionScreenController projectionScreenController = getProjectionScreenController();
        if (projectionScreenController == null) {
            return false;
        }
        Popup popup = projectionScreenController.getPopup();
        if (popup == null) {
            return false;
        }
        return popup.isShowing();
    }

    public void setOnProjectionToggle(ProjectionScreenSettings.Listener onProjectionToggle) {
        this.onProjectionToggleListener = onProjectionToggle;
    }

    public void onProjectionToggle() {
        if (onProjectionToggleListener != null) {
            onProjectionToggleListener.onChanged();
        }
    }
}
