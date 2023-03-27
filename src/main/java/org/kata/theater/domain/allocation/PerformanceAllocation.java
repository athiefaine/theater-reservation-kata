package org.kata.theater.domain.allocation;

import org.kata.theater.domain.reservation.ReservationSeat;
import org.kata.theater.domain.topology.SeatTopology;
import org.kata.theater.domain.topology.TheaterTopology;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class PerformanceAllocation {

    private final TheaterTopology theaterTopology;

    private final List<String> freeSeats;
    private final int requestedSeatCount;
    private final String reservationCategory;
    private final AllocationQuotaSpecification allocationQuota;


    public PerformanceAllocation(TheaterTopology theaterTopology, List<String> freeSeats, int requestedSeatCount,
                                 String reservationCategory, AllocationQuotaSpecification allocationQuota) {
        this.theaterTopology = theaterTopology;
        this.freeSeats = freeSeats;
        this.requestedSeatCount = requestedSeatCount;
        this.reservationCategory = reservationCategory;
        this.allocationQuota = allocationQuota;
    }


    public int totalSeatCount() {
        return theaterTopology.totalSeatCount();
    }

    public int freeSeatCount() {
        return freeSeats.size();
    }

    public List<ReservationSeat> findSeatsForReservation() {
        List<SeatTopology> seatTopologies = theaterTopology.getRows().stream()
                .map(row -> row.findSeatsForReservation(requestedSeatCount, reservationCategory, freeSeats))
                .filter(Predicate.not(Collection::isEmpty))
                .findFirst()
                .orElse(Collections.emptyList());
        if (!allocationQuota.isSatisfiedBy(performanceInventory())) {
            return Collections.emptyList();
        }
        return seatTopologies
                .stream()
                .map(seatTopology -> new ReservationSeat(seatTopology.getSeatReference(), seatTopology.getCategory()))
                .collect(Collectors.toList());
    }

    public PerformanceInventory performanceInventory() {
        return PerformanceInventory.builder()
                .availableSeatsCount(freeSeatCount() - requestedSeatCount)
                .totalSeatsCount(totalSeatCount())
                .build();
    }
}
