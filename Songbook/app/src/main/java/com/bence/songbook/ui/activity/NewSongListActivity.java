package com.bence.songbook.ui.activity;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bence.songbook.Memory;
import com.bence.songbook.R;
import com.bence.songbook.models.QueueSong;
import com.bence.songbook.models.Song;
import com.bence.songbook.models.SongList;
import com.bence.songbook.models.SongListElement;
import com.bence.songbook.repository.impl.ormLite.SongListElementRepositoryImpl;
import com.bence.songbook.repository.impl.ormLite.SongListRepositoryImpl;
import com.bence.songbook.ui.utils.Preferences;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class NewSongListActivity extends AppCompatActivity {
    public static final String TAG = NewSongListActivity.class.getSimpleName();
    private boolean saveQueue;
    private EditText titleEditText;
    private EditText descriptionEditText;
    private boolean edit;
    private boolean addSongToSongList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(Preferences.getTheme(this));
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_new_song_list);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.new_song_list);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        saveQueue = getIntent().getBooleanExtra("saveQueue", false);
        addSongToSongList = getIntent().getBooleanExtra("addSongToSongList", false);
        titleEditText = findViewById(R.id.titleEditText);
        descriptionEditText = findViewById(R.id.descriptionEditText);
        edit = getIntent().getBooleanExtra("edit", false);
        if (edit) {
            SongList songList = Memory.getInstance().getEditingSongList();
            if (songList.getId() != null) {
                toolbar.setTitle(R.string.edit_song_list);
            }
            titleEditText.setText(songList.getTitle());
            descriptionEditText.setText(songList.getDescription());
        }
    }

    public void onCancel(View view) {
        finish();
    }

    public void onCreateSongList(View view) {
        String title = titleEditText.getText().toString();
        if (title.isEmpty()) {
            titleEditText.setError(getString(R.string.required));
            return;
        }
        String description = descriptionEditText.getText().toString();
        SongList songList;
        if (edit) {
            songList = Memory.getInstance().getEditingSongList();
            songList.setModifiedDate(new Date());
        } else {
            songList = new SongList();
            songList.setCreatedDate(new Date());
            songList.setOwned(true);
            songList.setPublish(false);
            songList.setModifiedDate(songList.getCreatedDate());
        }
        songList.setTitle(title);
        songList.setDescription(description);
        SongListRepositoryImpl songListRepository = new SongListRepositoryImpl(this);
        songListRepository.save(songList);
        if (saveQueue) {
            List<QueueSong> queue = Memory.getInstance().getQueue();
            if (queue != null) {
                List<SongListElement> songListElements = new ArrayList<>(queue.size());
                for (QueueSong queueSong : queue) {
                    SongListElement songListElement = new SongListElement();
                    songListElement.setNumber(queueSong.getQueueNumber());
                    songListElement.setSong(queueSong.getSong());
                    songListElements.add(songListElement);
                }
                songList.setSongListElements(songListElements);
                SongListElementRepositoryImpl songListElementRepository = new SongListElementRepositoryImpl(this);
                songListElementRepository.save(songListElements);
            }
        }
        if (addSongToSongList) {
            List<SongListElement> songListElements = new ArrayList<>(1);
            Song song = Memory.getInstance().getPassingSong();
            SongListElement songListElement = new SongListElement();
            songListElement.setNumber(0);
            songListElement.setSong(song);
            songListElements.add(songListElement);
            songList.setSongListElements(songListElements);
            SongListElementRepositoryImpl songListElementRepository = new SongListElementRepositoryImpl(this);
            try {
                songListElementRepository.save(songListElements);
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), "Could not save song list element", Toast.LENGTH_LONG).show();
                return;
            }
        }
        setResult(1);
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
