package org.kata.theater.domain.reservation;

import org.kata.theater.data.Performance;
import org.kata.theater.domain.price.Amount;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

public class ReservationRequest {
    private final String reservationCategory;
    private final Performance performance;
    private final String reservationId;
    private final List<String> foundSeats;
    private final Map<String, String> seatsCategory;
    private final Amount totalBilling;

    public ReservationRequest(String reservationCategory, Performance performance, String reservationId, List<String> foundSeats, Map<String, String> seatsCategory, Amount totalBilling) {
        this.reservationCategory = reservationCategory;
        this.performance = performance;
        this.reservationId = reservationId;
        this.foundSeats = foundSeats;
        this.seatsCategory = seatsCategory;
        this.totalBilling = totalBilling;
    }

    public String seatCategory(String seatReference) {
        return getSeatsCategory().get(seatReference);
    }

    public boolean isFulfillable() {
        return !foundSeats.isEmpty();
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

    public Performance getPerformance() {
        return performance;
    }

    public String reservationId() {
        return reservationId;
    }

    public List<String> reservedSeats() {
        return foundSeats;
    }

    public Map<String, String> getSeatsCategory() {
        return seatsCategory;
    }

    public Amount totalBilling() {
        return totalBilling;
    }
}
