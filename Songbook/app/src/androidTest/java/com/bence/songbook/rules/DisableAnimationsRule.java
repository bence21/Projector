package com.bence.songbook.rules;

import android.os.ParcelFileDescriptor;

import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import java.io.IOException;

public class DisableAnimationsRule implements TestRule {

    private static final String ANIMATOR_DURATION_SCALE = "animator_duration_scale";
    private static final String TRANSITION_ANIMATION_SCALE = "transition_animation_scale";
    private static final String WINDOW_ANIMATION_SCALE = "window_animation_scale";

    @Override
    public Statement apply(Statement base, Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                setAnimationScale(0);
                try {
                    base.evaluate();
                } finally {
                    setAnimationScale(1);
                }
            }
        };
    }

    private static void setAnimationScale(int scale) {
        String value = String.valueOf(scale);
        executeShellCommand("settings put global " + ANIMATOR_DURATION_SCALE + " " + value);
        executeShellCommand("settings put global " + TRANSITION_ANIMATION_SCALE + " " + value);
        executeShellCommand("settings put global " + WINDOW_ANIMATION_SCALE + " " + value);
    }

    @SuppressWarnings("EmptyTryBlock")
    private static void executeShellCommand(String command) {
        try (ParcelFileDescriptor ignored =
                     InstrumentationRegistry.getInstrumentation().getUiAutomation().executeShellCommand(command)) {
            // Shell output is intentionally discarded.
        } catch (IOException e) {
            throw new RuntimeException("Failed to execute shell command: " + command, e);
        }
    }
}
