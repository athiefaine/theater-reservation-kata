package org.kata.theater.exposition.catalog;

import lombok.Value;

import java.time.LocalDateTime;

@Value
public class PerformanceDto {

    long id;
    String play;

    LocalDateTime startTime;

    LocalDateTime endTime;

    String performanceNature;
}
