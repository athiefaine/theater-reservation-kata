package org.kata.theater.domain.allocation;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class PerformanceAllocationStatistics {

    int totalSeatsCount;

    int availableSeatsCount;

}
