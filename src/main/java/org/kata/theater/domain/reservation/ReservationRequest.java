package org.kata.theater.domain.reservation;

import lombok.Builder;
import lombok.Value;
import org.kata.theater.domain.price.Amount;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Value
@Builder
public class ReservationRequest {
    String reservationCategory;

    TheaterSession theaterSession;
    String reservationId;

    List<ReservationSeat> reservedSeats;
    Amount totalBilling;

    public boolean isFulfillable() {
        return !reservedSeats.isEmpty();
    }

    public String performanceTitle() {
        return theaterSession.getTitle();
    }

    public LocalDate date() {
        return theaterSession.getStartDateTime().toLocalDate();
    }

    public LocalTime time() {
        return theaterSession.getStartDateTime().toLocalTime();
    }

    public String reservationCategory() {
        return reservationCategory;
    }

    public List<ReservationSeat> reservedSeats() {
        return reservedSeats;
    }


    public String reservationId() {
        return reservationId;
    }

    public Amount totalBilling() {
        return totalBilling;
    }
}
