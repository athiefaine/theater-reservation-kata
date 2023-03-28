package org.kata.theater.domain.reservation;

import lombok.Builder;
import lombok.Value;
import org.kata.theater.domain.pricing.Amount;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Value
@Builder
public class ReservationRequest {
    Category reservationCategory;

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

    public Category reservationCategory() {
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
