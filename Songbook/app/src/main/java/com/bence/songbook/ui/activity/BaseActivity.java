package com.bence.songbook.ui.activity;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

/**
 * Base activity that handles edge-to-edge display for Android 15+.
 * Ensures system UI (status bar, navigation bar) doesn't overlap content.
 * <p>
 * Activities should call setupWindowInsets() after setContentView() if they
 * need custom handling, otherwise it will be set up automatically in onPostCreate().
 */
public class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Enable edge-to-edge display
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Set up window insets handling after content view is set
        setupWindowInsets();
    }

    /**
     * Sets up window insets handling to prevent content from being hidden behind system UI.
     * This is called automatically in onPostCreate(), but can be overridden for custom handling.
     */
    protected void setupWindowInsets() {
        View rootView = findViewById(android.R.id.content);
        if (rootView instanceof ViewGroup) {
            ViewGroup contentView = (ViewGroup) rootView;
            if (contentView.getChildCount() > 0) {
                // Get the activity's root layout (first child of content view)
                View activityRoot = contentView.getChildAt(0);

                // Apply window insets to the activity's root layout
                ViewCompat.setOnApplyWindowInsetsListener(activityRoot, (v, windowInsets) -> {
                    Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());

                    // Apply padding to prevent content from being hidden behind system UI
                    v.setPadding(insets.left, insets.top, insets.right, insets.bottom);

                    return windowInsets;
                });
            }
        }
    }
}

