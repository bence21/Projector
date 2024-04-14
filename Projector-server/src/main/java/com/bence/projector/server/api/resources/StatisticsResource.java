package com.bence.projector.server.api.resources;

import com.bence.projector.server.api.assembler.StatisticsAssembler;
import com.bence.projector.server.api.dto.StatisticsDTO;
import com.bence.projector.server.backend.model.Statistics;
import com.bence.projector.server.backend.service.StatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.List;

@RestController
public class StatisticsResource {

    @Autowired
    private StatisticsService statisticsService;
    @Autowired
    private StatisticsAssembler statisticsAssembler;

    static void saveStatistics(HttpServletRequest httpServletRequest, StatisticsService statisticsService) {
        try {
            Statistics statistics = new Statistics();
            statistics.setAccessedDate(new Date());
            statistics.setRemoteAddress(httpServletRequest.getRemoteAddr());
            statistics.setUri(httpServletRequest.getRequestURI());
            statistics.setMethod(httpServletRequest.getMethod());
            statisticsService.save(statistics);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @RequestMapping(method = RequestMethod.GET, value = "/admin/api/statistics")
    public List<StatisticsDTO> findAll(HttpServletRequest httpServletRequest) {
        saveStatistics(httpServletRequest, statisticsService);
        final List<Statistics> all = statisticsService.findAll();
        return statisticsAssembler.createDtoList(all);
    }
}
