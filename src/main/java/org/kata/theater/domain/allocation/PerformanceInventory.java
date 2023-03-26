package org.kata.theater.domain.allocation;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class PerformanceInventory {

    int totalSeatsCount;

    int availableSeatsCount;

}
