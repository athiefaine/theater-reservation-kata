package org.kata.theater.infra.reservation;

import org.kata.theater.ReservationService;
import org.kata.theater.data.ReservationEntity;
import org.kata.theater.domain.allocation.PerformanceAllocation;
import org.kata.theater.domain.reservation.Reservation;
import org.kata.theater.domain.reservation.ReservationBook;
import org.kata.theater.domain.reservation.ReservationSeat;
import org.kata.theater.infra.mappers.ReservationMapper;

public class ReservationBookAdapter implements ReservationBook {

    ReservationMapper reservationMapper = new ReservationMapper();
    @Override
    public Reservation registerReservation(PerformanceAllocation performanceAllocation) {
        String reservationId = ReservationService.initNewReservation();

        Reservation reservation = new Reservation(reservationId,
                performanceAllocation.getPerformance().getId(),
                performanceAllocation.findSeatsForReservation().stream()
                        .map(ReservationSeat::getSeatReference)
                        .toArray(String[]::new));
        ReservationEntity reservationEntity = reservationMapper.businessToEntity(reservation);
        ReservationService.updateReservation(reservationEntity);
        return reservation;
    }
}
