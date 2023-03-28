package org.kata.theater.infra.mappers;

import org.kata.theater.data.ReservationEntity;
import org.kata.theater.domain.reservation.Reservation;

public class ReservationMapper {

    public ReservationEntity businessToEntity(Reservation reservation) {
        ReservationEntity reservationEntity = new ReservationEntity();
        reservationEntity.setReservationId(Long.parseLong(reservation.getReservationId()));
        reservationEntity.setPerformanceId(reservation.getPerformanceId());
        reservationEntity.setSeats(reservation.getSeats());
        return  reservationEntity;
    }
}
