package com.bence.projector.server.api.resources;

import com.bence.projector.common.dto.BooleanResponse;
import com.bence.projector.common.dto.SongCollectionDTO;
import com.bence.projector.common.dto.SongCollectionElementDTO;
import com.bence.projector.server.api.assembler.SongCollectionAssembler;
import com.bence.projector.server.backend.model.Language;
import com.bence.projector.server.backend.model.Song;
import com.bence.projector.server.backend.model.SongCollection;
import com.bence.projector.server.backend.model.SongCollectionElement;
import com.bence.projector.server.backend.service.LanguageService;
import com.bence.projector.server.backend.service.SongCollectionElementService;
import com.bence.projector.server.backend.service.SongCollectionService;
import com.bence.projector.server.backend.service.SongService;
import com.bence.projector.server.backend.service.StatisticsService;
import com.bence.projector.server.mailsending.ConfigurationUtil;
import com.bence.projector.server.mailsending.FreemarkerConfiguration;
import com.bence.projector.server.mailsending.MailSenderService;
import com.bence.projector.server.utils.AppProperties;
import freemarker.template.Template;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletRequest;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.bence.projector.server.api.resources.StatisticsResource.saveStatistics;

@RestController
public class SongCollectionResource {

    private final SongCollectionService songCollectionService;
    private final SongCollectionElementService songCollectionElementService;
    private final SongCollectionAssembler songCollectionAssembler;
    private final StatisticsService statisticsService;
    private final JavaMailSender sender;
    private final SongService songService;
    private final LanguageService languageService;
    private final MailSenderService mailSenderService;

    @Autowired
    public SongCollectionResource(SongCollectionService songCollectionService, SongCollectionElementService songCollectionElementService, SongCollectionAssembler songCollectionAssembler, StatisticsService statisticsService, JavaMailSender sender, SongService songService, LanguageService languageService, MailSenderService mailSenderService) {
        this.songCollectionService = songCollectionService;
        this.songCollectionElementService = songCollectionElementService;
        this.songCollectionAssembler = songCollectionAssembler;
        this.statisticsService = statisticsService;
        this.sender = sender;
        this.songService = songService;
        this.languageService = languageService;
        this.mailSenderService = mailSenderService;
    }

    @RequestMapping(method = RequestMethod.GET, value = "api/songCollections")
    public List<SongCollectionDTO> findAll(HttpServletRequest httpServletRequest) {
        saveStatistics(httpServletRequest, statisticsService);
        final List<SongCollection> all = songCollectionService.findAll();
        return songCollectionAssembler.createDtoList(all);
    }

    @RequestMapping(method = RequestMethod.GET, value = "api/songCollections/song/{id}")
    public List<SongCollectionDTO> findAllBySong(@PathVariable("id") String songId, HttpServletRequest httpServletRequest) {
        saveStatistics(httpServletRequest, statisticsService);
        Song song = songService.findOneByUuid(songId);
        List<SongCollection> all;
        if (song != null) {
            all = songCollectionService.findAllBySong(song);
            List<SongCollectionDTO> dtoList = new ArrayList<>(all.size());
            for (SongCollection songCollection : all) {
                if (songCollection.getLanguage() == null) {
                    continue;
                }
                SongCollection collection = new SongCollection(songCollection);
                ArrayList<SongCollectionElement> songCollectionElements = new ArrayList<>();
                for (SongCollectionElement collectionElement : songCollection.getSongCollectionElements()) {
                    if (songId.equals(collectionElement.getSongUuid())) {
                        songCollectionElements.add(collectionElement);
                    }
                }
                collection.setSongCollectionElements(songCollectionElements);
                dtoList.add(songCollectionAssembler.createDto(collection));
            }
            return dtoList;
        } else {
            all = new ArrayList<>();
        }
        return songCollectionAssembler.createDtoList(all);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/api/songCollection/{id}")
    public SongCollectionDTO find(@PathVariable final String id, HttpServletRequest httpServletRequest) {
        saveStatistics(httpServletRequest, statisticsService);
        final SongCollection songCollectionDTO = songCollectionService.findOneByUuid(id);
        return songCollectionAssembler.createDto(songCollectionDTO);
    }

    @RequestMapping(method = RequestMethod.GET, value = "api/songCollections/language/{languageUuid}/lastModifiedDate/{lastModifiedDate}")
    public List<SongCollectionDTO> findAllByLanguageAndLastModifiedDate(@PathVariable("languageUuid") String languageUuid, @PathVariable("lastModifiedDate") Long lastModifiedDate, HttpServletRequest httpServletRequest) {
        saveStatistics(httpServletRequest, statisticsService);
        Language language = languageService.findOneByUuid(languageUuid);
        if (language == null) {
            return new ArrayList<>();
        }
        final List<SongCollection> all = songCollectionService.findAllByLanguageAndAndModifiedDateGreaterThan(language, new Date(lastModifiedDate));
        return songCollectionAssembler.createDtoList(all);
    }

    @RequestMapping(method = RequestMethod.GET, value = "api/songCollections/language/{languageUuid}/minimal")
    public List<SongCollectionDTO> findAllByLanguage(@PathVariable("languageUuid") String languageUuid, HttpServletRequest httpServletRequest) {
        Language language = languageService.findOneByUuid(languageUuid);
        if (language == null) {
            return new ArrayList<>();
        }
        final List<SongCollection> songCollections = songCollectionService.findAllByLanguage(language);
        return songCollectionAssembler.createMinimalDtoList(songCollections);
    }

    @RequestMapping(method = RequestMethod.PUT, value = "admin/api/songCollection/{songCollectionUuid}/songCollectionElement")
    public ResponseEntity<Object> addToSongCollection(HttpServletRequest httpServletRequest, @PathVariable String songCollectionUuid, @RequestBody SongCollectionElementDTO elementDTO) {
        return addToSongCollectionByRole(httpServletRequest, songCollectionUuid, elementDTO);
    }

    @RequestMapping(method = RequestMethod.PUT, value = "reviewer/api/songCollection/{songCollectionUuid}/songCollectionElement")
    public ResponseEntity<Object> addToSongCollectionReviewer(HttpServletRequest httpServletRequest, @PathVariable String songCollectionUuid, @RequestBody SongCollectionElementDTO elementDTO) {
        return addToSongCollectionByRole(httpServletRequest, songCollectionUuid, elementDTO);
    }

    private ResponseEntity<Object> addToSongCollectionByRole(HttpServletRequest httpServletRequest, String songCollectionUuid, SongCollectionElementDTO elementDTO) {
        saveStatistics(httpServletRequest, statisticsService);
        final SongCollection songCollection = songCollectionService.findOneByUuid(songCollectionUuid);
        if (songCollection != null) {
            List<SongCollectionElement> songCollectionElements = songCollection.getSongCollectionElements();
            SongCollectionElement elementModel = null;
            for (SongCollectionElement element : songCollectionElements) {
                String songUuid = element.getSongUuid();
                if (songUuid == null) {
                    continue;
                }
                if (songUuid.equals(elementDTO.getSongUuid())) {
                    elementModel = element;
                    break;
                }
            }
            if (elementModel == null) {
                elementModel = songCollectionAssembler.createElementModel(elementDTO);
                elementModel.setSongCollection(songCollection);
                songCollectionElements.add(elementModel);
            } else {
                elementModel.setOrdinalNumber(elementDTO.getOrdinalNumber());
            }
            songCollection.setModifiedDate(new Date());
            songCollectionElementService.save(elementModel);
            songCollectionService.save(songCollection);
            return new ResponseEntity<>(songCollectionAssembler.createDto(songCollection), HttpStatus.ACCEPTED);
        }
        return new ResponseEntity<>("Could not create", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @RequestMapping(method = RequestMethod.DELETE, value = "admin/api/songCollection/{songCollectionUuid}/song/{songUuid}/ordinalNumber/{ordinalNumber}")
    public ResponseEntity<BooleanResponse> deleteSongCollectionElement(@PathVariable String songCollectionUuid, @PathVariable String songUuid, @PathVariable String ordinalNumber) {
        return deleteSongCollectionElementByRole(songCollectionUuid, songUuid, ordinalNumber);
    }

    @RequestMapping(method = RequestMethod.DELETE, value = "reviewer/api/songCollection/{songCollectionUuid}/song/{songUuid}/ordinalNumber/{ordinalNumber}")
    public ResponseEntity<BooleanResponse> deleteSongCollectionElementReviewer(@PathVariable String songCollectionUuid, @PathVariable String songUuid, @PathVariable String ordinalNumber) {
        return deleteSongCollectionElementByRole(songCollectionUuid, songUuid, ordinalNumber);
    }

    private ResponseEntity<BooleanResponse> deleteSongCollectionElementByRole(String songCollectionUuid, String songUuid, String ordinalNumber) {
        BooleanResponse booleanResponse = new BooleanResponse();
        booleanResponse.setResponse(deleteSongCollectionElementByData(songCollectionUuid, songUuid, ordinalNumber));
        return new ResponseEntity<>(booleanResponse, HttpStatus.ACCEPTED);
    }

    private boolean deleteSongCollectionElementByData(String songCollectionUuid, String songUuid, String ordinalNumber) {
        SongCollection songCollection = songCollectionService.findOneByUuid(songCollectionUuid);
        if (songCollection == null || songUuid == null || ordinalNumber == null) {
            return false;
        }
        List<SongCollectionElement> songCollectionElements = songCollection.getSongCollectionElements();
        for (SongCollectionElement songCollectionElement : songCollectionElements) {
            if (ordinalNumber.equals(songCollectionElement.getOrdinalNumber()) && songUuid.equals(songCollectionElement.getSongUuid())) {
                songCollectionElementService.delete(songCollectionElement.getId());
                songCollection.setModifiedDate(new Date());
                songCollectionElements.remove(songCollectionElement);
                songCollectionService.saveWithoutForeign(songCollection);
                return true;
            }
        }
        return false;
    }

    @RequestMapping(method = RequestMethod.POST, value = "admin/api/songCollection")
    public ResponseEntity<Object> createSongCollection(@RequestBody final SongCollectionDTO songCollectionDTO) {
        final SongCollection songCollection = songCollectionAssembler.createModel(songCollectionDTO);
        final SongCollection savedSongCollection = songCollectionService.save(songCollection);
        if (savedSongCollection != null) {
            SongCollectionDTO dto = songCollectionAssembler.createDto(savedSongCollection);
            return new ResponseEntity<>(dto, HttpStatus.ACCEPTED);
        }
        return new ResponseEntity<>("Could not create", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @RequestMapping(method = RequestMethod.POST, value = "/api/songCollection/upload")
    public ResponseEntity<Object> uploadSong(@RequestBody final SongCollectionDTO songCollectionDTO, HttpServletRequest httpServletRequest) {
        saveStatistics(httpServletRequest, statisticsService);
        final SongCollection songCollection = songCollectionAssembler.createModel(songCollectionDTO);
        songCollection.setOriginalId(songCollectionDTO.getUuid());
        songCollection.setDeleted(true);
        songCollection.setUploaded(true);
        final SongCollection savedSongCollection = songCollectionService.save(songCollection);
        if (savedSongCollection != null) {
            Thread thread = new Thread(() -> {
                try {
                    List<SongCollection> songCollections = songCollectionService.findAll();
                    boolean deleted = false;
                    for (SongCollection songCollection1 : songCollections) {
                        if (!savedSongCollection.getId().equals(songCollection.getId()) && songCollectionService.matches(savedSongCollection, songCollection1)) {
                            songCollectionService.delete(savedSongCollection.getId());
                            deleted = true;
                            break;
                        }
                    }
                    if (!deleted) {
                        sendEmail(savedSongCollection);
                    }
                } catch (MessagingException e) {
                    e.printStackTrace();
                }
            });
            thread.start();
            SongCollectionDTO dto = songCollectionAssembler.createDto(savedSongCollection);
            return new ResponseEntity<>(dto, HttpStatus.ACCEPTED);
        }
        return new ResponseEntity<>("Could not create", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private void sendEmail(SongCollection song)
            throws MessagingException, MailSendException {
        final String freemarkerName = FreemarkerConfiguration.COLLECTION_UPDATE + ".ftl";
        freemarker.template.Configuration config = ConfigurationUtil.getConfiguration();
        config.setDefaultEncoding("UTF-8");
        MimeMessage message = sender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        mailSenderService.setToAdmin(helper);
        helper.setFrom(mailSenderService.getNoReplyInternetAddress());
        helper.setSubject("Gyűjtemény frissítése");
        try {
            Template template = config.getTemplate(freemarkerName);

            StringWriter writer = new StringWriter();
            template.process(createPattern(song), writer);

            helper.getMimeMessage().setContent(writer.toString(), "text/html;charset=utf-8");
        } catch (Exception e) {
            e.printStackTrace();
            helper.getMimeMessage().setContent("<div>\n" +
                    "    <h3>Gyűjtemény frissítése: " + song.getName() + "</h3>\n" +
                    "    <a href=\"" + AppProperties.getInstance().baseUrl() + "/api/songCollection/" + song.getId() + "\">Link</a>\n" +
                    "</div>", "text/html;charset=utf-8");
        }
        sender.send(message);
    }

    private Map<String, Object> createPattern(SongCollection songCollection) {
        Map<String, Object> data = new HashMap<>();
        data.put("baseUrl", AppProperties.getInstance().baseUrl());
        data.put("name", songCollection.getName());
        data.put("uuid", songCollection.getId());
        return data;
    }
}
