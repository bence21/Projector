package com.bence.psbremote.ui.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.bence.psbremote.R;
import com.bence.psbremote.SongSenderRemoteListener;
import com.bence.psbremote.model.Song;
import com.bence.psbremote.util.Memory;
import com.bence.psbremote.util.OnChangeListener;

import java.util.ArrayList;
import java.util.List;

public class SongsActivity extends AppCompatActivity {

    private Memory memory = Memory.getInstance();
    private ListView listView;
    private SongSenderRemoteListener songSenderRemoteListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_songs);
        listView = findViewById(R.id.listView);
        final EditText searchEditText = findViewById(R.id.searchEditText);
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                songSenderRemoteListener.onSearch(searchEditText.getText().toString().trim());
            }
        });
        songSenderRemoteListener = memory.getSongSenderRemoteListener();
        memory.getSongs().addOnChangeListener(new OnChangeListener() {
            @Override
            public void onChange() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        SongAdapter adapter = new SongAdapter(SongsActivity.this, R.layout.content_song_list_row, memory.getSongs());
                        listView.setAdapter(adapter);
                    }
                });
            }
        });
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                songSenderRemoteListener.onSongListViewItemClick(position);
                songButtonClick(null);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        SongAdapter adapter = new SongAdapter(SongsActivity.this, R.layout.content_song_list_row, memory.getSongs());
        listView.setAdapter(adapter);
    }

    public void songButtonClick(View view) {
        Intent intent = new Intent(this, SongActivity.class);
        startActivity(intent);
    }

    class SongAdapter extends ArrayAdapter<Song> {
        private List<Song> songList;

        SongAdapter(Context context, int textViewResourceId,
                    List<Song> songList) {
            super(context, textViewResourceId, songList);
            this.songList = new ArrayList<>();
            this.songList.addAll(songList);
        }

        @SuppressLint({"InflateParams", "SetTextI18n"})
        @SuppressWarnings("ConstantConditions")
        @NonNull
        @Override
        public View getView(final int position, View convertView, @NonNull ViewGroup parent) {

            SongAdapter.ViewHolder holder;

            if (convertView == null) {
                LayoutInflater layoutInflater = (LayoutInflater) getSystemService(
                        Context.LAYOUT_INFLATER_SERVICE);
                convertView = layoutInflater.inflate(R.layout.content_song_list_row, null);

                holder = new SongAdapter.ViewHolder();
                holder.ordinalNumberTextView = convertView.findViewById(R.id.ordinalNumberTextView);
                holder.titleTextView = convertView.findViewById(R.id.titleTextView);
                convertView.setTag(holder);
            } else {
                holder = (SongAdapter.ViewHolder) convertView.getTag();
            }

            Song song = songList.get(position);
            holder.ordinalNumberTextView.setText("");
            holder.titleTextView.setText(song.getTitle());
            holder.titleTextView.setTag(song);

            return convertView;
        }

        @Override
        public void notifyDataSetChanged() {
            super.notifyDataSetChanged();
        }

        private class ViewHolder {
            TextView ordinalNumberTextView;
            TextView titleTextView;
        }
    }
}
