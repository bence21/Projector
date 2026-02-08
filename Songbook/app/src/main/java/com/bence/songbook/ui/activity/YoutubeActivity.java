package com.bence.songbook.ui.activity;

import static android.graphics.text.LineBreaker.BREAK_STRATEGY_SIMPLE;
import static com.bence.songbook.Memory.onTextForListeners;
import static com.bence.songbook.ui.activity.FullscreenActivity.getTextForTitleSlide;
import static com.bence.songbook.ui.utils.YouTubeIFrame.setYouTubeIFrameToWebView;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.text.Html;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.webkit.WebView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bence.songbook.Memory;
import com.bence.songbook.R;
import com.bence.songbook.models.Song;
import com.bence.songbook.models.SongVerse;
import com.bence.songbook.repository.impl.ormLite.SongRepositoryImpl;
import com.bence.songbook.ui.utils.OnSwipeTouchListener;
import com.bence.songbook.ui.utils.Preferences;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class YoutubeActivity extends BaseActivity {

    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler mHideHandler = new Handler();
    private final Runnable mShowPart2Runnable = () -> {
        // Delayed display of UI elements
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
    private final Runnable mHideRunnable = this::hide;
    private Song song;
    private int verseIndex;
    private List<SongVerse> verseList;
    private long startTime;
    private long duration = 0;
    private SongRepositoryImpl songRepository;
    private Date lastDatePressedAtEnd = null;
    private boolean show_title_switch;
    private Integer insetsTop = null;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setTheme(Preferences.getTheme(this));
        super.onCreate(savedInstanceState);
        setContentView(R.layout.youtube_activity);
        onCreate1();
        onCreate2();
        onCreate3();
    }

    private void onCreate1() {
        song = Memory.getInstance().getPassingSong();
        try {
            textView = findViewById(R.id.fullscreen_content);
            textView.setSingleLine(false);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                textView.setBreakStrategy(BREAK_STRATEGY_SIMPLE);
            }
            // Set up the user interaction to manually show or hide the system UI.
            // Upon interacting with UI controls, delay any scheduled hide()
            // operations to prevent the jarring behavior of controls going away
            // while interacting with the UI.
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            int max_text_size = sharedPreferences.getInt("max_text_size", -1);
            if (max_text_size > 0) {
                textView.setTextSize(max_text_size);
            }
            boolean light_theme = sharedPreferences.getBoolean("light_theme_switch", false);
            LinearLayout layout = findViewById(R.id.layout);
            if (light_theme) {
                layout.setBackgroundResource(R.color.white);
                textView.setBackgroundResource(R.color.white);
                textView.setTextColor(getResources().getColor(R.color.black));
            } else {
                layout.setBackgroundResource(R.color.black);
                textView.setBackgroundResource(R.color.black);
                textView.setTextColor(getResources().getColor(R.color.white));
            }
        } catch (Exception e) {
            logE(e);
        }
    }

    public static void logWithNullCheck(Exception e, String simpleName) {
        String message = e.getMessage();
        if (message == null) {
            message = "";
        }
        Log.e(simpleName, message);
    }

    private boolean setNextVerseByPageUpDown(int keyCode) {
        if (keyCode == KeyEvent.KEYCODE_PAGE_UP) {
            setPreviousVerse();
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_PAGE_DOWN) {
            setNextVerse();
            return true;
        }
        return false;
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (consumePageUpDown(event)) {
            return true;
        }
        return super.dispatchKeyEvent(event);
    }

    private boolean consumePageUpDown(KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            int keyCode = event.getKeyCode();
            return setNextVerseByPageUpDown(keyCode);
        }
        return false;
    }

    @SuppressLint("ClickableViewAccessibility")
    private void onCreate2() {
        try {
            List<SongVerse> verses = song.getSongVersesByVerseOrder();
            verseList = new ArrayList<>(verses.size());
            verseList.addAll(verses);
            verseIndex = 0;
            textView.setOnTouchListener(new OnSwipeTouchListener(this) {

                public void onSwipeLeft() {
                    setNextVerse();
                }

                public void onSwipeRight() {
                    setPreviousVerse();
                }

                @Override
                public void performTouchLeftRight(MotionEvent event) {
                    if (event == null || textView == null) {
                        return;
                    }
                    //noinspection IntegerDivisionInFloatingPointContext
                    if (event.getX() < textView.getWidth() / 2) {
                        setPreviousVerse();
                    } else {
                        setNextVerse();
                    }
                }
            });

            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            show_title_switch = sharedPreferences.getBoolean("show_title_switch", false);
            if (show_title_switch) {
                SongVerse songVerse = new SongVerse();
                String title = getTextForTitleSlide(song);
                songVerse.setText(title);
                verseList.add(0, songVerse);
            }
            boolean blank_switch = sharedPreferences.getBoolean("blank_switch", false);
            if (blank_switch) {
                SongVerse songVerse = new SongVerse();
                songVerse.setText("");
                verseList.add(songVerse);
            }
            setText(verseList.get(verseIndex).getText());
            Thread thread = new Thread(() -> songRepository = new SongRepositoryImpl(getApplicationContext()));
            thread.start();
        } catch (Exception e) {
            logE(e);
        }
    }

    private void onCreate3() {
        try {
            WebView webView = findViewById(R.id.webView);
            if (webView == null) {
                return;
            }
            if (song == null) {
                return;
            }
            setYouTubeIFrameToWebView(webView, song.getYoutubeUrl(), this);
        } catch (Exception e) {
            Toast.makeText(this, "Could not create webView. " + e.getMessage(), Toast.LENGTH_LONG).show();
            logE(e);
        }
    }

    private static void logE(Exception e) {
        logWithNullCheck(e, YoutubeActivity.class.getSimpleName());
    }

    @Override
    protected void setupWindowInsets() {
        // Only apply window insets handling on Android 15 (API 35) and above
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            return;
        }

        // YoutubeActivity uses fullscreen mode, but we still need to handle insets
        // when system UI is visible. Apply height to the spacer view.
        View spacer = findViewById(R.id.status_bar_spacer);
        if (spacer != null) {
            ViewCompat.setOnApplyWindowInsetsListener(spacer, (v, windowInsets) -> {
                Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());

                // Set the spacer height to match the status bar height
                android.view.ViewGroup.LayoutParams params = v.getLayoutParams();
                if (insetsTop == null) {
                    insetsTop = insets.top;
                }
                params.height = insetsTop;
                v.setLayoutParams(params);

                return windowInsets;
            });
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

    void setText(String text) {
        onTextForListeners(text);
        String s = text.replaceAll("<color=\"0x(.{0,6})..\">", "<font color='0x$1'>")
                .replaceAll("</color>", "</font>")
                .replaceAll("\\[", "<i>")
                .replaceAll("]", "</i>")
                .replaceAll("\n", "<br>");
        textView.setText(Html.fromHtml(s), TextView.BufferType.SPANNABLE);
    }

    @Override
    protected void onPause() {
        super.onPause();
        long endTime = new Date().getTime();
        duration += endTime - startTime;
    }

    @Override
    protected void onResume() {
        super.onResume();
        startTime = new Date().getTime();
        hide();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (songRepository != null) {
            Thread thread = new Thread(() -> {
                try {
                    Song song = songRepository.findOne(YoutubeActivity.this.song.getId());
                    Date date = new Date();
                    long endTime = date.getTime();
                    duration += endTime - startTime;
                    song.setLastAccessed(date);
                    Long accessedTimes = song.getAccessedTimes();
                    song.setAccessedTimes(accessedTimes + 1);
                    song.setAccessedTimeAverage((accessedTimes * song.getAccessedTimeAverage() + duration) / song.getAccessedTimes());
                    songRepository.save(song);
                } catch (Exception ignored) {
                }
            });
            thread.start();
        }
    }

    private void setPreviousVerse() {
        if (verseIndex > 0) {
            --verseIndex;
            setText(verseList.get(verseIndex).getText());
            textView.startAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_from_left));
        }
    }

    private void setNextVerse() {
        if (verseIndex + 1 < verseList.size()) {
            ++verseIndex;
            setText(verseList.get(verseIndex).getText());
            textView.startAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_from_right));
        } else {
            Date now = new Date();
            int interval = 777;
            if (lastDatePressedAtEnd != null) {
                if (now.getTime() - lastDatePressedAtEnd.getTime() >= interval) {
                    Toast.makeText(this, R.string.press_twice, Toast.LENGTH_SHORT).show();
                } else {
                    if (show_title_switch) {
                        verseIndex = 1;
                    } else {
                        verseIndex = 0;
                    }
                    setText(verseList.get(verseIndex).getText());
                    lastDatePressedAtEnd = null;
                    return;
                }
            }
            lastDatePressedAtEnd = now;
        }
    }
}
