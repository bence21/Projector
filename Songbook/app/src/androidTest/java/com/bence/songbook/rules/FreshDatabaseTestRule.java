package com.bence.songbook.rules;

import android.content.Context;
import android.os.Looper;

import androidx.test.platform.app.InstrumentationRegistry;

import com.bence.songbook.SongbookTestActions;
import com.bence.songbook.SongbookTestSetup;
import com.bence.songbook.repository.QueueRepository;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

public class FreshDatabaseTestRule implements TestRule {

    @Override
    public Statement apply(Statement base, Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                runOnMainThreadIfNeeded(() -> {
                    SongbookTestSetup.prepareForFreshInstall();
                    SongbookTestActions.resetMainActivityReadyState();
                    Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
                    QueueRepository.resetForTests();
                    QueueRepository.getInstance(context).clear();
                });
                base.evaluate();
            }
        };
    }

    private static void runOnMainThreadIfNeeded(Runnable runnable) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            runnable.run();
        } else {
            InstrumentationRegistry.getInstrumentation().runOnMainSync(runnable);
        }
    }
}
