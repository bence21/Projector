package com.bence.psbremote.util;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ObservableList<T> extends ArrayList<T> {

    private List<OnChangeListener> onChangeListeners;

    public void addOnChangeListener(OnChangeListener onChangeListener) {
        if (onChangeListeners == null) {
            onChangeListeners = new ArrayList<>();
        }
        onChangeListeners.add(onChangeListener);
    }

    @Override
    public boolean addAll(@NonNull Collection<? extends T> c) {
        boolean b = super.addAll(c);
        if (onChangeListeners != null) {
            for (OnChangeListener onChangeListener : onChangeListeners) {
                onChangeListener.onChange();
            }
        }
        return b;
    }
}
