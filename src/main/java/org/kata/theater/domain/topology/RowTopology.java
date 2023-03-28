package org.kata.theater.domain.topology;

import lombok.Value;
import org.kata.theater.domain.reservation.Category;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Value
public class RowTopology {

    List<SeatTopology> seats;

    public int seatCount() {
        return seats.size();
    }

    public List<SeatTopology> findSeatsForReservation(int reservationCount, Category reservationCategory, List<String> freeSeats) {
        List<SeatTopology> reservableSeats = new ArrayList<>();
        for (SeatTopology seat : seats) {
            if (isReservable(reservationCategory, freeSeats, seat)) {
                reservableSeats.add(seat);
            } else {
                reservableSeats = new ArrayList<>();
            }
            if (reservableSeats.size() == reservationCount) {
                return reservableSeats;
            }
        }
        return Collections.emptyList();
    }

    private boolean isReservable(Category reservationCategory, List<String> freeSeats, SeatTopology seat) {
        return freeSeats.contains(seat.getSeatReference()) && reservationCategory.equals(seat.getCategory());
    }
}
