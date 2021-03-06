package com.bence.songbook.ui.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AnimationUtils;

import com.bence.songbook.Memory;
import com.bence.songbook.R;
import com.bence.songbook.network.ProjectionTextChangeListener;
import com.bence.songbook.network.TCPClient;
import com.bence.songbook.ui.utils.OnSwipeTouchListener;

import java.util.List;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class ConnectToSharedFullscreenActivity extends AbstractFullscreenActivity {
    private List<String> texts;
    private int textIndex = -1;

    private void setPreviousVerse() {
        if (textIndex > 0) {
            --textIndex;
            setText(texts.get(textIndex));
            textView.startAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_from_left));
        }
    }

    private void setNextVerse() {
        if (textIndex + 1 < texts.size()) {
            ++textIndex;
            setText(texts.get(textIndex));
            textView.startAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_from_right));
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        texts = Memory.getInstance().getSharedTexts();
        textIndex = texts.size() - 1;
        if (textIndex >= 0) {
            setText(texts.get(textIndex));
        } else {
            setText(getString(R.string.connection_successfully_wait_));
        }
        final View mContentView = findViewById(R.id.fullscreen_content);
        mContentView.setOnTouchListener(new OnSwipeTouchListener(this) {

            public void onSwipeLeft() {
                setNextVerse();
            }

            public void onSwipeRight() {
                setPreviousVerse();
            }

            @SuppressLint("ClickableViewAccessibility")
            public boolean onTouch(View v, MotionEvent event) {
                return gestureDetector.onTouchEvent(event);
            }

            @Override
            public void performTouchLeftRight(MotionEvent event) {
                if (event.getX() < mContentView.getWidth() / 2) {
                    setPreviousVerse();
                } else {
                    setNextVerse();
                }
            }
        });
        try {
            Intent intent = getIntent();
            String connectToShared = intent.getStringExtra("connectToShared");
            if (connectToShared != null && !connectToShared.isEmpty()) {
                TCPClient.connectToShared(this, connectToShared, new ProjectionTextChangeListener() {
                    @Override
                    public void onSetText(final String text) {
                        try {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    setText(text);
                                    if (textIndex < 0 || !texts.get(textIndex).equals(text)) {
                                        textIndex = texts.size();
                                        texts.add(text);
                                    }
                                }
                            });
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
            String message = e.getMessage();
            if (message != null) {
                Log.e(ConnectToSharedFullscreenActivity.class.getSimpleName(), message);
            }
            setResult(-1);
            finish();
        }
        super.setContext(this);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_UP:
                setPreviousVerse();
                break;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                setNextVerse();
                break;
            default:
                return super.onKeyDown(keyCode, event);
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        setResult(0);
        finish();
    }
}
