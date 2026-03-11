package projector.controller.video;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Factory for creating video player instances.
 * Attempts to create VLC player first, falls back to JavaFX MediaPlayer if VLC is unavailable.
 */
public class VideoPlayerFactory {

    private static final Logger LOG = LoggerFactory.getLogger(VideoPlayerFactory.class);
    private static Boolean vlcAvailable = null; // Cache the availability check

    /**
     * Create a video player instance.
     * Tries VLC first, falls back to JavaFX MediaPlayer if VLC is unavailable.
     *
     * @return A VideoPlayerInterface instance
     */
    public static VideoPlayerInterface createPlayer() {
        // Try VLC first
        if (isVLCAvailable()) {
            try {
                VLCVideoPlayer vlcPlayer = new VLCVideoPlayer();
                if (vlcPlayer.isAvailable()) {
                    return vlcPlayer;
                }
            } catch (Exception e) {
                LOG.warn("Failed to create VLC player, falling back to JavaFX MediaPlayer", e);
            }
        }

        // Fallback to JavaFX MediaPlayer
        return new JavaFXVideoPlayer();
    }

    /**
     * Check if VLC is available on the system.
     *
     * @return true if VLC native libraries are available
     */
    public static boolean isVLCAvailable() {
        if (vlcAvailable != null) {
            return vlcAvailable;
        }

        try {
            // Try to create a VLC factory to test availability
            // Use recommended arguments to suppress verbose warnings and reset plugin cache
            uk.co.caprica.vlcj.factory.MediaPlayerFactory testFactory =
                    new uk.co.caprica.vlcj.factory.MediaPlayerFactory(
                            "--quiet",
                            "--intf=dummy",
                            "--reset-plugins-cache"
                    );
            testFactory.release();
            vlcAvailable = true;
        } catch (UnsatisfiedLinkError e) {
            vlcAvailable = false;
            LOG.warn("VLC native libraries not found. VLC must be installed separately. Falling back to JavaFX MediaPlayer. Error: {}", e.getMessage());
        } catch (NoClassDefFoundError e) {
            vlcAvailable = false;
            LOG.warn("VLCJ classes not found. Check classpath. Falling back to JavaFX MediaPlayer. Error: {}", e.getMessage());
        } catch (Exception e) {
            vlcAvailable = false;
            LOG.warn("VLC initialization failed. Falling back to JavaFX MediaPlayer. Error: {}", e.getMessage(), e);
        }

        return vlcAvailable;
    }

}

