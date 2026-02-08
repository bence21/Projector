package projector.controller;

import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import projector.application.ProjectionType;
import projector.controller.util.ProjectionScreensUtil;

import java.io.File;

public class VideoViewerController {

    private static final Logger LOG = LoggerFactory.getLogger(VideoViewerController.class);

    @FXML
    private BorderPane borderPane;
    @FXML
    private StackPane mediaStackPane;
    @FXML
    private MediaView mediaView;
    @FXML
    private HBox controlBar;
    @FXML
    private Button playPauseButton;
    @FXML
    private Label timeLabel;
    @FXML
    private Slider seekSlider;
    @FXML
    private Slider volumeSlider;
    @FXML
    private Label volumeLabel;

    private MediaPlayer mediaPlayer;
    private boolean isPlaying = false;
    private boolean isSeeking = false;
    private final ProjectionScreensUtil projectionScreensUtil = ProjectionScreensUtil.getInstance();

    public void initialize() {
        setupKeyboardNavigation();
        setupControls();
        setupSliderListeners();
    }

    private void setupKeyboardNavigation() {
        if (borderPane == null) {
            return;
        }
        borderPane.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (mediaPlayer == null) {
                return;
            }
            KeyCode keyCode = event.getCode();
            if (keyCode == KeyCode.SPACE) {
                togglePlayPause();
                event.consume();
            } else if (keyCode == KeyCode.LEFT) {
                seekRelative(-5);
                event.consume();
            } else if (keyCode == KeyCode.RIGHT) {
                seekRelative(5);
                event.consume();
            } else if (keyCode == KeyCode.UP) {
                adjustVolume(0.1);
                event.consume();
            } else if (keyCode == KeyCode.DOWN) {
                adjustVolume(-0.1);
                event.consume();
            }
        });
    }

    private void setupControls() {
        // Initialize controls as disabled
        if (playPauseButton != null) {
            playPauseButton.setDisable(true);
        }
        if (seekSlider != null) {
            seekSlider.setDisable(true);
        }
        if (volumeSlider != null) {
            volumeSlider.setDisable(true);
        }
        if (timeLabel != null) {
            timeLabel.setText("00:00 / 00:00");
        }
        if (volumeLabel != null) {
            volumeLabel.setText("100%");
        }
    }

    private void setupSliderListeners() {
        // Set up seek slider value change listener
        if (seekSlider != null) {
            seekSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
                if (isSeeking) {
                    onSeekSliderValueChanged();
                }
            });
        }

        // Set up volume slider value change listener
        if (volumeSlider != null) {
            volumeSlider.valueProperty().addListener((observable, oldValue, newValue) -> onVolumeSliderChanged());
        }
    }

    public void setVideoFile(String filePath) {
        File videoFile = new File(filePath);
        if (!videoFile.exists()) {
            LOG.error("Video file does not exist: {}", filePath);
            return;
        }

        try {
            String videoURI = videoFile.toURI().toString();
            Media media = new Media(videoURI);
            mediaPlayer = new MediaPlayer(media);

            setupMediaPlayer();
            setupMediaView();

            // Handle media player ready state
            mediaPlayer.setOnReady(() -> Platform.runLater(() -> {
                enableControls();
                updateTimeLabel();
            }));

            // Handle errors
            mediaPlayer.setOnError(() -> {
                LOG.error("MediaPlayer error: {}", mediaPlayer.getError());
                Platform.runLater(() -> {
                    if (timeLabel != null) {
                        timeLabel.setText("Error loading video");
                    }
                });
            });

            // Project video to all projection screens
            projectionScreensUtil.setImage(filePath, ProjectionType.IMAGE, null);

            // Set volume: if there are projection screens, mute the viewer and set volume on first screen
            // If there are no projection screens, the viewer (preview) should have sound
            if (mediaPlayer != null && volumeSlider != null) {
                double initialVolume = volumeSlider.getValue();
                if (projectionScreensUtil.hasProjectionScreens()) {
                    // Mute the video viewer - sound should only come from the first projection screen
                    mediaPlayer.setVolume(0.0);
                    // Set initial volume on first projection screen
                    projectionScreensUtil.setVideoVolumeOnAllScreens(initialVolume);
                    // Start periodic synchronization task
                    projectionScreensUtil.startVideoSyncTask();
                } else {
                    // No projection screens - the viewer (preview) should have sound
                    mediaPlayer.setVolume(initialVolume);
                }
            }

        } catch (Exception e) {
            LOG.error("Error loading video file: {}", filePath, e);
        }
    }

    private void setupMediaPlayer() {
        if (mediaPlayer == null) {
            return;
        }

        // Don't bind volume slider to media player volume - we'll control it manually
        // The slider will control projection screen volume when screens exist,
        // or viewer volume when no screens exist
        if (volumeSlider != null) {
            volumeSlider.setValue(1.0); // Default to 100% volume
        }

        // Update time label and seek slider during playback
        mediaPlayer.currentTimeProperty().addListener((Observable observable) -> {
            if (!isSeeking) {
                Platform.runLater(() -> {
                    updateTimeLabel();
                    updateSeekSlider();
                });
            }
        });

        // Update play/pause button based on player state
        mediaPlayer.statusProperty().addListener((observable, oldStatus, newStatus) -> Platform.runLater(() -> {
            if (newStatus == MediaPlayer.Status.PLAYING) {
                isPlaying = true;
                updatePlayPauseButton();
            } else if (newStatus == MediaPlayer.Status.PAUSED || newStatus == MediaPlayer.Status.STOPPED) {
                isPlaying = false;
                updatePlayPauseButton();
            }
        }));

        // Handle end of media
        mediaPlayer.setOnEndOfMedia(() -> Platform.runLater(() -> {
            isPlaying = false;
            updatePlayPauseButton();
        }));
    }

    private void setupMediaView() {
        if (mediaView == null || mediaPlayer == null) {
            return;
        }

        mediaView.setMediaPlayer(mediaPlayer);
        mediaView.setPreserveRatio(true);

        // Make MediaView fit the available space
        if (mediaStackPane != null) {
            mediaView.fitWidthProperty().bind(mediaStackPane.widthProperty());
            mediaView.fitHeightProperty().bind(mediaStackPane.heightProperty());
        }
    }

    private void enableControls() {
        if (playPauseButton != null) {
            playPauseButton.setDisable(false);
        }
        if (seekSlider != null) {
            seekSlider.setDisable(false);
            Duration duration = mediaPlayer.getMedia().getDuration();
            if (duration != null && !duration.isUnknown()) {
                seekSlider.setMax(duration.toSeconds());
            }
        }
        if (volumeSlider != null) {
            volumeSlider.setDisable(false);
        }
    }

    @FXML
    private void togglePlayPause() {
        if (mediaPlayer == null) {
            return;
        }

        if (isPlaying) {
            mediaPlayer.pause();
            isPlaying = false;
            // Synchronize pause with all projection screens
            projectionScreensUtil.pauseVideoOnAllScreens();
        } else {
            mediaPlayer.play();
            isPlaying = true;
            // Synchronize play with all projection screens
            projectionScreensUtil.playVideoOnAllScreens();
        }
        updatePlayPauseButton();
    }

    private void updatePlayPauseButton() {
        if (playPauseButton == null) {
            return;
        }
        // Note: Button text should be set in FXML or via resource bundle
        // For now, we'll use simple text
        playPauseButton.setText(isPlaying ? "Pause" : "Play");
    }

    private void updateTimeLabel() {
        if (timeLabel == null || mediaPlayer == null) {
            return;
        }

        Duration currentTime = mediaPlayer.getCurrentTime();
        Duration duration = mediaPlayer.getMedia().getDuration();

        String currentTimeStr = formatTime(currentTime);
        String durationStr = formatTime(duration);

        timeLabel.setText(currentTimeStr + " / " + durationStr);
    }

    private void updateSeekSlider() {
        if (seekSlider == null || mediaPlayer == null || isSeeking) {
            return;
        }

        Duration currentTime = mediaPlayer.getCurrentTime();
        Duration duration = mediaPlayer.getMedia().getDuration();

        if (currentTime != null && duration != null && !duration.isUnknown()) {
            seekSlider.setValue(currentTime.toSeconds());
        }
    }

    @FXML
    private void onSeekSliderPressed() {
        isSeeking = true;
    }

    @FXML
    private void onSeekSliderReleased() {
        if (mediaPlayer == null || seekSlider == null) {
            isSeeking = false;
            return;
        }

        double seekTime = seekSlider.getValue();
        Duration seekDuration = Duration.seconds(seekTime);
        mediaPlayer.seek(seekDuration);
        // Synchronize seek with all projection screens
        projectionScreensUtil.seekVideoOnAllScreens(seekDuration);
        isSeeking = false;
    }

    @FXML
    private void onSeekSliderValueChanged() {
        if (isSeeking && mediaPlayer != null && seekSlider != null) {
            double seekTime = seekSlider.getValue();
            Duration seekDuration = Duration.seconds(seekTime);
            mediaPlayer.seek(seekDuration);
            // Synchronize seek with all projection screens
            projectionScreensUtil.seekVideoOnAllScreens(seekDuration);
        }
    }

    private void seekRelative(double seconds) {
        if (mediaPlayer == null) {
            return;
        }

        Duration currentTime = mediaPlayer.getCurrentTime();
        Duration duration = mediaPlayer.getMedia().getDuration();

        if (currentTime != null && duration != null && !duration.isUnknown()) {
            double newTime = currentTime.toSeconds() + seconds;
            newTime = Math.max(0, Math.min(newTime, duration.toSeconds()));
            Duration seekDuration = Duration.seconds(newTime);
            mediaPlayer.seek(seekDuration);
            // Synchronize seek with all projection screens
            projectionScreensUtil.seekVideoOnAllScreens(seekDuration);
        }
    }

    @FXML
    private void onVolumeSliderChanged() {
        if (volumeSlider == null || mediaPlayer == null) {
            return;
        }

        double volume = volumeSlider.getValue();
        updateVolumeLabel(volume);

        if (projectionScreensUtil.hasProjectionScreens()) {
            // Synchronize volume with projection screens (first screen gets volume, others muted)
            projectionScreensUtil.setVideoVolumeOnAllScreens(volume);
            // Keep viewer muted when projection screens exist
            mediaPlayer.setVolume(0.0);
        } else {
            // No projection screens - the viewer (preview) should have sound
            mediaPlayer.setVolume(volume);
        }
    }

    private void adjustVolume(double delta) {
        if (volumeSlider == null) {
            return;
        }

        double newVolume = volumeSlider.getValue() + delta;
        newVolume = Math.max(0.0, Math.min(1.0, newVolume));
        volumeSlider.setValue(newVolume);
    }

    private void updateVolumeLabel(double volume) {
        if (volumeLabel == null) {
            return;
        }

        int volumePercent = (int) (volume * 100);
        volumeLabel.setText(volumePercent + "%");
    }

    private String formatTime(Duration duration) {
        if (duration == null || duration.isUnknown()) {
            return "00:00";
        }

        long totalSeconds = (long) duration.toSeconds();
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;

        if (hours > 0) {
            return String.format("%d:%02d:%02d", hours, minutes, seconds);
        } else {
            return String.format("%02d:%02d", minutes, seconds);
        }
    }

    public void cleanup() {
        // Stop synchronization task
        projectionScreensUtil.stopVideoSyncTask();

        if (mediaPlayer != null) {
            try {
                mediaPlayer.stop();
                mediaPlayer.dispose();
                mediaPlayer = null;
            } catch (Exception e) {
                LOG.error("Error cleaning up MediaPlayer", e);
            }
        }
    }
}

