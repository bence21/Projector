package com.bence.projector.server.api.resources;

import com.bence.projector.common.dto.FavouriteSongDTO;
import com.bence.projector.server.api.assembler.FavouriteSongAssembler;
import com.bence.projector.server.backend.model.FavouriteSong;
import com.bence.projector.server.backend.model.Song;
import com.bence.projector.server.backend.model.User;
import com.bence.projector.server.backend.service.FavouriteSongService;
import com.bence.projector.server.backend.service.StatisticsService;
import com.bence.projector.server.backend.service.UserService;
import com.bence.projector.server.utils.MemoryUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import static com.bence.projector.server.api.resources.StatisticsResource.saveStatistics;
import static com.bence.projector.server.api.resources.util.UserPrincipalUtil.getUserFromPrincipalAndUserService;

@RestController
public class FavouriteSongResource {
    @Autowired
    private StatisticsService statisticsService;
    @Autowired
    private FavouriteSongAssembler favouriteSongAssembler;
    @Autowired
    private UserService userService;
    @Autowired
    private FavouriteSongService favouriteSongService;

    @RequestMapping(value = "/user/api/favouriteSongs", method = RequestMethod.POST)
    public List<FavouriteSongDTO> uploadAndUpdateFavouriteSongs(Principal principal, @RequestBody final List<FavouriteSongDTO> favouriteSongDTOS, HttpServletRequest httpServletRequest) {
        saveStatistics(httpServletRequest, statisticsService);
        User user = getUserFromPrincipalAndUserService(principal, userService);
        if (user != null) {
            List<FavouriteSong> userFavouriteSongs = user.getFavouriteSongs();
            List<FavouriteSong> appliedFavouriteSongs = applyFavouriteSongs(userFavouriteSongs, favouriteSongDTOS, user);
            favouriteSongService.save(appliedFavouriteSongs);
            return favouriteSongAssembler.createDtoList(appliedFavouriteSongs);
        }
        return MemoryUtil.getEmptyList();
    }

    @RequestMapping(value = "/user/api/favouriteSongs/{lastModifiedDate}", method = RequestMethod.GET)
    public List<FavouriteSongDTO> getFavouriteSongs(Principal principal, @PathVariable Long lastModifiedDate) {
        User user = getUserFromPrincipalAndUserService(principal, userService);
        if (user != null) {
            List<FavouriteSong> userFavouriteSongs = favouriteSongService.findAllByUserAndModifiedDateGreaterThan(user, new Date(lastModifiedDate));
            return favouriteSongAssembler.createDtoList(userFavouriteSongs);
        }
        return MemoryUtil.getEmptyList();
    }

    private List<FavouriteSong> applyFavouriteSongs(List<FavouriteSong> userFavouriteSongs, List<FavouriteSongDTO> favouriteSongDTOS, User user) {
        ArrayList<FavouriteSong> appliedFavouriteSongs = new ArrayList<>();
        HashMap<String, FavouriteSong> hashMap = getHashMapBySong(userFavouriteSongs);
        for (FavouriteSongDTO favouriteSongDTO : favouriteSongDTOS) {
            FavouriteSong favouriteSong = createOrUploadFavouriteSong(hashMap, favouriteSongDTO);
            favouriteSong.setUser(user);
            favouriteSong.setServerModifiedDate(new Date());
            appliedFavouriteSongs.add(favouriteSong);
        }
        return appliedFavouriteSongs;
    }

    private FavouriteSong createOrUploadFavouriteSong(HashMap<String, FavouriteSong> hashMap, FavouriteSongDTO favouriteSongDTO) {
        String key = getKey(favouriteSongDTO);
        if (key != null) {
            FavouriteSong favouriteSongFromMap = hashMap.get(key);
            if (favouriteSongFromMap != null) {
                favouriteSongAssembler.updateModel(favouriteSongFromMap, favouriteSongDTO);
                return favouriteSongFromMap;
            }
        }
        return favouriteSongAssembler.createModel(favouriteSongDTO);
    }

    private String getKey(FavouriteSongDTO favouriteSongDTO) {
        if (favouriteSongDTO == null) {
            return null;
        }
        return favouriteSongDTO.getSongUuid();
    }

    private String getKey(FavouriteSong favouriteSong) {
        Song song = favouriteSong.getSong();
        if (song == null) {
            return null;
        }
        return song.getUuid();
    }

    private HashMap<String, FavouriteSong> getHashMapBySong(List<FavouriteSong> userFavouriteSongs) {
        HashMap<String, FavouriteSong> hashMap = new HashMap<>();
        for (FavouriteSong favouriteSong : userFavouriteSongs) {
            hashMap.put(getKey(favouriteSong), favouriteSong);
        }
        return hashMap;
    }
}
