package org.kata.theater;

import org.kata.theater.dao.ReservationDao;
import org.kata.theater.data.ReservationEntity;

// ach, a good old singleton
public class ReservationService {

    private static long currentId = 123_455L;
    public static String initNewReservation() {
        currentId++;
        return String.valueOf(currentId);
    }

    public static void updateReservation(ReservationEntity reservationEntity) {
        new ReservationDao().update(reservationEntity);
    }

    public static ReservationEntity findReservation(long reservationId) {
        return new ReservationDao().find(reservationId);
    }

    public static void cancelReservation(long reservationId) {
        ReservationEntity reservationEntity = new ReservationDao().find(reservationId);
        reservationEntity.setSeats(new String[0]);
        new ReservationDao().update(reservationEntity);
    }
}
