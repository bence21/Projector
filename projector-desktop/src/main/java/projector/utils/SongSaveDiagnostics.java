package projector.utils;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import projector.application.ApplicationVersion;
import projector.model.Song;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntConsumer;

public final class SongSaveDiagnostics {

    private static final Logger LOG = LoggerFactory.getLogger(SongSaveDiagnostics.class);
    private static final long DEV_POPUP_DEBOUNCE_MS = 300;

    private static final AtomicInteger pendingDevWarnings = new AtomicInteger();
    private static volatile IntConsumer devWarningHandler;
    private static volatile PauseTransition debounceTimer;

    private SongSaveDiagnostics() {
    }

    public static void setDevWarningHandler(IntConsumer handler) {
        devWarningHandler = handler;
    }

    public static void warnIfMissingLanguage(Song song) {
        if (song == null || song.getLanguage() != null) {
            return;
        }
        String message = formatMessage(song);
        LOG.warn(message);
        if (ApplicationVersion.getInstance().isTesting()) {
            throw new AssertionError(message);
        }
        if (DevelopmentMode.isActive()) {
            scheduleDevWarning();
        }
    }

    private static String formatMessage(Song song) {
        return "Song saved without language: uuid=" + song.getUuid()
                + ", title=" + song.getTitle()
                + ", id=" + song.getId();
    }

    private static void scheduleDevWarning() {
        pendingDevWarnings.incrementAndGet();
        Platform.runLater(() -> {
            if (debounceTimer == null) {
                debounceTimer = new PauseTransition(Duration.millis(DEV_POPUP_DEBOUNCE_MS));
                debounceTimer.setOnFinished(event -> fireDevWarning());
            }
            debounceTimer.playFromStart();
        });
    }

    private static void fireDevWarning() {
        int count = pendingDevWarnings.getAndSet(0);
        if (count <= 0) {
            return;
        }
        IntConsumer handler = devWarningHandler;
        if (handler != null) {
            handler.accept(count);
        }
    }
}
