package org.kata.theater.domain.allocation;

import org.kata.theater.domain.topology.TheaterTopologies;
import org.kata.theater.domain.topology.TheaterTopology;

import java.util.List;

public class SeatAllocator {

    private final TheaterTopologies theaterTopologies;

    private final AllocationQuotas allocationQuotas;
    private final PerformanceInventory performanceInventory;

    public SeatAllocator(TheaterTopologies theaterTopologies, AllocationQuotas allocationQuotas, PerformanceInventory performanceInventory) {
        this.theaterTopologies = theaterTopologies;
        this.allocationQuotas = allocationQuotas;
        this.performanceInventory = performanceInventory;
    }

    public PerformanceAllocation allocateSeats(int reservationCount, String reservationCategory, Performance performance) {
        List<String> freeSeatsRefs = performanceInventory.fetchFreeSeatsForPerformance(performance);
        AllocationQuotaSpecification allocationQuota = allocationQuotas.find(performance.getNature());
        TheaterTopology theaterTopology = theaterTopologies.fetchTopologyForPerformance(performance);

        PerformanceAllocation performanceAllocation =
                new PerformanceAllocation(performance, theaterTopology, freeSeatsRefs,
                        reservationCount, reservationCategory, allocationQuota);

        performanceInventory.allocateSeats(performanceAllocation);

        return performanceAllocation;
    }

    public void cancelAllocation(Long performanceId, List<String> seats) {
        performanceInventory.deallocateSeats(performanceId, seats);
    }
}
