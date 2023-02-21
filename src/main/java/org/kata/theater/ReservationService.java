package org.kata.theater;

import org.kata.theater.dao.ReservationDao;
import org.kata.theater.data.Reservation;

// ach, a good old singleton
public class ReservationService {

    private static long currentId = 123_455L;
    public static String initNewReservation() {
        currentId++;
        return String.valueOf(currentId);
    }

    public static void updateReservation(Reservation reservation) {
        new ReservationDao().update(reservation);
    }

    public static Reservation findReservation(long reservationId) {
        return new ReservationDao().find(reservationId);
    }

    public static void cancelReservation(long reservationId) {
        Reservation reservation = new ReservationDao().find(reservationId);
        reservation.setStatus("CANCELLED");
        reservation.setSeats(new String[0]);
        new ReservationDao().update(reservation);
    }
}
