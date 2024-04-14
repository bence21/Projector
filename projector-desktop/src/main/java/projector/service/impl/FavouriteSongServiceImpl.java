package projector.service.impl;

import com.bence.projector.common.dto.FavouriteSongDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import projector.api.FavouriteSongApiBean;
import projector.api.assembler.FavouriteSongAssembler;
import projector.controller.util.UserService;
import projector.model.FavouriteSong;
import projector.model.Language;
import projector.model.Song;
import projector.repository.DAOFactory;
import projector.service.FavouriteSongService;
import projector.service.LanguageService;
import projector.service.ServiceException;
import projector.service.ServiceManager;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class FavouriteSongServiceImpl extends AbstractBaseService<FavouriteSong> implements FavouriteSongService {

    private static final Logger LOG = LoggerFactory.getLogger(FavouriteSongServiceImpl.class);
    private List<FavouriteSong> favouriteSongs;

    public FavouriteSongServiceImpl() {
        super(DAOFactory.getInstance().getFavouriteSongDAO());
    }

    @Override
    public void syncFavourites() {
        Thread thread = new Thread(() -> {
            if (!UserService.getInstance().isLoggedIn()) {
                return;
            }
            FavouriteSongService favouriteSongService = ServiceManager.getFavouriteSongService();
            List<FavouriteSong> favouriteSongs = favouriteSongService.findAll();
            List<FavouriteSong> notUploadedFavouriteSongs = getNotUploadedFavouriteSongs(favouriteSongs);
            if (notUploadedFavouriteSongs.size() == 0) {
                return;
            }
            FavouriteSongApiBean favouriteSongApiBean = new FavouriteSongApiBean();
            List<FavouriteSongDTO> favouriteSongDTOS = favouriteSongApiBean.uploadFavouriteSongs(notUploadedFavouriteSongs);
            if (favouriteSongDTOS != null) {
                setUploadedToServer(notUploadedFavouriteSongs, favouriteSongService);
            }
        });
        thread.start();
    }

    @Override
    public void syncFavouritesFromServer(final FavouriteSongUpdateListener favouriteSongUpdateListener) {
        Thread thread = new Thread(() -> {
            try {
                FavouriteSongApiBean favouriteSongApiBean = new FavouriteSongApiBean();
                List<Language> selectedLanguages = getSelectedLanguages();
                Date serverModifiedDate = getServerModifiedDateByLanguages(selectedLanguages);
                List<FavouriteSongDTO> favouriteSongDTOS = favouriteSongApiBean.getFavouriteSongs(serverModifiedDate);
                if (favouriteSongDTOS != null) {
                    if (favouriteSongDTOS.size() > 0) {
                        FavouriteSongService favouriteSongService = ServiceManager.getFavouriteSongService();
                        List<FavouriteSong> favouriteSongs = favouriteSongService.findAll();
                        List<FavouriteSong> appliedFavouriteSongs = applyFavouriteSongs(favouriteSongs, favouriteSongDTOS);
                        favouriteSongService.create(appliedFavouriteSongs);
                        saveLastServerModifiedDate(selectedLanguages, serverModifiedDate, favouriteSongDTOS);
                        if (favouriteSongUpdateListener != null) {
                            favouriteSongUpdateListener.onUpdated();
                        }
                    }
                }
            } catch (Exception e) {
                LOG.error(e.getMessage(), e);
            }
        });
        thread.start();
    }

    private List<Language> getSelectedLanguages() {
        List<Language> languages = ServiceManager.getLanguageService().findAll();
        List<Language> selectedLanguages = new ArrayList<>();
        for (Language language : languages) {
            if (language.isSelected()) {
                selectedLanguages.add(language);
            }
        }
        if (selectedLanguages.size() == 0) {
            return languages;
        }
        return selectedLanguages;
    }

    private Date getServerModifiedDateByLanguages(List<Language> selectedLanguages) {
        if (selectedLanguages.size() == 0) {
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

    private List<FavouriteSong> applyFavouriteSongs(List<FavouriteSong> favouriteSongs, List<FavouriteSongDTO> favouriteSongDTOS) {
        ArrayList<FavouriteSong> appliedFavouriteSongs = new ArrayList<>();
        HashMap<String, FavouriteSong> hashMap = getHashMapBySong(favouriteSongs);
        FavouriteSongAssembler favouriteSongAssembler = FavouriteSongAssembler.getInstance();
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

    private void saveLastServerModifiedDate(final List<Language> selectedLanguages, Date serverModifiedDate, final List<FavouriteSongDTO> favouriteSongDTOS) {
        for (FavouriteSongDTO favouriteSongDTO : favouriteSongDTOS) {
            Date favouriteSongDTOServerModifiedDate = favouriteSongDTO.getServerModifiedDate();
            if (favouriteSongDTOServerModifiedDate != null && serverModifiedDate.getTime() < favouriteSongDTOServerModifiedDate.getTime()) {
                serverModifiedDate = favouriteSongDTOServerModifiedDate;
            }
        }
        for (Language language : selectedLanguages) {
            language.setFavouriteSongLastServerModifiedDate(serverModifiedDate);
        }
        LanguageService languageService = ServiceManager.getLanguageService();
        languageService.create(selectedLanguages);
    }

    @Override
    public List<FavouriteSong> findAll() throws ServiceException {
        List<FavouriteSong> favouriteSongsFromRepository = super.findAll();
        List<FavouriteSong> favouriteSongs = getFavouriteSongs();
        favouriteSongs.clear();
        favouriteSongs.addAll(favouriteSongsFromRepository);
        return favouriteSongs;
    }

    private List<FavouriteSong> getFavouriteSongs() {
        if (favouriteSongs == null) {
            favouriteSongs = new ArrayList<>();
        }
        return favouriteSongs;
    }

    private void setUploadedToServer(List<FavouriteSong> favouriteSongs, FavouriteSongService favouriteSongService) {
        for (FavouriteSong favouriteSong : favouriteSongs) {
            favouriteSong.setUploadedToServer(true);
        }
        favouriteSongService.create(favouriteSongs);
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
