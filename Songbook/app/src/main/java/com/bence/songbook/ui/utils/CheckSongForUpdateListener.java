package com.bence.songbook.ui.utils;

import android.view.LayoutInflater;
import android.widget.PopupWindow;

public class CheckSongForUpdateListener {

    LayoutInflater inflater;

    protected CheckSongForUpdateListener(LayoutInflater layoutInflater) {
        inflater = layoutInflater;
    }

    public void onSongHasBeenModified(PopupWindow updatePopupWindow) {
    }

    public void onUpdateButtonClick() {
    }
}
