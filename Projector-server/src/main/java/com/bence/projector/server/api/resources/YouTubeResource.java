package com.bence.projector.server.api.resources;

import com.bence.projector.common.dto.YouTubeApiDTO;
import com.bence.projector.server.utils.AppProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class YouTubeResource {

    @RequestMapping(method = RequestMethod.GET, path = "/api/youtube/youtube_api_key")
    public ResponseEntity<YouTubeApiDTO> getYouTubeAPIKey() {
        YouTubeApiDTO youTubeApiDTO = new YouTubeApiDTO();
        youTubeApiDTO.setYouTubeApiKey(AppProperties.getInstance().getYouTubeAPIKey());
        return new ResponseEntity<>(youTubeApiDTO, HttpStatus.ACCEPTED);
    }
}
