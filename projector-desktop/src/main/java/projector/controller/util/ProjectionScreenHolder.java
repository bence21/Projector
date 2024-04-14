package projector.controller.util;

import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import projector.application.ProjectionScreenSettings;
import projector.controller.ProjectionScreenController;
import projector.controller.listener.OnMainPaneSizeChangeListener;
import projector.controller.listener.PopupCreatedListener;

import java.util.ArrayList;
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
            projectionScreenSettings.changingName(newValue);
            setName(newValue);
        });
    }
}
