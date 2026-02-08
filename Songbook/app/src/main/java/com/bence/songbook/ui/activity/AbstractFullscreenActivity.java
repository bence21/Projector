package com.bence.songbook.ui.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.text.Html;
import android.text.Layout;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;

import com.bence.songbook.R;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public abstract class AbstractFullscreenActivity extends BaseActivity {

    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler mHideHandler = new Handler();
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
    private View mControlsView;
    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            // Delayed display of UI elements
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.show();
            }
            mControlsView.setVisibility(View.VISIBLE);
        }
    };
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };
    private ScaleGestureDetector scaleGestureDetector;
    private float textSize;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_fullscreen);
            mControlsView = findViewById(R.id.fullscreen_content_controls);
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
            boolean light_theme = sharedPreferences.getBoolean("light_theme_switch", false);
            FrameLayout frameLayout = findViewById(R.id.layout);
            if (light_theme) {
                frameLayout.setBackgroundResource(R.color.white);
                textView.setBackgroundResource(R.color.white);
                textView.setTextColor(getResources().getColor(R.color.black));
            } else {
                frameLayout.setBackgroundResource(R.color.black);
                textView.setBackgroundResource(R.color.black);
                textView.setTextColor(getResources().getColor(R.color.white));
            }
        } catch (Exception e) {
            Log.e(AbstractFullscreenActivity.class.getSimpleName(), e.getMessage());
        }
    }

    @Override
    protected void setupWindowInsets() {
        // Fullscreen activities intentionally hide system UI, so skip window insets handling
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
        mControlsView.setVisibility(View.GONE);

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

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (scaleGestureDetector != null) {
            scaleGestureDetector.onTouchEvent(event);
        }
        return true;
    }

    void setText(String text) {
        String s = text.replaceAll("<color=\"0x(.{0,6})..\">", "<font color='0x$1'>")
                .replaceAll("</color>", "</font>")
                .replaceAll("\\[", "<i>")
                .replaceAll("]", "</i>")
                .replaceAll("\n", "<br>");
        textView.setText(Html.fromHtml(s), TextView.BufferType.SPANNABLE);
        textSize = textView.getTextSize();
    }

    protected void setContext(Context context) {
        scaleGestureDetector = new ScaleGestureDetector(context, new ScaleListener());
    }

    class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            System.out.println(textView.getTextSize());
            return super.onScaleBegin(detector);
        }

        @Override
        public boolean onScale(ScaleGestureDetector scaleGestureDetector) {
            float size = textView.getTextSize();
            if (Math.abs(textSize - size) > 24) {
                textSize = size;
            }
            float scaleFactor = scaleGestureDetector.getScaleFactor();
            float newSize = textSize * scaleFactor;
            if (newSize >= 12 && newSize <= 200) {
                textSize = newSize;
                textView.setTextSize(textSize);
            }
            return true;
        }
    }
}
