package com.bence.psbremote.ui.activity;

import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class FullscreenActivity extends AbstractFullscreenActivity {

    private int verseIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {

        } catch (Exception e) {
            Log.e(FullscreenActivity.class.getSimpleName(), e.getMessage());
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        super.hide();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
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

    private void setPreviousVerse() {
        if (verseIndex > 0) {
            --verseIndex;
        }
    }

    private void setNextVerse() {
    }

    @Override
    public void setText(String text) {
        super.setText(text);
    }

}
