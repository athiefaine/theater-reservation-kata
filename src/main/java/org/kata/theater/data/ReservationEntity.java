package org.kata.theater.data;

import java.util.Arrays;

public class ReservationEntity {

    private Long reservationId;

    private Long performanceId;

    private String[] seats;

    public String getStatus() {
        return getSeats().length > 0 ? "PENDING" : "ABORTED";
    }

    public Long getReservationId() {
        return reservationId;
    }

    public void setReservationId(Long reservationId) {
        this.reservationId = reservationId;
    }

    public Long getPerformanceId() {
        return performanceId;
    }

    public void setPerformanceId(Long performanceId) {
        this.performanceId = performanceId;
    }

    public String[] getSeats() {
        return seats;
    }

    public void setSeats(String[] seats) {
        this.seats = seats;
    }

    @Override
    public String toString() {
        return "Reservation{" +
                "reservationId=" + reservationId +
                ", status='" + getStatus() + '\'' +
                ", seats=" + Arrays.toString(seats) +
                '}';
    }
}
