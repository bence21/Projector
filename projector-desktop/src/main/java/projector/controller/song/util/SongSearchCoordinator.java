package projector.controller.song.util;

import projector.model.Song;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static projector.utils.StringUtils.stripAccents;

public class SongSearchCoordinator {

    public static final int CHUNK_MATCH_INTERVAL = 50;
    public static final long CHUNK_TIME_MS = 80;

    private volatile Thread activeSearchThread;
    private volatile int searchGeneration;

    private LastSearching lastFinishedMode;
    private String lastFinishedNormalizedQuery = "";
    private List<Song> lastFinishedMatches = Collections.emptyList();
    private int lastSearchContextKey;

    private final AtomicReference<List<?>> livePartialResults = new AtomicReference<>();

    public synchronized int beginSearch() {
        Thread previous = activeSearchThread;
        if (previous != null) {
            previous.interrupt();
        }
        return ++searchGeneration;
    }

    public void setActiveThread(Thread thread) {
        this.activeSearchThread = thread;
    }

    public void updateLivePartial(List<?> partial) {
        if (partial == null || partial.isEmpty()) {
            return;
        }
        livePartialResults.set(new ArrayList<>(partial));
    }

    @SuppressWarnings("unchecked")
    public <T> List<T> takeLivePartial() {
        List<?> partial = livePartialResults.getAndSet(null);
        if (partial == null) {
            return null;
        }
        return (List<T>) partial;
    }

    public int getSearchGeneration() {
        return searchGeneration;
    }

    public boolean isContinuation(LastSearching mode, String newRaw, int contextKey) {
        if (mode != lastFinishedMode || contextKey != lastSearchContextKey) {
            return false;
        }
        if (lastFinishedMatches.isEmpty() || lastFinishedNormalizedQuery.isEmpty()) {
            return false;
        }
        String newN = normalizeQuery(newRaw);
        return newN.startsWith(lastFinishedNormalizedQuery) && newN.length() > lastFinishedNormalizedQuery.length();
    }

    public static String normalizeQuery(String raw) {
        if (raw == null) {
            return "";
        }
        return stripAccents(raw.trim()).toLowerCase();
    }

    public void storeFinishedRun(LastSearching mode, String normalizedQuery, List<Song> matches, int contextKey) {
        this.lastFinishedMode = mode;
        this.lastFinishedNormalizedQuery = normalizedQuery;
        this.lastFinishedMatches = matches == null ? Collections.emptyList() : new ArrayList<>(matches);
        this.lastSearchContextKey = contextKey;
    }

    public void invalidateContinuation() {
        lastFinishedNormalizedQuery = "";
        lastFinishedMatches = Collections.emptyList();
        lastSearchContextKey = 0;
        lastFinishedMode = null;
        livePartialResults.set(null);
    }

    public List<Song> getLastFinishedMatches() {
        return lastFinishedMatches;
    }

    public boolean shouldEmitChunk(int matchesSinceLastEmit, long lastEmitTimeMs) {
        return matchesSinceLastEmit >= CHUNK_MATCH_INTERVAL
                || System.currentTimeMillis() - lastEmitTimeMs >= CHUNK_TIME_MS;
    }
}
