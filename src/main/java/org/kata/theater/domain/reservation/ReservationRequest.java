package org.kata.theater.domain.reservation;

import org.kata.theater.data.Performance;
import org.kata.theater.domain.price.Amount;

import java.util.List;
import java.util.Map;

public class ReservationRequest {
    private final String reservationCategory;
    private final Performance performance;
    private final String res_id;
    private final List<String> foundSeats;
    private final Map<String, String> seatsCategory;
    private final Amount totalBilling;

    public ReservationRequest(String reservationCategory, Performance performance, String res_id, List<String> foundSeats, Map<String, String> seatsCategory, Amount totalBilling) {
        this.reservationCategory = reservationCategory;
        this.performance = performance;
        this.res_id = res_id;
        this.foundSeats = foundSeats;
        this.seatsCategory = seatsCategory;
        this.totalBilling = totalBilling;
    }

    public String getReservationCategory() {
        return reservationCategory;
    }

    public Performance getPerformance() {
        return performance;
    }

    public String getRes_id() {
        return res_id;
    }

    public List<String> getFoundSeats() {
        return foundSeats;
    }

    public Map<String, String> getSeatsCategory() {
        return seatsCategory;
    }

    public Amount getTotalBilling() {
        return totalBilling;
    }
}
