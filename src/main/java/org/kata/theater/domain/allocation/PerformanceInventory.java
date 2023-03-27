package org.kata.theater.domain.allocation;

import java.util.List;

public interface PerformanceInventory {

    List<String> fetchFreeSeatsForPerformance(Performance performance);


    void allocateSeats(PerformanceAllocation performanceAllocation);

    void deallocateSeats(long performanceId, List<String> deallocatedSeats);
}
