package projector.controller.util;

import javafx.application.Platform;
import javafx.scene.image.Image;
import projector.application.ProjectionType;
import projector.controller.ProjectionScreenController;
import projector.controller.ProjectionTextChangeListener;
import projector.controller.listener.ProjectionScreenListener;
import projector.model.Song;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

public class ProjectionScreensUtil {
    private static ProjectionScreensUtil instance = null;
    private final List<ProjectionScreenHolder> projectionScreenHolders;
    private final Map<Integer, ProjectionScreenHolder> doubleScreenHolders;
    private final Map<Integer, ProjectionScreenHolder> automaticScreenHolders;
    private final List<ProjectionScreenListener> projectionScreenListeners;
    private boolean lock;
    @SuppressWarnings({"FieldCanBeLocal", "unused"})
    private boolean blank;
    private List<ProjectionTextChangeListener> projectionTextChangeListeners;
    private String text = "";
    private ProjectionType projectionType = null;
    private ProjectionData projectionData = null;
    private ProjectionScreenController mainProjectionScreenController = null;

    private ProjectionScreensUtil() {
        projectionScreenHolders = new ArrayList<>();
        projectionScreenListeners = new ArrayList<>();
        doubleScreenHolders = new HashMap<>();
        automaticScreenHolders = new HashMap<>();
    }

    public static ProjectionScreensUtil getInstance() {
        if (instance == null) {
            instance = new ProjectionScreensUtil();
        }
        return instance;
    }

    public List<ProjectionScreenHolder> getProjectionScreenHolders() {
        return projectionScreenHolders;
    }

    public ProjectionScreenHolder addProjectionScreenController(ProjectionScreenController projectionScreenController, String name) {
        ProjectionScreenHolder projectionScreenHolder = new ProjectionScreenHolder(projectionScreenController, name);
        addProjectionScreenHolder(projectionScreenHolder);
        projectionScreenController.setProjectionScreenSettings(projectionScreenHolder.getProjectionScreenSettings());
        return projectionScreenHolder;
    }

    private void addProjectionScreenHolder(ProjectionScreenHolder projectionScreenHolder) {
        projectionScreenHolders.add(projectionScreenHolder);
        for (ProjectionScreenListener projectionScreenListener : projectionScreenListeners) {
            projectionScreenListener.onNew(projectionScreenHolder);
        }
    }

    public void addDoubleProjectionScreenController(ProjectionScreenController doubleProjectionScreenController) {
        getADoubleProjectionScreenHolder(doubleProjectionScreenController, " - double screen", doubleScreenHolders);
    }

    private int getNextIndex(Map<Integer, ProjectionScreenHolder> screenHolders) {
        int n = screenHolders.size();
        for (int i = 0; i < n; ++i) {
            if (!screenHolders.containsKey(i)) {
                return i;
            }
        }
        return n;
    }

    public void addAutomaticDoubleProjectionScreenController(ProjectionScreenController doubleProjectionScreenController) {
        ProjectionScreenHolder projectionScreenHolder = getADoubleProjectionScreenHolder(doubleProjectionScreenController, " - screen", automaticScreenHolders);
        projectionScreenHolder.setOpenedAutomatically(true);
    }

    private ProjectionScreenHolder getADoubleProjectionScreenHolder(ProjectionScreenController doubleProjectionScreenController, String caption, Map<Integer, ProjectionScreenHolder> screenHolders) {
        int index = getNextIndex(screenHolders);
        int number = index + 2;
        String name = number + caption;
        ProjectionScreenHolder projectionScreenHolder = new ProjectionScreenHolder(doubleProjectionScreenController, name);
        projectionScreenHolder.setDoubleIndex(index);
        addProjectionScreenHolder(projectionScreenHolder);
        doubleProjectionScreenController.setProjectionScreenSettings(projectionScreenHolder.getProjectionScreenSettings());
        screenHolders.put(index, projectionScreenHolder);
        return projectionScreenHolder;
    }

    public void addProjectionScreenListener(ProjectionScreenListener projectionScreenListener) {
        projectionScreenListeners.add(projectionScreenListener);
    }

    public void removeProjectionScreenController(ProjectionScreenController projectionScreenController) {
        ProjectionScreenHolder projectionScreenHolder = projectionScreenController.getProjectionScreenSettings().getProjectionScreenHolder();
        doubleScreenHolders.remove(projectionScreenHolder.getDoubleIndex());
        removeProjectionScreenHolder(projectionScreenHolder);
    }

    public ProjectionScreenHolder getScreenHolderByIndex(Integer index) {
        if (index < 0 || index >= automaticScreenHolders.size()) {
            return null;
        }
        return automaticScreenHolders.get(index);
    }

    public void closeFromIndex(int index) {
        while (index < automaticScreenHolders.size()) {
            ProjectionScreenHolder projectionScreenHolder = getScreenHolderByIndex(index);
            if (projectionScreenHolder == null) {
                return;
            }
            if (projectionScreenHolder.isOpenedAutomatically()) {
                projectionScreenHolder.close();
                automaticScreenHolders.remove(projectionScreenHolder.getDoubleIndex());
                removeProjectionScreenHolder(projectionScreenHolder);
            }
            ++index;
        }
    }

    private void removeProjectionScreenHolder(ProjectionScreenHolder projectionScreenHolder) {
        projectionScreenHolders.remove(projectionScreenHolder);
        onRemoveListenersCall(projectionScreenHolder);
    }

    private void onRemoveListenersCall(ProjectionScreenHolder projectionScreenHolder) {
        for (ProjectionScreenListener projectionScreenListener : projectionScreenListeners) {
            projectionScreenListener.onRemoved(projectionScreenHolder);
        }
    }

    public boolean isLock() {
        return lock;
    }

    public void setLock(boolean lock) {
        this.lock = lock;
        for (ProjectionScreenHolder projectionScreenHolder : projectionScreenHolders) {
            if (projectionScreenHolder.isNotPreview()) {
                projectionScreenHolder.setLock(lock);
            }
        }
    }

    public void setBlank(boolean blank) {
        this.blank = blank;
        for (ProjectionScreenHolder projectionScreenHolder : projectionScreenHolders) {
            if (projectionScreenHolder.isNotPreview()) {
                projectionScreenHolder.setBlank(blank);
            }
        }
    }

    public void setText(String text, ProjectionType projectionType, ProjectionData projectionData) {
        this.text = text;
        this.projectionType = projectionType;
        this.projectionData = projectionData;
        for (ProjectionScreenHolder projectionScreenHolder : projectionScreenHolders) {
            projectionScreenHolder.setText(text, projectionType, projectionData);
        }
        for (ProjectionTextChangeListener projectionTextChangeListener : getProjectionTextChangeListeners()) {
            projectionTextChangeListener.onSetText(text, projectionType, projectionData);
        }
    }

    public void drawImage(Image image) {
        for (ProjectionScreenHolder projectionScreenHolder : projectionScreenHolders) {
            projectionScreenHolder.drawImage(image);
        }
    }

    public void addProjectionTextChangeListener(ProjectionTextChangeListener projectionTextChangeListener) {
        getProjectionTextChangeListeners().add(projectionTextChangeListener);
        projectionTextChangeListener.onSetText(text, projectionType, projectionData);
    }

    private synchronized List<ProjectionTextChangeListener> getProjectionTextChangeListeners() {
        if (projectionTextChangeListeners == null) {
            projectionTextChangeListeners = new CopyOnWriteArrayList<>();
        }
        return projectionTextChangeListeners;
    }

    public void removeProjectionTextChangeListener(ProjectionTextChangeListener projectionTextChangeListener) {
        if (projectionTextChangeListeners != null) {
            Platform.runLater(() -> projectionTextChangeListeners.remove(projectionTextChangeListener));
        }
        if (mainProjectionScreenController != null) {
            mainProjectionScreenController.removeProjectionTextChangeListener((projectionTextChangeListener));
        }
    }

    public void addImageChangeListenerToProjectionScreenController(ProjectionTextChangeListener projectionTextChangeListener, ProjectionScreenController projectionScreenController) {
        this.mainProjectionScreenController = projectionScreenController;
        projectionScreenController.addProjectionImageChangeListener(projectionTextChangeListener);
    }

    public void stopOtherCountDownTimer() {
        for (ProjectionScreenHolder projectionScreenHolder : projectionScreenHolders) {
            projectionScreenHolder.stopCountDownTimer();
        }
    }

    public void clearAll() {
        for (ProjectionScreenHolder projectionScreenHolder : projectionScreenHolders) {
            projectionScreenHolder.clearAll();
        }
        for (ProjectionTextChangeListener projectionTextChangeListener : getProjectionTextChangeListeners()) {
            projectionTextChangeListener.onSetText("", ProjectionType.CLEAR, projectionData);
        }
    }

    public void onClose() {
        for (ProjectionScreenHolder projectionScreenHolder : projectionScreenHolders) {
            projectionScreenHolder.onClose();
        }
    }

    public void setCountDownTimer(ProjectionScreenController selectedProjectionScreenController, Date finishDate, AutomaticAction selectedAction, boolean showFinishTime) {
        if (selectedProjectionScreenController == null) {
            stopOtherCountDownTimer();
            for (ProjectionScreenHolder projectionScreenHolder : projectionScreenHolders) {
                projectionScreenHolder.setCountDownTimer(finishDate, selectedAction, showFinishTime);
            }
            for (ProjectionTextChangeListener projectionTextChangeListener : getProjectionTextChangeListeners()) {
                projectionTextChangeListener.onSetCountDownTimer(finishDate, selectedAction, showFinishTime);
            }
        } else {
            selectedProjectionScreenController.setCountDownTimer(finishDate, selectedAction, showFinishTime);
        }
    }

    public void setImage(String fileImagePath, ProjectionType projectionType, String nextFileImagePath) {
        for (ProjectionScreenHolder projectionScreenHolder : projectionScreenHolders) {
            projectionScreenHolder.setImage(fileImagePath, projectionType, nextFileImagePath);
        }
    }

    public void songEnding() {
        for (ProjectionScreenHolder projectionScreenHolder : projectionScreenHolders) {
            projectionScreenHolder.songEnding();
        }
    }

    public void setProgress(double progress) {
        for (ProjectionScreenHolder projectionScreenHolder : projectionScreenHolders) {
            projectionScreenHolder.setProgress(progress);
        }
    }

    public void onSettingsChanged() {
        for (ProjectionScreenHolder projectionScreenHolder : projectionScreenHolders) {
            projectionScreenHolder.onSettingsChanged();
        }
    }

    public void setBackGroundColor() {
        for (ProjectionScreenHolder projectionScreenHolder : projectionScreenHolders) {
            projectionScreenHolder.setBackGroundColor();
        }
    }

    public void hidePopups() {
        for (ProjectionScreenHolder projectionScreenHolder : projectionScreenHolders) {
            projectionScreenHolder.hidePopups();
        }
    }

    public void onProjectionToggle() {
        for (ProjectionScreenHolder projectionScreenHolder : projectionScreenHolders) {
            projectionScreenHolder.onProjectionToggle();
        }
    }

    public void setNextScheduledSong(Song nextScheduledSong) {
        for (ProjectionScreenHolder projectionScreenHolder : projectionScreenHolders) {
            projectionScreenHolder.setNextScheduledSong(nextScheduledSong);
        }
    }
}
