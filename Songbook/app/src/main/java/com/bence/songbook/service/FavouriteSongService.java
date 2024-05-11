package com.bence.songbook.service;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.bence.projector.common.dto.FavouriteSongDTO;
import com.bence.songbook.api.FavouriteSongApiBean;
import com.bence.songbook.api.assembler.FavouriteSongAssembler;
import com.bence.songbook.models.FavouriteSong;
import com.bence.songbook.models.Language;
import com.bence.songbook.models.Song;
import com.bence.songbook.repository.impl.ormLite.FavouriteSongRepositoryImpl;
import com.bence.songbook.repository.impl.ormLite.LanguageRepositoryImpl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class FavouriteSongService {

    private static final String TAG = FavouriteSongService.class.getName();
    private static FavouriteSongService instance;

    private FavouriteSongService() {

    }

    public static FavouriteSongService getInstance() {
        if (instance == null) {
            instance = new FavouriteSongService();
        }
        return instance;
    }

    public void syncFavourites(final Context context) {
        Thread thread = new Thread(() -> {
            if (!UserService.getInstance().isLoggedIn(context)) {
                return;
            }
            FavouriteSongRepositoryImpl favouriteSongRepository = new FavouriteSongRepositoryImpl(context);
            List<FavouriteSong> favouriteSongs = favouriteSongRepository.findAll();
            List<FavouriteSong> notUploadedFavouriteSongs = getNotUploadedFavouriteSongs(favouriteSongs);
            if (notUploadedFavouriteSongs.isEmpty()) {
                return;
            }
            FavouriteSongApiBean favouriteSongApiBean = new FavouriteSongApiBean(context);
            List<FavouriteSongDTO> favouriteSongDTOS = favouriteSongApiBean.uploadFavouriteSongs(notUploadedFavouriteSongs);
            if (favouriteSongDTOS != null) {
                setUploadedToServer(notUploadedFavouriteSongs, favouriteSongRepository, context);
            }
        });
        thread.start();
    }

    public void syncFavouritesFromServer(final Context context, final FavouriteSongUpdateListener favouriteSongUpdateListener) {
        Thread thread = new Thread(() -> {
            FavouriteSongApiBean favouriteSongApiBean = new FavouriteSongApiBean(context);
            List<Language> selectedLanguages = getSelectedLanguages(context);
            Date serverModifiedDate = getServerModifiedDateByLanguages(selectedLanguages);
            List<FavouriteSongDTO> favouriteSongDTOS = favouriteSongApiBean.getFavouriteSongs(serverModifiedDate);
            if (favouriteSongDTOS != null) {
                if (!favouriteSongDTOS.isEmpty()) {
                    FavouriteSongRepositoryImpl favouriteSongRepository = new FavouriteSongRepositoryImpl(context);
                    List<FavouriteSong> favouriteSongs = favouriteSongRepository.findAll();
                    List<FavouriteSong> appliedFavouriteSongs = applyFavouriteSongs(favouriteSongs, favouriteSongDTOS, context);
                    favouriteSongRepository.save(appliedFavouriteSongs);
                    saveLastServerModifiedDate(selectedLanguages, serverModifiedDate, favouriteSongDTOS, context);
                    if (favouriteSongUpdateListener != null) {
                        favouriteSongUpdateListener.onUpdated();
                    }
                }
            }
        });
        thread.start();
    }

    private List<Language> getSelectedLanguages(Context context) {
        List<Language> languages = new LanguageRepositoryImpl(context).findAll();
        List<Language> selectedLanguages = new ArrayList<>();
        for (Language language : languages) {
            if (language.isSelected()) {
                selectedLanguages.add(language);
            }
        }
        if (selectedLanguages.isEmpty()) {
            return languages;
        }
        return selectedLanguages;
    }

    private Date getServerModifiedDateByLanguages(List<Language> selectedLanguages) {
        if (selectedLanguages.isEmpty()) {
            return new Date(0);
        }
        long serverModifiedDate = Long.MAX_VALUE;
        for (Language language : selectedLanguages) {
            if (language.getFavouriteSongLastServerModifiedDate().getTime() < serverModifiedDate) {
                serverModifiedDate = language.getFavouriteSongLastServerModifiedDate().getTime();
            }
        }
        return new Date(serverModifiedDate);
    }

    private List<FavouriteSong> applyFavouriteSongs(List<FavouriteSong> favouriteSongs, List<FavouriteSongDTO> favouriteSongDTOS, Context context) {
        ArrayList<FavouriteSong> appliedFavouriteSongs = new ArrayList<>();
        HashMap<String, FavouriteSong> hashMap = getHashMapBySong(favouriteSongs);
        FavouriteSongAssembler favouriteSongAssembler = FavouriteSongAssembler.getInstance(context);
        for (FavouriteSongDTO favouriteSongDTO : favouriteSongDTOS) {
            FavouriteSong favouriteSong = createOrUploadFavouriteSong(hashMap, favouriteSongDTO, favouriteSongAssembler);
            if (favouriteSong.getSong() == null) {
                continue;
            }
            appliedFavouriteSongs.add(favouriteSong);
        }
        return appliedFavouriteSongs;
    }

    private FavouriteSong createOrUploadFavouriteSong(HashMap<String, FavouriteSong> hashMap, FavouriteSongDTO favouriteSongDTO, FavouriteSongAssembler favouriteSongAssembler) {
        String key = getKey(favouriteSongDTO);
        if (key != null) {
            FavouriteSong favouriteSongFromMap = hashMap.get(key);
            if (favouriteSongFromMap != null) {
                if (favouriteSongFromMap.getModifiedDate().before(favouriteSongDTO.getModifiedDate())) {
                    favouriteSongAssembler.updateModel(favouriteSongFromMap, favouriteSongDTO);
                }
                return favouriteSongFromMap;
            }
        }
        return favouriteSongAssembler.createModel(favouriteSongDTO);
    }

    private HashMap<String, FavouriteSong> getHashMapBySong(List<FavouriteSong> favouriteSongs) {
        HashMap<String, FavouriteSong> hashMap = new HashMap<>();
        for (FavouriteSong favouriteSong : favouriteSongs) {
            hashMap.put(getKey(favouriteSong), favouriteSong);
        }
        return hashMap;
    }

    private String getKey(FavouriteSongDTO favouriteSongDTO) {
        return favouriteSongDTO.getSongUuid();
    }

    private String getKey(FavouriteSong favouriteSong) {
        Song song = favouriteSong.getSong();
        if (song == null) {
            return null;
        }
        return song.getUuid();
    }

    private void saveLastServerModifiedDate(final List<Language> selectedLanguages, Date serverModifiedDate, final List<FavouriteSongDTO> favouriteSongDTOS, Context context) {
        for (FavouriteSongDTO favouriteSongDTO : favouriteSongDTOS) {
            Date favouriteSongDTOServerModifiedDate = favouriteSongDTO.getServerModifiedDate();
            if (favouriteSongDTOServerModifiedDate != null && serverModifiedDate.getTime() < favouriteSongDTOServerModifiedDate.getTime()) {
                serverModifiedDate = favouriteSongDTOServerModifiedDate;
            }
        }
        for (Language language : selectedLanguages) {
            language.setFavouriteSongLastServerModifiedDate(serverModifiedDate);
        }
        LanguageRepositoryImpl languageRepository = new LanguageRepositoryImpl(context);
        languageRepository.save(selectedLanguages);
    }

    private void setUploadedToServer(List<FavouriteSong> favouriteSongs, FavouriteSongRepositoryImpl favouriteSongRepository, Context context) {
        for (FavouriteSong favouriteSong : favouriteSongs) {
            favouriteSong.setUploadedToServer(true);
        }
        try {
            favouriteSongRepository.save(favouriteSongs);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
            Toast.makeText(context, "Could not save favourite!", Toast.LENGTH_SHORT).show();
        }
    }

    private List<FavouriteSong> getNotUploadedFavouriteSongs(List<FavouriteSong> favouriteSongs) {
        List<FavouriteSong> notUploadedFavouriteSongs = new ArrayList<>();
        for (FavouriteSong favouriteSong : favouriteSongs) {
            if (!favouriteSong.isUploadedToServer()) {
                notUploadedFavouriteSongs.add(favouriteSong);
            }
        }
        return notUploadedFavouriteSongs;
    }

    public interface FavouriteSongUpdateListener {
        void onUpdated();
    }
}
