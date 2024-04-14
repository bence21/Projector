package com.bence.psbremote.ui.activity;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.Layout;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.bence.psbremote.R;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public abstract class AbstractFullscreenActivity extends AppCompatActivity {

    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler mHideHandler = new Handler();
    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            // Delayed display of UI elements
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.show();
            }
        }
    };
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };
    TextView textView;
    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Delayed removal of status and navigation bar

            // Note that some of these constants are new as of API 16 (Jelly Bean)
            // and API 19 (KitKat). It is safe to use them, as they are inlined
            // at compile-time and do nothing on earlier devices.
            textView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    };
    private int activity = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            if (activity == -1) {
                activity = R.layout.activity_song;
            }
            setContentView(activity);
            textView = findViewById(R.id.fullscreen_content);
            textView.setSingleLine(false);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                textView.setBreakStrategy(Layout.BREAK_STRATEGY_SIMPLE);
            }
            // Set up the user interaction to manually show or hide the system UI.
            // Upon interacting with UI controls, delay any scheduled hide()
            // operations to prevent the jarring behavior of controls going away
            // while interacting with the UI.
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            boolean rotateInFullscreen = sharedPreferences.getBoolean("rotateInFullscreen", true);
            if (!rotateInFullscreen) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER);
            }
            int max_text_size = sharedPreferences.getInt("max_text_size", -1);
            if (max_text_size > 0) {
                textView.setTextSize(max_text_size);
            }
        } catch (Exception e) {
            Log.e(AbstractFullscreenActivity.class.getSimpleName(), e.getMessage());
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide();
    }

    void hide() {
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    /**
     * Schedules a call to hide() in [delay] milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide() {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, 1);
    }

    @Override
    protected void onResume() {
        super.onResume();
        hide();
    }

    public void setText(String text) {
        if (text == null) {
            return;
        }
        String s = text.replaceAll("<color=\"0x(.{0,6})..\">", "<font color='0x$1'>")
                .replaceAll("</color>", "</font>")
                .replaceAll("\\[", "<i>")
                .replaceAll("]", "</i>")
                .replaceAll("\n", "<br>");
        textView.setText(Html.fromHtml(s), TextView.BufferType.SPANNABLE);
    }
}
