package projector.controller.video;

import javafx.scene.Node;
import javafx.util.Duration;

/**
 * Interface for video player implementations.
 * Provides abstraction over VLC and JavaFX MediaPlayer.
 */
public interface VideoPlayerInterface {

    /**
     * Start playing the video.
     */
    void play();

    /**
     * Pause the video playback.
     */
    void pause();

    /**
     * Stop the video playback.
     */
    void stop();

    /**
     * Seek to a specific time position.
     *
     * @param time The time to seek to
     */
    void seek(Duration time);

    /**
     * Set the volume level (0.0 to 1.0).
     *
     * @param volume Volume level between 0.0 and 1.0
     */
    void setVolume(double volume);

    /**
     * Get the current playback time.
     *
     * @return Current time, or null if unknown
     */
    Duration getCurrentTime();

    /**
     * Get the total duration of the video.
     *
     * @return Total duration, or null if unknown
     */
    Duration getDuration();

    /**
     * Get the current status of the player.
     *
     * @return Current player status
     */
    VideoPlayerStatus getStatus();

    /**
     * Get the Node used to display the video.
     * For VLC, this will be an ImageView.
     * For JavaFX MediaPlayer, this will be a MediaView.
     *
     * @return Node for video display
     */
    Node getVideoNode();

    /**
     * Set a callback for when the video is ready to play.
     *
     * @param onReady Runnable to execute when ready
     */
    void setOnReady(Runnable onReady);

    /**
     * Set a callback for when an error occurs.
     *
     * @param onError Runnable to execute on error
     */
    void setOnError(Runnable onError);

    /**
     * Set a callback for when the video reaches the end.
     *
     * @param onEndOfMedia Runnable to execute at end of media
     */
    void setOnEndOfMedia(Runnable onEndOfMedia);

    /**
     * Set a callback for when the current time changes.
     *
     * @param listener Runnable to execute on time change
     */
    void setOnTimeChanged(Runnable listener);

    /**
     * Set a callback for when the status changes.
     *
     * @param listener Runnable to execute on status change
     */
    void setOnStatusChanged(Runnable listener);

    /**
     * Load a video file.
     *
     * @param filePath Path to the video file
     */
    void load(String filePath);

    /**
     * Dispose of resources and clean up.
     */
    void dispose();

    /**
     * Check if the player is available/initialized.
     *
     * @return true if player is available
     */
    boolean isAvailable();
}

