package projector.controller.video;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.image.ImageView;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.caprica.vlcj.factory.MediaPlayerFactory;
import uk.co.caprica.vlcj.javafx.videosurface.ImageViewVideoSurface;
import uk.co.caprica.vlcj.player.base.MediaPlayer;
import uk.co.caprica.vlcj.player.base.MediaPlayerEventAdapter;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;

import java.io.File;
import java.util.concurrent.atomic.AtomicReference;

/**
 * VLC-based video player implementation using VLCJ-JavaFX.
 */
public class VLCVideoPlayer implements VideoPlayerInterface {
    
    private static final Logger LOG = LoggerFactory.getLogger(VLCVideoPlayer.class);
    
    private MediaPlayerFactory mediaPlayerFactory;
    private EmbeddedMediaPlayer mediaPlayer;
    private ImageView imageView;
    private VideoPlayerStatus status = VideoPlayerStatus.UNKNOWN;
    private volatile boolean disposed = false;
    
    private Runnable onReady;
    private Runnable onError;
    private Runnable onEndOfMedia;
    private Runnable onTimeChanged;
    private Runnable onStatusChanged;
    
    private final AtomicReference<Duration> currentTime = new AtomicReference<>();
    private final AtomicReference<Duration> duration = new AtomicReference<>();
    
    public VLCVideoPlayer() {
        try {
            // Initialize VLC factory with error handling
            // Use recommended arguments to suppress verbose warnings and reset plugin cache
            try {
                mediaPlayerFactory = new MediaPlayerFactory(
                    "--quiet",
                    "--intf=dummy",
                    "--reset-plugins-cache"
                );
            } catch (UnsatisfiedLinkError e) {
                LOG.error("VLC native libraries not found. Please ensure VLC is installed.", e);
                status = VideoPlayerStatus.ERROR;
                throw e;
            } catch (NoClassDefFoundError e) {
                LOG.error("VLCJ classes not found. Check classpath.", e);
                status = VideoPlayerStatus.ERROR;
                throw e;
            }
            
            mediaPlayer = mediaPlayerFactory.mediaPlayers().newEmbeddedMediaPlayer();
            
            // Create ImageView for video display
            imageView = new ImageView();
            imageView.setPreserveRatio(true);
            
            // Set up video surface
            ImageViewVideoSurface videoSurface = new ImageViewVideoSurface(imageView);
            mediaPlayer.videoSurface().set(videoSurface);
            
            // Set up event listeners
            mediaPlayer.events().addMediaPlayerEventListener(new MediaPlayerEventAdapter() {
                @Override
                public void playing(MediaPlayer mediaPlayer) {
                    Platform.runLater(() -> {
                        if (disposed) return;
                        status = VideoPlayerStatus.PLAYING;
                        if (onStatusChanged != null) {
                            onStatusChanged.run();
                        }
                    });
                }
                
                @Override
                public void paused(MediaPlayer mediaPlayer) {
                    Platform.runLater(() -> {
                        if (disposed) return;
                        status = VideoPlayerStatus.PAUSED;
                        if (onStatusChanged != null) {
                            onStatusChanged.run();
                        }
                    });
                }
                
                @Override
                public void stopped(MediaPlayer mediaPlayer) {
                    Platform.runLater(() -> {
                        if (disposed) return;
                        status = VideoPlayerStatus.STOPPED;
                        if (onStatusChanged != null) {
                            onStatusChanged.run();
                        }
                    });
                }
                
                @Override
                public void finished(MediaPlayer mediaPlayer) {
                    Platform.runLater(() -> {
                        if (disposed) return;
                        status = VideoPlayerStatus.STOPPED;
                        if (onEndOfMedia != null) {
                            onEndOfMedia.run();
                        }
                        if (onStatusChanged != null) {
                            onStatusChanged.run();
                        }
                    });
                }
                
                @Override
                public void error(MediaPlayer mediaPlayer) {
                    Platform.runLater(() -> {
                        if (disposed) return;
                        status = VideoPlayerStatus.ERROR;
                        LOG.error("VLC media player error");
                        if (onError != null) {
                            onError.run();
                        }
                        if (onStatusChanged != null) {
                            onStatusChanged.run();
                        }
                    });
                }
                
                @Override
                public void lengthChanged(MediaPlayer mediaPlayer, long newLength) {
                    Platform.runLater(() -> {
                        if (disposed) return;
                        duration.set(Duration.millis(newLength));
                    });
                }
                
                @Override
                public void timeChanged(MediaPlayer mediaPlayer, long newTime) {
                    Platform.runLater(() -> {
                        if (disposed) return;
                        currentTime.set(Duration.millis(newTime));
                        if (onTimeChanged != null) {
                            onTimeChanged.run();
                        }
                    });
                }
                
                @Override
                public void opening(MediaPlayer mediaPlayer) {
                    Platform.runLater(() -> {
                        if (disposed) return;
                        status = VideoPlayerStatus.READY;
                        if (onReady != null) {
                            onReady.run();
                        }
                        if (onStatusChanged != null) {
                            onStatusChanged.run();
                        }
                    });
                }
            });
            
            status = VideoPlayerStatus.READY;
        } catch (Exception e) {
            LOG.error("Failed to initialize VLC player", e);
            status = VideoPlayerStatus.ERROR;
        }
    }
    
    @Override
    public void play() {
        if (mediaPlayer != null && status != VideoPlayerStatus.ERROR) {
            try {
                mediaPlayer.controls().play();
            } catch (Exception e) {
                LOG.error("Failed to play video", e);
            }
        }
    }
    
    @Override
    public void pause() {
        if (mediaPlayer != null && status != VideoPlayerStatus.ERROR) {
            try {
                mediaPlayer.controls().pause();
            } catch (Exception e) {
                LOG.error("Failed to pause video", e);
            }
        }
    }
    
    @Override
    public void stop() {
        if (mediaPlayer != null) {
            try {
                mediaPlayer.controls().stop();
            } catch (Exception e) {
                LOG.error("Failed to stop video", e);
            }
        }
    }
    
    @Override
    public void seek(Duration time) {
        if (mediaPlayer != null && time != null && !time.isUnknown()) {
            try {
                long milliseconds = (long) time.toMillis();
                mediaPlayer.controls().setTime(milliseconds);
            } catch (Exception e) {
                LOG.error("Failed to seek video", e);
            }
        }
    }
    
    @Override
    public void setVolume(double volume) {
        if (mediaPlayer != null) {
            try {
                // VLC volume is 0-200, but we use 0.0-1.0
                int vlcVolume = (int) (Math.max(0.0, Math.min(1.0, volume)) * 200);
                mediaPlayer.audio().setVolume(vlcVolume);
            } catch (Exception e) {
                LOG.error("Failed to set volume", e);
            }
        }
    }
    
    @Override
    public Duration getCurrentTime() {
        if (disposed || mediaPlayer == null) {
            return currentTime.get();
        }
        try {
            long time = mediaPlayer.status().time();
            if (time >= 0) {
                return Duration.millis(time);
            }
        } catch (Exception e) {
            LOG.error("Failed to get current time", e);
        }
        return currentTime.get();
    }
    
    @Override
    public Duration getDuration() {
        if (disposed || mediaPlayer == null) {
            return duration.get();
        }
        try {
            long length = mediaPlayer.status().length();
            if (length > 0) {
                return Duration.millis(length);
            }
        } catch (Exception e) {
            LOG.error("Failed to get duration", e);
        }
        return duration.get();
    }
    
    @Override
    public VideoPlayerStatus getStatus() {
        if (disposed) {
            return VideoPlayerStatus.UNKNOWN;
        }
        return status;
    }
    
    @Override
    public Node getVideoNode() {
        return imageView;
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
        if (mediaPlayer == null) {
            LOG.error("Media player not initialized");
            return;
        }
        
        File file = new File(filePath);
        if (!file.exists()) {
            LOG.error("Video file does not exist: {}", filePath);
            if (onError != null) {
                onError.run();
            }
            return;
        }
        
        try {
            stop(); // Stop any currently playing media
            mediaPlayer.media().play(filePath);
            status = VideoPlayerStatus.READY;
        } catch (Exception e) {
            LOG.error("Failed to load video file: {}", filePath, e);
            status = VideoPlayerStatus.ERROR;
            if (onError != null) {
                onError.run();
            }
        }
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
                mediaPlayer.release();
            } catch (Exception e) {
                LOG.error("Error disposing media player", e);
            }
            mediaPlayer = null;
        }
        if (mediaPlayerFactory != null) {
            try {
                mediaPlayerFactory.release();
            } catch (Exception e) {
                LOG.error("Error releasing media player factory", e);
            }
            mediaPlayerFactory = null;
        }
        status = VideoPlayerStatus.UNKNOWN;
    }
    
    @Override
    public boolean isAvailable() {
        return mediaPlayer != null && status != VideoPlayerStatus.ERROR;
    }
}

