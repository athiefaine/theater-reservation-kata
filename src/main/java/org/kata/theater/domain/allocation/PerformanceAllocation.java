package org.kata.theater.domain.allocation;

import org.kata.theater.domain.reservation.ReservationSeat;
import org.kata.theater.domain.topology.TheaterTopology;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class PerformanceAllocation {

    private final TheaterTopology theaterTopology;

    private final List<String> freeSeats;


    public PerformanceAllocation(TheaterTopology theaterTopology, List<String> freeSeats) {
        this.theaterTopology = theaterTopology;
        this.freeSeats = freeSeats;
    }


    public int totalSeatCount() {
        return theaterTopology.totalSeatCount();
    }

    public int freeSeatCount() {
        return freeSeats.size();
    }

    public List<ReservationSeat> findSeatsForReservation(int reservationCount, String reservationCategory) {
        return theaterTopology.getRows().stream()
                .map(row -> row.findSeatsForReservation(reservationCount, reservationCategory, freeSeats))
                .filter(Predicate.not(Collection::isEmpty))
                .findFirst()
                .orElse(Collections.emptyList())
                .stream()
                .map(seatTopology -> new ReservationSeat(seatTopology.getSeatReference(), seatTopology.getCategory()))
                .collect(Collectors.toList());
    }
}
