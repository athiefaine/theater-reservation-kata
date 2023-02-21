package org.kata.theater.dao;

import org.kata.theater.data.Reservation;

import java.util.HashMap;
import java.util.Map;

public class ReservationDao {

    private static Map<Long, Reservation> reservationMap = new HashMap<>();
    public void update(Reservation reservation) {
        reservationMap.put(reservation.getReservationId(), reservation);
    }

    public Reservation find(long reservationId) {
        return reservationMap.get(reservationId);
    }
}
