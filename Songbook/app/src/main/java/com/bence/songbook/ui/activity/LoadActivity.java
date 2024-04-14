package com.bence.songbook.ui.activity;

import static com.bence.songbook.models.Song.copyLocallySet;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bence.songbook.Memory;
import com.bence.songbook.ProgressMessage;
import com.bence.songbook.R;
import com.bence.songbook.api.SongApiBean;
import com.bence.songbook.api.SongCollectionApiBean;
import com.bence.songbook.models.Language;
import com.bence.songbook.models.Song;
import com.bence.songbook.models.SongCollection;
import com.bence.songbook.repository.LanguageRepository;
import com.bence.songbook.repository.SongCollectionRepository;
import com.bence.songbook.repository.SongRepository;
import com.bence.songbook.repository.impl.ormLite.LanguageRepositoryImpl;
import com.bence.songbook.repository.impl.ormLite.SongCollectionRepositoryImpl;
import com.bence.songbook.repository.impl.ormLite.SongRepositoryImpl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class LoadActivity extends AppCompatActivity {

    private Toast noInternetConnectionToast;
    private LoadActivity loadActivity;
    private int startedCount = 0;
    private int downloadedSongs = 0;
    private boolean backPressed = false;
    public static String SERVER_IS_NOT_AVAILABLE = "Server is not available";

    @SuppressLint({"ShowToast", "DefaultLocale", "SetTextI18n"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_load);
        noInternetConnectionToast = Toast.makeText(getApplicationContext(), SERVER_IS_NOT_AVAILABLE, Toast.LENGTH_LONG);
        loadActivity = this;
        List<Language> languages = Memory.getInstance().getPassingLanguages();
        if (languages == null) {
            return;
        }
        List<LanguageProgress> selectedLanguages = new ArrayList<>();
        for (Language language : languages) {
            selectedLanguages.add(new LanguageProgress(language));
        }
        MyCustomAdapter dataAdapter = new MyCustomAdapter(this,
                R.layout.content_language_download_progress, selectedLanguages);
        ListView listView = findViewById(R.id.listView);
        listView.setAdapter(dataAdapter);
    }

    @Override
    public void onBackPressed() {
        if (!backPressed) {
            Toast.makeText(this, R.string.downloading_progress_not_completed, Toast.LENGTH_LONG).show();
        } else {
            super.onBackPressed();
        }
        backPressed = true;
    }

    private void sortSongs(List<Song> all) {
        Collections.sort(all, (lhs, rhs) -> rhs.getModifiedDate().compareTo(lhs.getModifiedDate()));
    }

    private static class LanguageProgress {
        private Language language;
        private ProgressBar progressBar;
        private TextView textView;

        LanguageProgress(Language language) {
            this.language = language;
        }

        public Language getLanguage() {
            return language;
        }

        public void setLanguage(Language language) {
            this.language = language;
        }

        ProgressBar getProgressBar() {
            return progressBar;
        }

        void setProgressBar(ProgressBar progressBar) {
            this.progressBar = progressBar;
        }

        public TextView getTextView() {
            return textView;
        }

        public void setTextView(TextView textView) {
            this.textView = textView;
        }
    }

    private class MyCustomAdapter extends ArrayAdapter<LanguageProgress> {

        private final List<LanguageProgress> languageList;

        MyCustomAdapter(Context context, int textViewResourceId,
                        List<LanguageProgress> languageList) {
            super(context, textViewResourceId, languageList);
            this.languageList = new ArrayList<>();
            this.languageList.addAll(languageList);
        }

        @SuppressLint({"InflateParams", "SetTextI18n"})
        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {

            MyCustomAdapter.ViewHolder holder;

            if (convertView == null) {
                LayoutInflater layoutInflater = (LayoutInflater) getSystemService(
                        Context.LAYOUT_INFLATER_SERVICE);
                convertView = layoutInflater.inflate(R.layout.content_language_download_progress, null);

                holder = new MyCustomAdapter.ViewHolder();
                holder.nativeNameTextView = convertView.findViewById(R.id.selected_language_native_name);
                holder.englishNameTextView = convertView.findViewById(R.id.selected_language_english_name);
                holder.textView = convertView.findViewById(R.id.textView);
                holder.progressBar = convertView.findViewById(R.id.progressBar);
                convertView.setTag(holder);
            } else {
                holder = (MyCustomAdapter.ViewHolder) convertView.getTag();
            }

            LanguageProgress languageProgress = languageList.get(position);
            Language language = languageProgress.getLanguage();
            holder.nativeNameTextView.setText(" (" + language.getNativeName() + ")");
            holder.englishNameTextView.setText(language.getEnglishName());
            languageProgress.setProgressBar(holder.progressBar);
            languageProgress.setTextView(holder.textView);
            new Downloader(languageProgress).execute();
            return convertView;
        }

        private class ViewHolder {
            TextView nativeNameTextView;
            TextView englishNameTextView;
            TextView textView;
            ProgressBar progressBar;
        }

    }

    @SuppressLint("StaticFieldLeak")
    class Downloader extends AsyncTask<Void, Integer, Void> {
        private final Language language;
        private final ProgressBar progressBar;
        private final TextView textView;
        private List<SongCollection> onlineModifiedSongCollections;
        private ProgressMessage progressMessage;
        private String progressText = "";

        Downloader(LanguageProgress languageProgress) {
            language = languageProgress.getLanguage();
            progressBar = languageProgress.getProgressBar();
            textView = languageProgress.getTextView();
            ++startedCount;
        }

        @Override
        protected Void doInBackground(Void... voids) {

            progressMessage = new ProgressMessage() {
                @Override
                public void onProgress(int value) {
                    publishProgress(value);
                }

                @Override
                public void onSongCollectionProgress(int value) {
                    publishProgress(-8, value);
                }

                @Override
                public void onSetMax(int value) {
                    publishProgress(-7, value);
                }
            };
            SongRepository songRepository = new SongRepositoryImpl(getApplicationContext());
            LanguageRepository languageRepository = new LanguageRepositoryImpl(getApplicationContext());
            progressText = getString(R.string.downloading_songs);
            publishProgress(-9);
            final SongApiBean songApiBean = new SongApiBean();
            List<Song> languageSongs = language.getSongs();
            sortSongs(languageSongs);
            long modifiedDate;
            if (languageSongs.size() > 0) {
                modifiedDate = languageSongs.get(0).getModifiedDate().getTime();
            } else {
                modifiedDate = 0L;
            }
            List<Song> onlineModifiedSongs = songApiBean.getSongsByLanguageAndAfterModifiedDate(language, modifiedDate, progressMessage);
            if (onlineModifiedSongs == null) {
                noInternetConnectionToast.show();
            } else {
                progressText = getString(R.string.saving_songs);
                publishProgress(-9);
                saveSongs(songRepository, languageRepository, languageSongs, onlineModifiedSongs);
            }

            SongCollectionApiBean songCollectionApiBean = new SongCollectionApiBean();
            SongCollectionRepository songCollectionRepository = new SongCollectionRepositoryImpl(getApplicationContext());
            List<SongCollection> songCollectionRepositoryAll = songCollectionRepository.findAllByLanguage(language);
            Date lastModifiedDate = new Date(0);
            for (SongCollection songCollection : songCollectionRepositoryAll) {
                Date songCollectionModifiedDate = songCollection.getModifiedDate();
                if (songCollectionModifiedDate.compareTo(lastModifiedDate) > 0) {
                    lastModifiedDate = songCollectionModifiedDate;
                }
            }
            progressText = getString(R.string.downloading_collections);
            publishProgress(-9);
            onlineModifiedSongCollections = songCollectionApiBean.getSongCollections(language, lastModifiedDate, progressMessage);
            if (onlineModifiedSongCollections == null) {
                noInternetConnectionToast.show();
            } else {
                saveSongCollections(songCollectionRepository, songCollectionRepositoryAll, languageRepository);
            }
            progressText = getString(R.string.completed);
            publishProgress(-9);
            return null;
        }

        private void saveSongs(SongRepository songRepository, LanguageRepository languageRepository, List<Song> languageSongs, List<Song> onlineModifiedSongs) {
            downloadedSongs += onlineModifiedSongs.size();
            HashMap<String, Song> songHashMap = new HashMap<>(languageSongs.size());
            for (Song song : languageSongs) {
                songHashMap.put(song.getUuid(), song);
            }
            List<Song> needToRemove = new ArrayList<>();
            for (Song song : onlineModifiedSongs) {
                if (songHashMap.containsKey(song.getUuid())) {
                    Song modifiedSong = songHashMap.get(song.getUuid());
                    if (modifiedSong == null) {
                        continue;
                    }
                    copyLocallySet(song, modifiedSong);
                    needToRemove.add(modifiedSong);
                    languageSongs.remove(modifiedSong);
                }
            }
            sortSongs(onlineModifiedSongs);
            languageSongs.addAll(onlineModifiedSongs);
            language.setSongs(languageSongs);
            songRepository.deleteAll(needToRemove);
            progressBar.setMax(onlineModifiedSongs.size());
            songRepository.save(onlineModifiedSongs, progressBar);
            languageRepository.save(language);
        }

        private void saveSongCollections(SongCollectionRepository songCollectionRepository, List<SongCollection> songCollectionRepositoryAll, LanguageRepository languageRepository) {
            HashMap<String, SongCollection> songCollectionHashMap = new HashMap<>(songCollectionRepositoryAll.size());
            for (SongCollection songCollection : songCollectionRepositoryAll) {
                songCollectionHashMap.put(songCollection.getUuid(), songCollection);
            }
            List<SongCollection> needToDelete = new ArrayList<>();
            for (SongCollection songCollection : onlineModifiedSongCollections) {
                if (songCollectionHashMap.containsKey(songCollection.getUuid())) {
                    SongCollection modifiedSongCollection = songCollectionHashMap.get(songCollection.getUuid());
                    needToDelete.add(modifiedSongCollection);
                }
            }
            songCollectionRepository.deleteAll(needToDelete);
            songCollectionRepository.save(onlineModifiedSongCollections, progressBar, progressMessage);
            languageRepository.save(language);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            progressBar.setMax(5);
            progressBar.setProgress(progressBar.getMax());
            if (--startedCount == 0) {
                loadActivity.setResult(1 + downloadedSongs);
                loadActivity.finish();
            }
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            Integer value = values[0];
            if (value == -9) {
                textView.setText(progressText);
                return;
            }
            if (value == -8) {
                textView.setText(onlineModifiedSongCollections.get(values[1]).getName());
                return;
            }
            if (value == -7) {
                progressBar.setMax(values[1]);
            }
            progressBar.setProgress(value);
        }
    }
}
