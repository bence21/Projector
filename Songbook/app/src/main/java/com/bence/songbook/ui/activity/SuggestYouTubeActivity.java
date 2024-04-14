package com.bence.songbook.ui.activity;

import static com.bence.songbook.ui.utils.YouTubeIFrame.setYouTubeIFrameToWebView;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bence.projector.common.dto.SuggestionDTO;
import com.bence.songbook.Memory;
import com.bence.songbook.R;
import com.bence.songbook.api.SuggestionApiBean;
import com.bence.songbook.models.Song;
import com.bence.songbook.models.SongVerse;
import com.bence.songbook.repository.impl.ormLite.SongRepositoryImpl;
import com.bence.songbook.service.UserService;
import com.bence.songbook.ui.utils.CheckSongForUpdate;
import com.bence.songbook.ui.utils.CheckSongForUpdateListener;
import com.bence.songbook.ui.utils.Preferences;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class SuggestYouTubeActivity extends AppCompatActivity {
    private static final String TAG = SuggestYouTubeActivity.class.getSimpleName();

    private Song song;
    private WebView youTubeView;
    private EditText youtubeEditText;
    private CharSequence initialClipboardContent;
    private boolean initialClipboardContentReceived;

    @SuppressLint("CutPasteId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(Preferences.getTheme(this));
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suggest_youtube);
        youTubeView = findViewById(R.id.youtube_view);
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> submit());
        song = Memory.getInstance().getPassingSong();
        if (song == null) {
            finish();
            return;
        }
        youtubeEditText = findViewById(R.id.youtubeUrl);
        youtubeEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                try {
                    final String youtubeId = parseYoutubeUrl(String.valueOf(s));
                    if (youtubeId.length() < 21 && youtubeId.length() > 9) {
                        youTubeView.setVisibility(View.VISIBLE);
                        if (youTubeView != null) {
                            setYouTubeIFrameToWebView(youTubeView, youtubeId);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        EditText titleEditText = findViewById(R.id.title);
        EditText textEditText = findViewById(R.id.text);
        titleEditText.setText(song.getTitle());
        textEditText.setText(getText(song));
        titleEditText.setKeyListener(null);
        titleEditText.setFocusable(false);
        titleEditText.setCursorVisible(false);
        textEditText.setKeyListener(null);
        textEditText.setFocusable(false);
        textEditText.setCursorVisible(false);
        checkForSongUpdate();
        initializeBackButton();
        initialClipboardContentReceived = false;
    }

    private void openYoutubeApp() {
        Toast.makeText(this, getString(R.string.select_youtube_video), Toast.LENGTH_LONG).show();
        try {
            Intent youtubeIntent = new Intent(Intent.ACTION_SEARCH);
            youtubeIntent.setPackage("com.google.android.youtube");
            youtubeIntent.putExtra("query", song.getTitle());
            youtubeIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(youtubeIntent);
        } catch (Exception e) {
            Uri parse = Uri.parse("https://www.youtube.com/results?search_query=" + song.getTitle().replace(" ", "+"));
            startActivity(new Intent(Intent.ACTION_VIEW, parse));
        }
    }

    private void checkForSongUpdate() {
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

    private void initializeBackButton() {
        Button backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(this::onBackButtonClick);
    }

    private String getText(Song song) {
        StringBuilder text = new StringBuilder();
        for (SongVerse songVerse : song.getVerses()) {
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
        String url = youtubeEditText.getText().toString().trim();
        if (url.isEmpty()) {
            Toast.makeText(this, R.string.no_change, Toast.LENGTH_SHORT).show();
            return;
        }
        String youtubeId = parseYoutubeUrl(url);
        if (youtubeId.length() < 21 && youtubeId.length() > 9) {
            suggestionDTO.setYoutubeUrl(youtubeId);
        } else {
            Toast.makeText(this, R.string.Cannot_parse_YouTube_Url, Toast.LENGTH_SHORT).show();
            return;
        }
        suggestionDTO.setSongId(song.getUuid());
        Thread thread = new Thread(() -> {
            SuggestionApiBean suggestionApiBean = new SuggestionApiBean();
            final SuggestionDTO uploadedSuggestion = suggestionApiBean.uploadSuggestion(suggestionDTO);
            runOnUiThread(() -> {
                int resId;
                if (uploadedSuggestion != null && !uploadedSuggestion.getUuid().trim().isEmpty()) {
                    resId = R.string.successfully_uploaded;
                } else {
                    resId = R.string.upload_failed;
                }
                Toast.makeText(SuggestYouTubeActivity.this, resId, Toast.LENGTH_SHORT).show();
            });
        });
        thread.start();
        song.setYoutubeUrl(youtubeId);
        SongRepositoryImpl songRepository = new SongRepositoryImpl(this);
        songRepository.save(song);
        finish();
    }

    private String parseYoutubeUrl(String url) {
        String youtubeUrl = url.replace("https://www.youtube.com/watch?v=", "");
        youtubeUrl = youtubeUrl.replace("https://www.youtube.com/embed/", "");
        youtubeUrl = youtubeUrl.replace("https://youtu.be/", "");
        return youtubeUrl;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private CharSequence getClipboardText() {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        if (clipboard != null) {
            try {
                ClipData.Item item = clipboard.getPrimaryClip().getItemAt(0);
                return item.getText();
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    private void setTextFromClipboard() {
        try {
            CharSequence pasteData = getClipboardText();
            if (pasteData != null && !pasteData.equals(initialClipboardContent)) {
                youtubeEditText.setText(pasteData);
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            if (!initialClipboardContentReceived) {
                initialClipboardContentReceived = true;
                initialClipboardContent = getClipboardText();
                openYoutubeApp();
            } else {
                setTextFromClipboard();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    public void onBackButtonClick(View view) {
        finish();
    }
}
