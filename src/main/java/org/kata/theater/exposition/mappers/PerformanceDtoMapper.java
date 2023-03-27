package org.kata.theater.exposition.mappers;

import org.kata.theater.domain.allocation.Performance;
import org.kata.theater.domain.allocation.PerformanceNature;
import org.kata.theater.domain.reservation.TheaterSession;
import org.kata.theater.exposition.catalog.PerformanceDto;

public class PerformanceDtoMapper {

    public Performance dtoToBusiness(PerformanceDto performanceDto) {
        return new Performance(performanceDto.getId(),
                TheaterSession.builder()
                        .title(performanceDto.getPlay())
                        .startDateTime(performanceDto.getStartTime())
                        .endDateTime(performanceDto.getEndTime())
                        .build(),
                new PerformanceNature(performanceDto.getPerformanceNature()));
    }
}
