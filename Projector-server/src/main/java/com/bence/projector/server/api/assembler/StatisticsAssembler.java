package com.bence.projector.server.api.assembler;

import com.bence.projector.server.api.dto.StatisticsDTO;
import com.bence.projector.server.backend.model.Statistics;
import org.springframework.stereotype.Component;

@Component
public class StatisticsAssembler implements GeneralAssembler<Statistics, StatisticsDTO> {

    @Override
    public StatisticsDTO createDto(Statistics statistics) {
        StatisticsDTO statisticsDTO = new StatisticsDTO();
        statisticsDTO.setAccessedDate(statistics.getAccessedDate());
        statisticsDTO.setMethod(statistics.getMethod());
        statisticsDTO.setRemoteAddress(statistics.getRemoteAddress());
        statisticsDTO.setUri(statistics.getUri());
        return statisticsDTO;
    }

    @Override
    public Statistics createModel(StatisticsDTO statisticsDTO) {
        return updateModel(new Statistics(), statisticsDTO);
    }

    @Override
    public Statistics updateModel(Statistics statistics, StatisticsDTO statisticsDTO) {
        statistics.setAccessedDate(statisticsDTO.getAccessedDate());
        statistics.setMethod(statisticsDTO.getMethod());
        statistics.setRemoteAddress(statisticsDTO.getRemoteAddress());
        statistics.setUri(statisticsDTO.getUri());
        return statistics;
    }
}
