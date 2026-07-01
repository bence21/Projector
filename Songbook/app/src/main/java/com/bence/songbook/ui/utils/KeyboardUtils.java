package com.bence.songbook.ui.utils;

import android.app.Activity;
import android.content.Context;
import android.os.IBinder;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.Nullable;

public final class KeyboardUtils {

    private KeyboardUtils() {
    }

    public static void hideKeyboard(@Nullable Activity activity) {
        if (activity == null || activity.isFinishing()) {
            return;
        }
        if (activity.isDestroyed()) {
            return;
        }
        View view = activity.getCurrentFocus();
        if (view == null) {
            return;
        }
        IBinder token = view.getWindowToken();
        if (token == null) {
            return;
        }
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm == null) {
            return;
        }
        try {
            imm.hideSoftInputFromWindow(token, 0);
        } catch (SecurityException ignored) {
            // Android 15+ ImeTracker binder regression on some OEM devices
        }
    }
}
