package com.bence.projector.server.api.resources;

import com.bence.projector.common.dto.SongListDTO;
import com.bence.projector.server.api.assembler.SongListAssembler;
import com.bence.projector.server.backend.model.Song;
import com.bence.projector.server.backend.model.SongList;
import com.bence.projector.server.backend.model.SongListElement;
import com.bence.projector.server.backend.repository.SongRepository;
import com.bence.projector.server.backend.service.SongListService;
import com.bence.projector.server.backend.service.StatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

import static com.bence.projector.server.api.resources.StatisticsResource.saveStatistics;

@RestController
public class SongListResource {
    private final StatisticsService statisticsService;
    private final SongListService songListService;
    private final SongListAssembler songListAssembler;
    private final SongRepository songRepository;

    @Autowired
    public SongListResource(StatisticsService statisticsService, SongListService songListService, SongListAssembler songListAssembler, SongRepository songRepository) {
        this.statisticsService = statisticsService;
        this.songListService = songListService;
        this.songListAssembler = songListAssembler;
        this.songRepository = songRepository;
    }

    @RequestMapping(value = "admin/api/songLists", method = RequestMethod.GET)
    public List<SongListDTO> getSongLists() {
        List<SongList> all = songListService.findAll();
        return songListAssembler.createDtoList(all);
    }

    @RequestMapping(value = "api/songList/{id}", method = RequestMethod.GET)
    public SongListDTO getSongList(@PathVariable final String id, HttpServletRequest httpServletRequest) {
        saveStatistics(httpServletRequest, statisticsService);
        SongList songList = songListService.findOneByUuid(id);
        return songListAssembler.createDto(songList);
    }

    @RequestMapping(value = "api/songList", method = RequestMethod.POST)
    public SongListDTO songList(@RequestBody final SongListDTO songListDTO, HttpServletRequest httpServletRequest) {
        saveStatistics(httpServletRequest, statisticsService);
        SongList songList = songListService.findOneByUuid(songListDTO.getUuid());
        if (songList == null) {
            songList = songListAssembler.createModel(songListDTO);
        } else {
            songListAssembler.updateModel(songList, songListDTO);
        }
        if (songList != null) {
            List<SongListElement> needToRemove = new ArrayList<>();
            List<SongListElement> songListElements = songList.getSongListElements();
            for (SongListElement element : songListElements) {
                Song one = songRepository.findOneByUuid(element.getSongUuid());
                if (one == null) {
                    needToRemove.add(element);
                }
            }
            songListElements.removeAll(needToRemove);
            songListService.save(songList);
        }
        return songListAssembler.createDto(songList);
    }
}
