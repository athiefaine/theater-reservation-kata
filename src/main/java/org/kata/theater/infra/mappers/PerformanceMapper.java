package org.kata.theater.infra.mappers;

import org.kata.theater.data.PerformanceEntity;
import org.kata.theater.domain.allocation.Performance;
import org.kata.theater.domain.allocation.PerformanceNature;
import org.kata.theater.domain.reservation.TheaterSession;

public class PerformanceMapper {


    public Performance entityToBusiness(PerformanceEntity performanceEntity) {
        return new Performance(performanceEntity.id,
                TheaterSession.builder()
                        .title(performanceEntity.play)
                        .startDateTime(performanceEntity.startTime)
                        .endDateTime(performanceEntity.endTime)
                        .build(),
                new PerformanceNature(performanceEntity.performanceNature));
    }
}
