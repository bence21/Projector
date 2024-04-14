package com.bence.projector.server.api.resources;

import com.bence.projector.server.api.siteMap.XmlUrl;
import com.bence.projector.server.api.siteMap.XmlUrlSet;
import com.bence.projector.server.backend.model.Song;
import com.bence.projector.server.backend.service.SongService;
import com.bence.projector.server.utils.AppProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Date;
import java.util.List;

@Controller
public class SiteMapController {
    @Autowired
    private SongService songService;

    @RequestMapping(value = "/sitemap.xml", method = RequestMethod.GET, produces = "application/xml")
    @ResponseBody
    public XmlUrlSet getSitemap() {
        XmlUrlSet xmlUrlSet = new XmlUrlSet();
        List<Song> all = songService.findAll();
        for (Song song : all) {
            if (!song.isDeleted()) {
                create(xmlUrlSet, song.getUuid(), song.getModifiedDate());
            }
        }
        return xmlUrlSet;
    }

    private void create(XmlUrlSet xmlUrlSet, String uuid, Date date) {
        xmlUrlSet.addUrl(new XmlUrl(getCanonicalUrlForSongUuid(uuid), date));
    }

    public static String getCanonicalUrlForSongUuid(String uuid) {
        return AppProperties.getInstance().baseUrl() + "/song/" + uuid;
    }

    public static String getCanonicalUrlForSong(Song song) {
        if (song == null) {
            return "";
        }
        return getCanonicalUrlForSongUuid(song.getUuid());
    }
}
