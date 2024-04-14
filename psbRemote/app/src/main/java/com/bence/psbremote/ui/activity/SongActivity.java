package com.bence.psbremote.ui.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.bence.psbremote.R;
import com.bence.psbremote.SongSenderRemoteListener;
import com.bence.psbremote.util.Memory;
import com.bence.psbremote.util.ObservableList;
import com.bence.psbremote.util.OnChangeListener;

import java.util.ArrayList;

public class SongActivity extends AbstractFullscreenActivity {

    private SongSenderRemoteListener songSenderRemoteListener;
    private Memory memory = Memory.getInstance();
    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        songSenderRemoteListener = memory.getSongSenderRemoteListener();
        listView = findViewById(R.id.listView);
        memory.addProjectionTextOnChangeListener(new OnChangeListener() {
            @Override
            public void onChange() {
                setText(memory.getProjectionText());
            }
        });
        memory.getSongVerses().addOnChangeListener(new OnChangeListener() {
            @Override
            public void onChange() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        setAdapter();
                    }
                });
            }
        });
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                songSenderRemoteListener.onSongVerseListViewItemClick(position);
            }
        });
    }

    private void setAdapter() {
        ObservableList<String> songVerses = memory.getSongVerses();
        ArrayList<String> strings = new ArrayList<>(songVerses);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(SongActivity.this,
                android.R.layout.simple_list_item_1, android.R.id.text1, strings);
        listView.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setAdapter();
        setText(memory.getProjectionText());
    }

    public void prevButtonClick(View view) {
        songSenderRemoteListener.onSongPrev();
    }

    public void nextButtonClick(View view) {
        songSenderRemoteListener.onSongNext();
    }
}
