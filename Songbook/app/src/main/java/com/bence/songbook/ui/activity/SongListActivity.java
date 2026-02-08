package com.bence.songbook.ui.activity;

import static com.bence.songbook.utils.BaseURL.BASE_URL;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.LongSparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import com.bence.projector.common.dto.SongListDTO;
import com.bence.songbook.Memory;
import com.bence.songbook.R;
import com.bence.songbook.api.SongListApiBean;
import com.bence.songbook.models.QueueSong;
import com.bence.songbook.models.Song;
import com.bence.songbook.models.SongList;
import com.bence.songbook.models.SongListElement;
import com.bence.songbook.repository.impl.ormLite.QueueSongRepositoryImpl;
import com.bence.songbook.repository.impl.ormLite.SongListElementRepositoryImpl;
import com.bence.songbook.repository.impl.ormLite.SongListRepositoryImpl;
import com.bence.songbook.ui.utils.DynamicListView;
import com.bence.songbook.ui.utils.Preferences;
import com.bence.songbook.ui.utils.SongListElementAdapter;
import com.bence.songbook.utils.Utility;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class SongListActivity extends BaseActivity {
    public static final String TAG = SongListActivity.class.getSimpleName();
    private static final int NEW_SONG_LIST_REQUEST_CODE = 1;
    private final Memory memory = Memory.getInstance();
    private List<SongListElement> songListElements;
    private SongList songList;
    private SongListRepositoryImpl songListRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(Preferences.getTheme(this));
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_song_list);
        Toolbar toolbar = findViewById(R.id.toolbar);
        songList = memory.getPassingSongList();
        if (songList == null) {
            return;
        }
        if (toolbar != null) {
            toolbar.setTitle(songList.getTitle());
            setSupportActionBar(toolbar);
        }
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        final DynamicListView<SongListElementAdapter> listView = findViewById(R.id.dListView);
        songListElements = songList.getSongListElements();
        Collections.sort(songListElements, (o1, o2) -> Utility.compare(o1.getNumber(), o2.getNumber()));
        SongListElementAdapter songListAdapter = new SongListElementAdapter(this, R.layout.list_row, songListElements,
                listView::onGrab, false);
        fetchSongAttributes();
        final SongListElementRepositoryImpl songListElementRepository = new SongListElementRepositoryImpl(this);
        songListRepository = new SongListRepositoryImpl(this);
        listView.setAdapter(songListAdapter);
        listView.setListener(new DynamicListView.Listener() {
            @Override
            public void swapElements(int indexOne, int indexTwo) {
                SongListElement temp = songListElements.get(indexOne);
                SongListElement secondTmp = songListElements.get(indexTwo);
                int queueNumber = temp.getNumber();
                int secondTmpOriginalNumber = secondTmp.getNumber();
                // In-memory swap for UI
                temp.setNumber(secondTmpOriginalNumber);
                secondTmp.setNumber(queueNumber);
                songListElements.set(indexOne, secondTmp);
                songListElements.set(indexTwo, temp);
                // Persist in a single transaction (placeholder avoids unique constraint)
                songListElementRepository.saveSwap(temp, secondTmp, queueNumber, secondTmpOriginalNumber);
                songList.setModifiedDate(new Date());
                songListRepository.save(songList);
            }

            @Override
            public void deleteElement(int originalItem) {
                SongListElement temp = songListElements.remove(originalItem);
                List<SongListElement> listElements = new ArrayList<>();
                for (int i = originalItem + 1; i < songListElements.size(); ++i) {
                    SongListElement element = songListElements.get(i);
                    element.setNumber(element.getNumber() - 1);
                    listElements.add(element);
                }
                songListElementRepository.save(listElements);
                songListElementRepository.delete(temp);
                songList.setModifiedDate(new Date());
                songListRepository.save(songList);
                listView.invalidateViews();
                listView.refreshDrawableState();
            }
        });
        listView.setOnItemClickListener((parent, view, position, id) -> {
            Song song = songListElements.get(position).getSong();
            Intent intent = new Intent(SongListActivity.this, SongActivity.class);
            memory.setPassingSong(song);
            startActivityForResult(intent, NEW_SONG_LIST_REQUEST_CODE);
        });
        if (getIntent().getBooleanExtra("newSongList", false)) {
            edit(2);
        }
    }

    private void fetchSongAttributes() {
        List<Song> songs = memory.getSongs();
        if (songs == null) {
            return;
        }
        LongSparseArray<Song> hashMap = new LongSparseArray<>(songs.size());
        for (Song song : songs) {
            hashMap.put(song.getId(), song);
        }
        for (SongListElement element : songListElements) {
            if (element.getSong() != null) {
                Long songId = element.getSong().getId();
                Song song = hashMap.get(songId);
                if (song != null) {
                    element.setSong(song);
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.content_song_list_menu, menu);
        return true;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            finish();
        } else if (itemId == R.id.action_add_to_queue) {
            QueueSongRepositoryImpl queueSongRepository = new QueueSongRepositoryImpl(this);
            List<QueueSong> newQueueSongs = new ArrayList<>(songListElements.size());
            for (SongListElement element : songListElements) {
                QueueSong queueSong = new QueueSong();
                queueSong.setSong(element.getSong());
                memory.addSongToQueue(queueSong);
                newQueueSongs.add(queueSong);
            }
            queueSongRepository.save(newQueueSongs);
            showToaster(getString(R.string.added_to_queue));
        } else if (itemId == R.id.action_share) {
            songList.setPublish(true);
            songListRepository.save(songList);
            if (songList.getUuid() != null) {
                Thread thread = new Thread(() -> {
                    SongListApiBean songListApiBean = new SongListApiBean(SongListActivity.this);
                    songListApiBean.uploadSongList(songList);
                });
                thread.start();
                shareSongList();
            } else {
                Thread thread = new Thread(() -> {
                    SongListApiBean songListApiBean = new SongListApiBean(SongListActivity.this);
                    SongListDTO songListDTO = songListApiBean.uploadSongList(songList);
                    if (songListDTO != null) {
                        songList.setUuid(songListDTO.getUuid());
                        songListRepository.save(songList);
                        shareSongList();
                    }
                });
                thread.start();
            }

        } else if (itemId == R.id.action_edit) {
            edit(NEW_SONG_LIST_REQUEST_CODE);
        } else if (itemId == R.id.action_delete) {
            SongListRepositoryImpl songListRepository = new SongListRepositoryImpl(this);
            songListRepository.delete(songList);
            setResult(1);
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private void edit(int requestCode) {
        Intent intent = new Intent(this, NewSongListActivity.class);
        memory.setEditingSongList(songList);
        intent.putExtra("edit", true);
        startActivityForResult(intent, requestCode);
    }

    private void shareSongList() {
        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("text/plain");
        share.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        share.putExtra(Intent.EXTRA_SUBJECT, songList.getTitle());
        share.putExtra(Intent.EXTRA_TITLE, songList.getTitle());
        share.putExtra(Intent.EXTRA_TEXT, songList.getTitle() + ":\n" + BASE_URL + "songList/" + songList.getUuid());
        startActivity(Intent.createChooser(share, "Share song list!"));
    }

    private void showToaster(String s) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == NEW_SONG_LIST_REQUEST_CODE) {
            if (resultCode == 1) {
                setResult(2);
                finish();
            }
        }
    }

}
