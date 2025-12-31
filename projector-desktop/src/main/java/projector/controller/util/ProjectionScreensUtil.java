package projector.controller.util;

import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.scene.media.MediaPlayer;
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
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

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
    private String fileImagePath = null;
    private Image lastImage = null;
    private ProjectionScreenController mainProjectionScreenController = null;
    private ScheduledExecutorService syncExecutorService = null;
    private ScheduledFuture<?> syncTask = null;
    private static final double SYNC_THRESHOLD = 0.2; // seconds
    private static final long SYNC_CHECK_INTERVAL = 2000; // milliseconds
    private double currentVideoVolume = 1.0; // Track current volume setting

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
        // Clear image context when setting text-based projection types
        this.fileImagePath = null;
        this.lastImage = null;
        for (ProjectionScreenHolder projectionScreenHolder : projectionScreenHolders) {
            projectionScreenHolder.setText(text, projectionType, projectionData);
        }
        for (ProjectionTextChangeListener projectionTextChangeListener : getProjectionTextChangeListeners()) {
            projectionTextChangeListener.onSetText(text, projectionType, projectionData);
        }
    }

    public void drawImage(Image image) {
        ProjectionType projectionType = ProjectionType.IMAGE;
        this.projectionType = projectionType;
        this.lastImage = image;
        this.fileImagePath = null;
        // Clear text context when setting image
        this.text = "";
        this.projectionData = null;
        for (ProjectionScreenHolder projectionScreenHolder : projectionScreenHolders) {
            projectionScreenHolder.drawImage(image, projectionType);
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
        // Update context for CLEAR projection type
        ProjectionType projectionType = ProjectionType.CLEAR;
        this.projectionType = projectionType;
        this.text = "";
        this.projectionData = null;
        this.fileImagePath = null;
        this.lastImage = null;
        for (ProjectionScreenHolder projectionScreenHolder : projectionScreenHolders) {
            projectionScreenHolder.clearAll(projectionType);
        }
        for (ProjectionTextChangeListener projectionTextChangeListener : getProjectionTextChangeListeners()) {
            projectionTextChangeListener.onSetText("", projectionType, projectionData);
        }
    }

    public void onClose() {
        stopVideoSyncTask();
        // Stop MediaPlayer on main projection screen (preview) if it exists
        if (mainProjectionScreenController != null) {
            mainProjectionScreenController.stopMediaPlayer();
        }
        // Stop all MediaPlayers on projection screens
        for (ProjectionScreenHolder projectionScreenHolder : projectionScreenHolders) {
            ProjectionScreenController controller = projectionScreenHolder.getProjectionScreenController();
            if (controller != null) {
                // Stop MediaPlayer if video is playing
                controller.stopMediaPlayer();
            }
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
        this.projectionType = projectionType;
        updateImageContext(fileImagePath);
        // Clear text context when setting image
        this.text = "";
        this.projectionData = null;
        for (ProjectionScreenHolder projectionScreenHolder : projectionScreenHolders) {
            projectionScreenHolder.setImage(fileImagePath, projectionType, nextFileImagePath);
        }
        // If this is a video file, ensure only first screen has sound
        if (projector.controller.GalleryController.isMediaFile(fileImagePath)) {
            // Set volume: first screen gets default volume (1.0), others muted
            setVideoVolumeOnAllScreens(1.0);
        }
    }

    /**
     * Updates the image context without broadcasting to controllers.
     * Used internally to avoid circular updates.
     */
    private void updateImageContext(String fileImagePath) {
        this.fileImagePath = fileImagePath;
        this.lastImage = null;
    }

    public void songEnding() {
        // Update context for SONG_ENDING projection type
        ProjectionType projectionType = ProjectionType.SONG_ENDING;
        this.projectionType = projectionType;
        // Clear image context when setting song ending
        this.fileImagePath = null;
        this.lastImage = null;
        for (ProjectionScreenHolder projectionScreenHolder : projectionScreenHolders) {
            projectionScreenHolder.songEnding(projectionType);
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

    public void playVideoOnAllScreens() {
        // Ensure volume is set correctly when starting playback
        setVideoVolumeOnAllScreens(currentVideoVolume);
        for (ProjectionScreenHolder projectionScreenHolder : projectionScreenHolders) {
            projectionScreenHolder.getProjectionScreenController().playVideoPlayer();
        }
    }

    public void pauseVideoOnAllScreens() {
        for (ProjectionScreenHolder projectionScreenHolder : projectionScreenHolders) {
            projectionScreenHolder.getProjectionScreenController().pauseVideoPlayer();
        }
    }

    public void seekVideoOnAllScreens(javafx.util.Duration time) {
        for (ProjectionScreenHolder projectionScreenHolder : projectionScreenHolders) {
            projectionScreenHolder.getProjectionScreenController().seekVideoPlayer(time);
        }
    }

    public void setVideoVolumeOnAllScreens(double volume) {
        currentVideoVolume = volume; // Store the current volume setting
        if (projectionScreenHolders.isEmpty()) {
            // If there are no projection screens, the video viewer (preview) should have sound
            // Volume will be set directly on the VideoViewerController's MediaPlayer
            // This method doesn't need to do anything in this case
            return;
        }
        // Only set volume on the first projection screen, mute all others
        for (int i = 0; i < projectionScreenHolders.size(); i++) {
            ProjectionScreenHolder projectionScreenHolder = projectionScreenHolders.get(i);
            double screenVolume = (i == 0) ? volume : 0.0;
            projectionScreenHolder.getProjectionScreenController().setVideoVolume(screenVolume);
        }
    }

    public boolean hasProjectionScreens() {
        return !projectionScreenHolders.isEmpty();
    }

    public void startVideoSyncTask() {
        stopVideoSyncTask(); // Stop any existing task first

        if (syncExecutorService == null) {
            syncExecutorService = Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "VideoSyncTask");
                t.setDaemon(true);
                return t;
            });
        }

        syncTask = syncExecutorService.scheduleAtFixedRate(
                this::checkAndSyncVideos,
                0,
                SYNC_CHECK_INTERVAL,
                TimeUnit.MILLISECONDS
        );
    }

    public void stopVideoSyncTask() {
        if (syncTask != null) {
            syncTask.cancel(false);
            syncTask = null;
        }
        if (syncExecutorService != null) {
            syncExecutorService.shutdown();
            try {
                if (!syncExecutorService.awaitTermination(1, TimeUnit.SECONDS)) {
                    syncExecutorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                syncExecutorService.shutdownNow();
                Thread.currentThread().interrupt();
            }
            syncExecutorService = null;
        }
    }

    private void checkAndSyncVideos() {
        if (projectionScreenHolders.isEmpty() || fileImagePath == null) {
            return;
        }

        // Only sync if we're playing a video file
        if (!projector.controller.GalleryController.isMediaFile(fileImagePath)) {
            return;
        }

        // Find the master screen (first screen with non-zero volume, or first screen if all muted)
        ProjectionScreenController masterScreen = null;
        javafx.util.Duration masterTime = null;

        for (ProjectionScreenHolder holder : projectionScreenHolders) {
            ProjectionScreenController controller = holder.getProjectionScreenController();
            javafx.util.Duration currentTime = controller.getVideoCurrentTime();
            MediaPlayer.Status status = controller.getVideoStatus();

            // Only consider playing screens
            if (currentTime != null && status == MediaPlayer.Status.PLAYING) {
                double volume = controller.getVideoVolume();
                if (masterScreen == null) {
                    // First playing screen becomes master
                    masterScreen = controller;
                    masterTime = currentTime;
                } else if (volume > 0.0 && masterScreen.getVideoVolume() == 0.0) {
                    // If we find a screen with sound and current master is muted, switch master
                    masterScreen = controller;
                    masterTime = currentTime;
                }
            }
        }

        // If no master found or master is not playing, don't sync
        if (masterScreen == null) {
            return;
        }

        // Sync all other screens to master
        final javafx.util.Duration finalMasterTime = masterTime;
        final ProjectionScreenController finalMasterScreen = masterScreen;

        for (ProjectionScreenHolder holder : projectionScreenHolders) {
            ProjectionScreenController controller = holder.getProjectionScreenController();

            // Skip the master screen
            if (controller == finalMasterScreen) {
                continue;
            }

            javafx.util.Duration currentTime = controller.getVideoCurrentTime();
            MediaPlayer.Status status = controller.getVideoStatus();

            // Only sync screens that are playing
            if (currentTime != null && status == MediaPlayer.Status.PLAYING) {
                double timeDiff = Math.abs(currentTime.toSeconds() - finalMasterTime.toSeconds());

                // If out of sync by more than threshold, resynchronize
                if (timeDiff > SYNC_THRESHOLD) {
                    //noinspection CodeBlock2Expr
                    Platform.runLater(() -> {
                        controller.seekVideoPlayer(finalMasterTime);
                    });
                }
            }
        }
    }

    // Getters for application context state
    public String getText() {
        return text;
    }

    public ProjectionType getProjectionType() {
        return projectionType;
    }

    public ProjectionData getProjectionData() {
        return projectionData;
    }

    public String getFileImagePath() {
        return fileImagePath;
    }

    public Image getLastImage() {
        return lastImage;
    }
}
