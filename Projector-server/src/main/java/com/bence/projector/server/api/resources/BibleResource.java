package com.bence.projector.server.api.resources;

import com.bence.projector.common.dto.BibleDTO;
import com.bence.projector.server.api.assembler.BibleAssembler;
import com.bence.projector.server.backend.model.Bible;
import com.bence.projector.server.backend.service.BibleService;
import com.bence.projector.server.backend.service.StatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

import static com.bence.projector.server.api.resources.StatisticsResource.saveStatistics;

@RestController
public class BibleResource {

    @Autowired
    private BibleService bibleService;
    @Autowired
    private BibleAssembler bibleAssembler;
    @Autowired
    private StatisticsService statisticsService;

    @RequestMapping(method = RequestMethod.GET, value = "/api/bibles")
    public List<BibleDTO> findAll(HttpServletRequest httpServletRequest) {
        saveStatistics(httpServletRequest, statisticsService);
        final List<Bible> all = bibleService.findAll();
        return bibleAssembler.createDtoList(all);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/api/bibleTitles")
    public List<BibleDTO> getAllTitles(HttpServletRequest httpServletRequest) {
        saveStatistics(httpServletRequest, statisticsService);
        final List<Bible> all = bibleService.findAll();
        for (Bible bible : all) {
            bible.setBooks(null);
        }
        return bibleAssembler.createDtoList(all);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/api/bible/{uuid}")
    public BibleDTO getBible(@PathVariable String uuid, HttpServletRequest httpServletRequest) {
        saveStatistics(httpServletRequest, statisticsService);
        final Bible bible = bibleService.findOneByUuid(uuid);
        return bibleAssembler.createDto(bible);
    }

    @RequestMapping(method = RequestMethod.POST, value = "api/bible")
    public ResponseEntity<Object> create(@RequestBody BibleDTO bibleDTO, HttpServletRequest httpServletRequest) {
        saveStatistics(httpServletRequest, statisticsService);
        Bible bible = bibleAssembler.createModel(bibleDTO);
        final Bible savedBible = bibleService.save(bible);
        if (savedBible != null) {
            return new ResponseEntity<>(bibleAssembler.createDto(savedBible), HttpStatus.ACCEPTED);
        }
        return new ResponseEntity<>("Could not create", HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
