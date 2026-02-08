package com.bence.songbook.ui.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;

import com.bence.projector.common.dto.SongVerseDTO;
import com.bence.projector.common.dto.SuggestionDTO;
import com.bence.projector.common.model.SectionType;
import com.bence.songbook.Memory;
import com.bence.songbook.R;
import com.bence.songbook.api.SuggestionApiBean;
import com.bence.songbook.models.Song;
import com.bence.songbook.models.SongVerse;
import com.bence.songbook.service.UserService;
import com.bence.songbook.ui.utils.CheckSongForUpdate;
import com.bence.songbook.ui.utils.CheckSongForUpdateListener;
import com.bence.songbook.ui.utils.Preferences;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class SuggestEditsActivity extends BaseActivity {

    private Song song;
    private boolean edit = false;
    private EditText titleEditText;
    private EditText textEditText;

    @SuppressLint("CutPasteId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(Preferences.getTheme(this));
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suggest_edits);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.suggest_edits);
        setSupportActionBar(toolbar);
        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setDisplayHomeAsUpEnabled(true);
            supportActionBar.setDisplayShowHomeEnabled(true);
        }
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> submit());
        song = Memory.getInstance().getPassingSong();
        if (song == null) {
            finish();
            return;
        }
        Intent intent = getIntent();
        String method = intent.getStringExtra("method");
        titleEditText = findViewById(R.id.title);
        textEditText = findViewById(R.id.text);
        if (titleEditText == null) {
            finish();
            return;
        }
        titleEditText.setText(song.getTitle());
        textEditText.setText(getText(song));
        if (method != null && method.equals("EDIT")) {
            edit = true;
        } else {
            titleEditText.setKeyListener(null);
            titleEditText.setFocusable(false);
            titleEditText.setCursorVisible(false);
            textEditText.setKeyListener(null);
            textEditText.setFocusable(false);
            textEditText.setCursorVisible(false);
        }
        if (song.getUuid() != null && !song.getUuid().isEmpty()) {
            LayoutInflater layoutInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
            CheckSongForUpdate.getInstance().addListener(new CheckSongForUpdateListener(layoutInflater) {
                @Override
                public void onSongHasBeenModified(final PopupWindow updatePopupWindow) {
                    runOnUiThread(() -> {
                        LinearLayout updateSongsLayout = findViewById(R.id.updateSongsLayout);
                        updatePopupWindow.showAtLocation(updateSongsLayout, Gravity.CENTER, 0, 0);
                    });
                }

                @Override
                public void onUpdateButtonClick() {
                    setResult(CheckSongForUpdate.UPDATE_SONGS_RESULT);
                    finish();
                }
            }, song);
        }
    }

    private String getText(Song song) {
        StringBuilder text = new StringBuilder();
        for (SongVerse songVerse : song.getSongVersesByVerseOrder()) {
            if (text.length() > 0) {
                text.append("\n\n");
            }
            text.append(songVerse.getText().trim());
        }
        return text.toString();
    }

    private void submit() {
        final SuggestionDTO suggestionDTO = new SuggestionDTO();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String email = UserService.getInstance().getEmailFromUserOrGmail(this);
        if (email.isEmpty()) {
            email = sharedPreferences.getString("email", "");
        }
        suggestionDTO.setCreatedByEmail(email);
        final EditText suggestionEditText = findViewById(R.id.suggestion);
        String description = suggestionEditText.getText().toString().trim();
        if ((description.isEmpty() || description.equals("text")) && !edit) {
            Toast.makeText(this, R.string.no_description, Toast.LENGTH_SHORT).show();
            return;
        }
        suggestionDTO.setDescription(description);
        suggestionDTO.setSongId(song.getUuid());
        if (edit) {
            String title = titleEditText.getText().toString().trim();
            if (title.isEmpty() || title.equals("text")) {
                Toast.makeText(this, R.string.no_title, Toast.LENGTH_SHORT).show();
                return;
            }
            String text = textEditText.getText().toString().trim();
            if (song.getTitle().equals(title) && getText(song).equals(text)) {
                Toast.makeText(this, R.string.no_change, Toast.LENGTH_SHORT).show();
                return;
            }
            if (text.equalsIgnoreCase("text")) {
                Toast.makeText(this, R.string.too_short_text, Toast.LENGTH_SHORT).show();
                return;
            }
            suggestionDTO.setTitle(title);
            String[] split = text.split("\n\n");
            List<SongVerseDTO> songVerseDTOList = new ArrayList<>(song.getVerses().size());
            for (String s : split) {
                SongVerseDTO songVerseDTO = new SongVerseDTO();
                songVerseDTO.setText(s);
                songVerseDTO.setType(SectionType.VERSE.getValue());
                songVerseDTOList.add(songVerseDTO);
            }
            if (songVerseDTOList.size() == 0 || (songVerseDTOList.size() == 1 && songVerseDTOList.get(0).getText().isEmpty())) {
                Toast.makeText(this, R.string.empty_verses, Toast.LENGTH_SHORT).show();
                return;
            }
            suggestionDTO.setVerses(songVerseDTOList);
        }
        Thread thread = new Thread(() -> {
            SuggestionApiBean suggestionApiBean = new SuggestionApiBean();
            final SuggestionDTO uploadedSuggestion = suggestionApiBean.uploadSuggestion(suggestionDTO);
            runOnUiThread(() -> {
                if (uploadedSuggestion != null && !uploadedSuggestion.getUuid().trim().isEmpty()) {
                    Toast.makeText(SuggestEditsActivity.this, R.string.successfully_uploaded, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(SuggestEditsActivity.this, R.string.upload_failed, Toast.LENGTH_SHORT).show();
                }
            });
        });
        thread.start();
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
