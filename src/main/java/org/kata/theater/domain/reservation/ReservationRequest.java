package org.kata.theater.domain.reservation;

import org.kata.theater.data.Performance;
import org.kata.theater.domain.price.Amount;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class ReservationRequest {
    private final String reservationCategory;
    private final Performance performance;
    private final String reservationId;

    private final List<ReservationSeat> reservedSeats;
    private final Amount totalBilling;

    public ReservationRequest(String reservationCategory, Performance performance, String reservationId, List<ReservationSeat> reservationSeats, Amount totalBilling) {
        this.reservationCategory = reservationCategory;
        this.performance = performance;
        this.reservationId = reservationId;
        this.reservedSeats = reservationSeats;
        this.totalBilling = totalBilling;
    }

    public boolean isFulfillable() {
        return !reservedSeats.isEmpty();
    }

    public String performanceTitle() {
        return getPerformance().play;
    }

    public LocalDate date() {
        return getPerformance().startTime.toLocalDate();
    }

    public LocalTime time() {
        return getPerformance().startTime.toLocalTime();
    }

    public String reservationCategory() {
        return reservationCategory;
    }

    public List<ReservationSeat> reservedSeats() {
        return reservedSeats;
    }

    public Performance getPerformance() {
        return performance;
    }

    public String reservationId() {
        return reservationId;
    }

    public Amount totalBilling() {
        return totalBilling;
    }
}
