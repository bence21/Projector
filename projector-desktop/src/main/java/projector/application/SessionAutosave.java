package projector.application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class SessionAutosave {

    private static final Logger LOG = LoggerFactory.getLogger(SessionAutosave.class);
    private static final long DEBOUNCE_MS = 750;

    private static SessionAutosave instance;
    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "session-autosave");
        t.setDaemon(true);
        return t;
    });
    private volatile ScheduledFuture<?> pendingSave;
    private volatile boolean restorePending;
    private final AtomicBoolean restoring = new AtomicBoolean(false);

    private SessionAutosave() {
    }

    public static SessionAutosave getInstance() {
        if (instance == null) {
            instance = new SessionAutosave();
        }
        return instance;
    }

    public void setRestorePending(boolean restorePending) {
        this.restorePending = restorePending;
    }

    public boolean isRestorePending() {
        return restorePending;
    }

    public boolean isRestoring() {
        return restoring.get();
    }

    public void runWhileRestoring(Runnable action) {
        restoring.set(true);
        try {
            action.run();
        } finally {
            restoring.set(false);
        }
    }

    /**
     * Debounced autosave after UI changes; no-op while session restore is in progress.
     */
    public void notifySessionChanged() {
        requestSave();
    }

    public void requestSave() {
        if (restoring.get()) {
            return;
        }
        synchronized (this) {
            if (pendingSave != null) {
                pendingSave.cancel(false);
            }
            pendingSave = executor.schedule(this::saveNow, DEBOUNCE_MS, TimeUnit.MILLISECONDS);
        }
    }

    public void saveNow() {
        synchronized (this) {
            if (pendingSave != null) {
                pendingSave.cancel(false);
                pendingSave = null;
            }
        }
        try {
            ApplicationUtil.getInstance().saveProjectorState();
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    public void shutdown() {
        saveNow();
        executor.shutdown();
    }
}
