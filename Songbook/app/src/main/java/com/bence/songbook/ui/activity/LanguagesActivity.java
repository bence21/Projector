package com.bence.songbook.ui.activity;

import static com.bence.songbook.ui.activity.LoadActivity.SERVER_IS_NOT_AVAILABLE;
import static com.bence.songbook.ui.activity.NewSongActivity.sortLanguagesByRecentlyViewedSongs;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.Toast;

import com.bence.songbook.Memory;
import com.bence.songbook.R;
import com.bence.songbook.api.LanguageApiBean;
import com.bence.songbook.models.Language;
import com.bence.songbook.repository.LanguageRepository;
import com.bence.songbook.repository.impl.ormLite.LanguageRepositoryImpl;
import com.bence.songbook.ui.adapter.LanguageAdapter;

import java.util.ArrayList;
import java.util.List;

public class LanguagesActivity extends BaseActivity {

    public static String syncAutomatically = "syncAutomatically";
    private LanguageAdapter dataAdapter = null;
    private List<Language> languages;
    private LanguagesActivity languagesActivity;
    private Toast noInternetConnectionToast;

    @SuppressLint("ShowToast")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_languages);
        languagesActivity = this;
        LanguageRepositoryImpl languageRepository = new LanguageRepositoryImpl(getApplicationContext());
        languages = languageRepository.findAll();
        sortLanguagesByRecentlyViewedSongs(languages, this);
        noInternetConnectionToast = Toast.makeText(getApplicationContext(), SERVER_IS_NOT_AVAILABLE, Toast.LENGTH_LONG);
        new Downloader().execute();

        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        final boolean syncAutomatically = sharedPreferences.getBoolean(LanguagesActivity.syncAutomatically, true);
        CheckBox checkBox = findViewById(R.id.checkBox);
        checkBox.setChecked(syncAutomatically);
        checkBox.setOnCheckedChangeListener(
                (buttonView, isChecked) ->
                        sharedPreferences.edit().putBoolean(LanguagesActivity.syncAutomatically, isChecked).apply()
        );
    }

    private void displayListView() {
        dataAdapter = new LanguageAdapter(this,
                R.layout.activity_language_checkbox_row, languages,
                (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE));
        ListView listView = findViewById(R.id.languageActivity_listView);
        listView.setAdapter(dataAdapter);
    }

    private void initializeDownloadButton() {
        Button myButton = findViewById(R.id.languageActivity_downloadButton);
        myButton.setOnClickListener(v -> {
            List<Language> languageList = dataAdapter.getLanguageList();
            List<Language> languages = new ArrayList<>();
            LanguageRepository languageRepository = new LanguageRepositoryImpl(getApplicationContext());
            for (Language language : languageList) {
                if (language.isSelected()) {
                    language.setSelectedForDownload(true);
                }
                languageRepository.save(language);
                if (language.isSelected()) {
                    languages.add(language);
                }
            }
            Memory.getInstance().setPassingLanguages(languages);
            Intent intent = new Intent(languagesActivity, LoadActivity.class);
            startActivityForResult(intent, 1);
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode >= 1) {
            setResult(resultCode);
            finish();
        }
    }

    @SuppressLint("StaticFieldLeak")
    class Downloader extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            LanguageApiBean languageApi = new LanguageApiBean();
            List<Language> onlineLanguages = languageApi.getLanguages();
            if (onlineLanguages != null) {
                List<Language> newLanguages = new ArrayList<>();
                for (Language onlineLanguage : onlineLanguages) {
                    boolean was = false;
                    for (Language language : languages) {
                        if (language.getUuid().equals(onlineLanguage.getUuid())) {
                            language.setSize(onlineLanguage.getSize());
                            was = true;
                            break;
                        }
                    }
                    if (!was) {
                        newLanguages.add(onlineLanguage);
                    }
                }
                languages.addAll(newLanguages);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (languages.size() == 0) {
                noInternetConnectionToast.show();
            } else {
                displayListView();
                initializeDownloadButton();
            }
        }
    }
}
