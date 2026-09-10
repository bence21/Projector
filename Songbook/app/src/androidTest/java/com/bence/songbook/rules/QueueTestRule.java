package com.bence.songbook.rules;

import android.content.Context;
import android.os.Looper;

import androidx.test.platform.app.InstrumentationRegistry;

import com.bence.songbook.SongbookTestSetup;
import com.bence.songbook.models.QueueSong;
import com.bence.songbook.repository.QueueRepository;
import com.bence.songbook.repository.impl.ormLite.QueueSongRepositoryImpl;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import java.util.List;

public class QueueTestRule implements TestRule {

    @Override
    public Statement apply(Statement base, Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                runOnMainThreadIfNeeded(QueueTestRule::resetQueueState);
                base.evaluate();
            }
        };
    }

    private static void resetQueueState() {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        QueueSongRepositoryImpl queueSongRepository = new QueueSongRepositoryImpl(context);
        List<QueueSong> persistedQueue = queueSongRepository.findAll();
        if (!persistedQueue.isEmpty()) {
            queueSongRepository.deleteAll(persistedQueue);
        }
        QueueRepository.getInstance(context).clear();
        SongbookTestSetup.preloadMemoryFromDatabase();
    }

    private static void runOnMainThreadIfNeeded(Runnable runnable) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            runnable.run();
        } else {
            InstrumentationRegistry.getInstrumentation().runOnMainSync(runnable);
        }
    }
}
