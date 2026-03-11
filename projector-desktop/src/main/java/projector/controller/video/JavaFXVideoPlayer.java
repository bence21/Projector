package projector.controller.video;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * JavaFX MediaPlayer wrapper implementing VideoPlayerInterface.
 * Used as a fallback when VLC is not available.
 */
public class JavaFXVideoPlayer implements VideoPlayerInterface {

    private static final Logger LOG = LoggerFactory.getLogger(JavaFXVideoPlayer.class);

    private MediaPlayer mediaPlayer;
    private Media currentMedia;
    private final MediaView mediaView;
    private VideoPlayerStatus status;
    private volatile boolean disposed = false;

    private Runnable onReady;
    private Runnable onError;
    private Runnable onEndOfMedia;
    private Runnable onTimeChanged;
    private Runnable onStatusChanged;

    public JavaFXVideoPlayer() {
        mediaView = new MediaView();
        mediaView.setPreserveRatio(true);
        status = VideoPlayerStatus.READY;
    }

    @Override
    public void play() {
        if (mediaPlayer != null && status != VideoPlayerStatus.ERROR) {
            try {
                mediaPlayer.play();
            } catch (Exception e) {
                LOG.error("Failed to play video", e);
            }
        }
    }

    @Override
    public void pause() {
        if (mediaPlayer != null && status != VideoPlayerStatus.ERROR) {
            try {
                mediaPlayer.pause();
            } catch (Exception e) {
                LOG.error("Failed to pause video", e);
            }
        }
    }

    @Override
    public void stop() {
        if (mediaPlayer != null) {
            try {
                mediaPlayer.stop();
            } catch (Exception e) {
                LOG.error("Failed to stop video", e);
            }
        }
    }

    @Override
    public void seek(Duration time) {
        if (mediaPlayer != null && time != null && !time.isUnknown()) {
            try {
                mediaPlayer.seek(time);
            } catch (Exception e) {
                LOG.error("Failed to seek video", e);
            }
        }
    }

    @Override
    public void setVolume(double volume) {
        if (mediaPlayer != null) {
            try {
                mediaPlayer.setVolume(Math.max(0.0, Math.min(1.0, volume)));
            } catch (Exception e) {
                LOG.error("Failed to set volume", e);
            }
        }
    }

    @Override
    public Duration getCurrentTime() {
        if (mediaPlayer != null) {
            try {
                Duration time = mediaPlayer.getCurrentTime();
                if (time != null && !time.isUnknown()) {
                    return time;
                }
            } catch (Exception e) {
                LOG.error("Failed to get current time", e);
            }
        }
        return null;
    }

    @Override
    public Duration getDuration() {
        if (mediaPlayer != null) {
            try {
                // Use MediaPlayer's totalDurationProperty which is more reliable
                // than Media.getDuration() and updates correctly during playback
                Duration duration = mediaPlayer.totalDurationProperty().getValue();
                if (duration != null && !duration.isUnknown()) {
                    return duration;
                }
                Duration fallback = getDurationFromCurrentMedia();
                if (fallback != null) {
                    return fallback;
                }
            } catch (Exception e) {
                LOG.error("Failed to get duration", e);
            }
        }
        return null;
    }

    private Duration getDurationFromCurrentMedia() {
        if (currentMedia != null) {
            Duration duration = currentMedia.getDuration();
            if (duration != null && !duration.isUnknown()) {
                return duration;
            }
        }
        return null;
    }

    @Override
    public VideoPlayerStatus getStatus() {
        if (disposed) {
            return VideoPlayerStatus.UNKNOWN;
        }
        if (mediaPlayer == null) {
            return status;
        }

        try {
            MediaPlayer.Status mpStatus = mediaPlayer.getStatus();
            if (mpStatus == null) {
                return VideoPlayerStatus.UNKNOWN;
            }

            return switch (mpStatus) {
                case READY -> VideoPlayerStatus.READY;
                case PLAYING -> VideoPlayerStatus.PLAYING;
                case PAUSED -> VideoPlayerStatus.PAUSED;
                case STOPPED -> VideoPlayerStatus.STOPPED;
                case HALTED, STALLED -> VideoPlayerStatus.ERROR;
                default -> VideoPlayerStatus.UNKNOWN;
            };
        } catch (Exception e) {
            LOG.error("Failed to get status", e);
            return VideoPlayerStatus.UNKNOWN;
        }
    }

    @Override
    public Node getVideoNode() {
        return mediaView;
    }

    @Override
    public void setOnReady(Runnable onReady) {
        this.onReady = onReady;
    }

    @Override
    public void setOnError(Runnable onError) {
        this.onError = onError;
    }

    @Override
    public void setOnEndOfMedia(Runnable onEndOfMedia) {
        this.onEndOfMedia = onEndOfMedia;
    }

    @Override
    public void setOnTimeChanged(Runnable listener) {
        this.onTimeChanged = listener;
    }

    @Override
    public void setOnStatusChanged(Runnable listener) {
        this.onStatusChanged = listener;
    }

    @Override
    public void load(String filePath) {
        File file = new File(filePath);
        if (!file.exists()) {
            LOG.error("Video file does not exist: {}", filePath);
            notifyLoadError();
            return;
        }

        try {
            disposeExistingPlayer();
            createMediaAndPlayer(file);
            attachMediaPlayerListeners();
            status = VideoPlayerStatus.READY;
        } catch (Exception e) {
            LOG.error("Failed to load video file: {}", filePath, e);
            notifyLoadError();
        }
    }

    private void notifyLoadError() {
        status = VideoPlayerStatus.ERROR;
        if (onError != null) {
            Platform.runLater(() -> {
                if (onError != null) {
                    onError.run();
                }
            });
        }
    }

    private void disposeExistingPlayer() {
        if (mediaPlayer != null) {
            stop();
            mediaPlayer.dispose();
        }
    }

    private void createMediaAndPlayer(File file) {
        String videoURI = file.toURI().toString();
        currentMedia = new Media(videoURI);
        mediaPlayer = new MediaPlayer(currentMedia);
        mediaView.setMediaPlayer(mediaPlayer);
        mediaView.setPreserveRatio(true);
    }

    private void attachMediaPlayerListeners() {
        mediaPlayer.setOnReady(this::handleOnReady);
        mediaPlayer.setOnError(this::handleOnError);
        mediaPlayer.setOnEndOfMedia(this::handleOnEndOfMedia);
        mediaPlayer.statusProperty().addListener(this::handleStatusChange);
        mediaPlayer.currentTimeProperty().addListener(this::handleTimeChange);
    }

    private void handleOnReady() {
        if (disposed) return;
        status = VideoPlayerStatus.READY;
        runOnFxIfPresent(onReady);
        runOnFxIfPresent(onStatusChanged);
    }

    private void handleOnError() {
        if (disposed) return;
        status = VideoPlayerStatus.ERROR;
        LOG.error("MediaPlayer error: {}", (Object) mediaPlayer.getError());
        runOnFxIfPresent(onError);
        runOnFxIfPresent(onStatusChanged);
    }

    private void handleOnEndOfMedia() {
        if (disposed) return;
        status = VideoPlayerStatus.STOPPED;
        runOnFxIfPresent(onEndOfMedia);
        runOnFxIfPresent(onStatusChanged);
    }

    private void handleStatusChange(javafx.beans.Observable observable, MediaPlayer.Status oldStatus, MediaPlayer.Status newStatus) {
        if (disposed) return;
        if (newStatus != null) {
            VideoPlayerStatus mapped = mapToVideoPlayerStatus(newStatus);
            if (mapped != null) {
                status = mapped;
            }
        }
        runOnFxIfPresent(onStatusChanged);
    }

    private void handleTimeChange(javafx.beans.value.ObservableValue<? extends Duration> observable, Duration oldValue, Duration newValue) {
        if (disposed) return;
        runOnFxIfPresent(onTimeChanged);
    }

    private void runOnFxIfPresent(Runnable runnable) {
        if (runnable != null) {
            Platform.runLater(runnable);
        }
    }

    private static VideoPlayerStatus mapToVideoPlayerStatus(MediaPlayer.Status fxStatus) {
        return switch (fxStatus) {
            case PLAYING -> VideoPlayerStatus.PLAYING;
            case PAUSED -> VideoPlayerStatus.PAUSED;
            case STOPPED -> VideoPlayerStatus.STOPPED;
            case READY -> VideoPlayerStatus.READY;
            default -> null;
        };
    }

    @Override
    public void dispose() {
        // Mark as disposed first to prevent any new callbacks from executing
        disposed = true;

        // Clear all callbacks to prevent race conditions
        onReady = null;
        onError = null;
        onEndOfMedia = null;
        onTimeChanged = null;
        onStatusChanged = null;

        if (mediaPlayer != null) {
            try {
                stop();
                mediaPlayer.dispose();
            } catch (Exception e) {
                LOG.error("Error disposing media player", e);
            }
            mediaPlayer = null;
        }
        currentMedia = null;
        status = VideoPlayerStatus.UNKNOWN;
    }

    @Override
    public boolean isAvailable() {
        return true; // JavaFX MediaPlayer is always available
    }

}

