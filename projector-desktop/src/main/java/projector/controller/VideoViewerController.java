package projector.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.media.MediaView;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import projector.application.ProjectionType;
import projector.controller.util.ProjectionScreensUtil;
import projector.controller.video.VideoPlayerFactory;
import projector.controller.video.VideoPlayerInterface;
import projector.controller.video.VideoPlayerStatus;

import java.io.File;

public class VideoViewerController {

    private static final Logger LOG = LoggerFactory.getLogger(VideoViewerController.class);

    @FXML
    private BorderPane borderPane;
    @FXML
    private StackPane mediaStackPane;
    @FXML
    private MediaView mediaView; // Keep for FXML compatibility, will be replaced by videoNode
    private Node videoNode; // The actual video display node
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

    private VideoPlayerInterface videoPlayer;
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
            if (videoPlayer == null) {
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
            // Stop and dispose existing player
            cleanup();

            // Create new video player
            videoPlayer = VideoPlayerFactory.createPlayer();

            // Get the video node and add it to the stack pane
            videoNode = videoPlayer.getVideoNode();
            if (videoNode != null && !mediaStackPane.getChildren().contains(videoNode)) {
                // Remove old MediaView if present
                if (mediaView != null) {
                    mediaStackPane.getChildren().remove(mediaView);
                }
                mediaStackPane.getChildren().add(videoNode);
            }

            setupVideoPlayer();

            // Handle video player ready state
            videoPlayer.setOnReady(() -> Platform.runLater(() -> {
                enableControls();
                if (videoPlayer != null) {
                    updateTimeLabel(videoPlayer);
                }
            }));

            // Handle errors
            videoPlayer.setOnError(() -> {
                LOG.error("Video player error");
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
            if (videoPlayer != null && volumeSlider != null) {
                double initialVolume = volumeSlider.getValue();
                if (projectionScreensUtil.hasProjectionScreens()) {
                    // Mute the video viewer - sound should only come from the first projection screen
                    videoPlayer.setVolume(0.0);
                    // Set initial volume on first projection screen
                    projectionScreensUtil.setVideoVolumeOnAllScreens(initialVolume);
                    // Start periodic synchronization task
                    projectionScreensUtil.startVideoSyncTask();
                } else {
                    // No projection screens - the viewer (preview) should have sound
                    videoPlayer.setVolume(initialVolume);
                }
            }

            // Load the video file
            if (videoPlayer != null) {
                videoPlayer.load(filePath);
            }

        } catch (Exception e) {
            LOG.error("Error loading video file: {}", filePath, e);
        }
    }

    private void setupVideoPlayer() {
        if (videoPlayer == null) {
            return;
        }

        // Capture the player reference when setting up callbacks to avoid null pointer issues
        final VideoPlayerInterface playerRef = videoPlayer;

        // Don't bind volume slider to video player volume - we'll control it manually
        // The slider will control projection screen volume when screens exist,
        // or viewer volume when no screens exist
        if (volumeSlider != null) {
            volumeSlider.setValue(1.0); // Default to 100% volume
        }

        // Update time label and seek slider during playback
        videoPlayer.setOnTimeChanged(() -> {
            if (!isSeeking) {
                Platform.runLater(() -> {
                    // Check if player still exists before updating
                    updateTimeLabel(playerRef);
                    updateSeekSlider(playerRef);
                });
            }
        });

        // Update play/pause button based on player state
        videoPlayer.setOnStatusChanged(() -> Platform.runLater(() -> {
            // Use captured reference to avoid null pointer if cleanup happens
            VideoPlayerStatus status = playerRef.getStatus();
            if (status == VideoPlayerStatus.PLAYING) {
                isPlaying = true;
                updatePlayPauseButton();
            } else if (status == VideoPlayerStatus.PAUSED || status == VideoPlayerStatus.STOPPED) {
                isPlaying = false;
                updatePlayPauseButton();
            }
        }));

        // Handle end of media
        videoPlayer.setOnEndOfMedia(() -> Platform.runLater(() -> {
            isPlaying = false;
            updatePlayPauseButton();
        }));

        // Setup video node sizing
        if (videoNode != null && mediaStackPane != null) {
            if (videoNode instanceof MediaView mv) {
                mv.setPreserveRatio(true);
                mv.fitWidthProperty().bind(mediaStackPane.widthProperty());
                mv.fitHeightProperty().bind(mediaStackPane.heightProperty());
            } else if (videoNode instanceof javafx.scene.image.ImageView iv) {
                iv.setPreserveRatio(true);
                iv.fitWidthProperty().bind(mediaStackPane.widthProperty());
                iv.fitHeightProperty().bind(mediaStackPane.heightProperty());
            }
        }
    }

    private void enableControls() {
        if (playPauseButton != null) {
            playPauseButton.setDisable(false);
        }
        if (seekSlider != null) {
            seekSlider.setDisable(false);
            Duration duration = videoPlayer != null ? videoPlayer.getDuration() : null;
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
        if (videoPlayer == null) {
            return;
        }

        if (isPlaying) {
            videoPlayer.pause();
            isPlaying = false;
            // Synchronize pause with all projection screens
            projectionScreensUtil.pauseVideoOnAllScreens();
        } else {
            videoPlayer.play();
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

    private void updateTimeLabel(VideoPlayerInterface player) {
        if (timeLabel == null || player == null) {
            return;
        }

        Duration currentTime = player.getCurrentTime();
        Duration duration = player.getDuration();

        String currentTimeStr = formatTime(currentTime);
        String durationStr = formatTime(duration);

        timeLabel.setText(currentTimeStr + " / " + durationStr);
    }

    private void updateSeekSlider(VideoPlayerInterface player) {
        if (seekSlider == null || player == null || isSeeking) {
            return;
        }

        Duration currentTime = player.getCurrentTime();
        Duration duration = player.getDuration();

        if (currentTime != null && duration != null && !duration.isUnknown()) {
            // Update the slider max to match the video duration
            // This ensures the slider range is correct even if duration wasn't available initially
            double durationSeconds = duration.toSeconds();
            if (durationSeconds > 0) {
                seekSlider.setMax(durationSeconds);
                seekSlider.setMin(0.0);
                // Set the current value
                double currentSeconds = currentTime.toSeconds();
                // Clamp the value to ensure it's within bounds
                currentSeconds = Math.max(0.0, Math.min(currentSeconds, durationSeconds));
                seekSlider.setValue(currentSeconds);
            }
        }
    }

    @FXML
    private void onSeekSliderPressed() {
        isSeeking = true;
    }

    @FXML
    private void onSeekSliderReleased() {
        if (videoPlayer == null || seekSlider == null) {
            isSeeking = false;
            return;
        }

        double seekTime = seekSlider.getValue();
        Duration seekDuration = Duration.seconds(seekTime);
        videoPlayer.seek(seekDuration);
        // Synchronize seek with all projection screens
        projectionScreensUtil.seekVideoOnAllScreens(seekDuration);
        isSeeking = false;
    }

    @FXML
    private void onSeekSliderValueChanged() {
        if (isSeeking && videoPlayer != null && seekSlider != null) {
            double seekTime = seekSlider.getValue();
            Duration seekDuration = Duration.seconds(seekTime);
            videoPlayer.seek(seekDuration);
            // Synchronize seek with all projection screens
            projectionScreensUtil.seekVideoOnAllScreens(seekDuration);
        }
    }

    private void seekRelative(double seconds) {
        if (videoPlayer == null) {
            return;
        }

        Duration currentTime = videoPlayer.getCurrentTime();
        Duration duration = videoPlayer.getDuration();

        if (currentTime != null && duration != null && !duration.isUnknown()) {
            double newTime = currentTime.toSeconds() + seconds;
            newTime = Math.max(0, Math.min(newTime, duration.toSeconds()));
            Duration seekDuration = Duration.seconds(newTime);
            videoPlayer.seek(seekDuration);
            // Synchronize seek with all projection screens
            projectionScreensUtil.seekVideoOnAllScreens(seekDuration);
        }
    }

    @FXML
    private void onVolumeSliderChanged() {
        if (volumeSlider == null || videoPlayer == null) {
            return;
        }

        double volume = volumeSlider.getValue();
        updateVolumeLabel(volume);

        if (projectionScreensUtil.hasProjectionScreens()) {
            // Synchronize volume with projection screens (first screen gets volume, others muted)
            projectionScreensUtil.setVideoVolumeOnAllScreens(volume);
            // Keep viewer muted when projection screens exist
            videoPlayer.setVolume(0.0);
        } else {
            // No projection screens - the viewer (preview) should have sound
            videoPlayer.setVolume(volume);
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

        if (videoPlayer != null) {
            try {
                // Clear callbacks first to prevent race conditions
                videoPlayer.setOnReady(null);
                videoPlayer.setOnError(null);
                videoPlayer.setOnEndOfMedia(null);
                videoPlayer.setOnTimeChanged(null);
                videoPlayer.setOnStatusChanged(null);

                // Then stop and dispose
                videoPlayer.stop();
                videoPlayer.dispose();
                videoPlayer = null;
            } catch (Exception e) {
                LOG.error("Error cleaning up video player", e);
            }
        }

        if (videoNode != null) {
            mediaStackPane.getChildren().remove(videoNode);
        }
        videoNode = null;
    }
}


