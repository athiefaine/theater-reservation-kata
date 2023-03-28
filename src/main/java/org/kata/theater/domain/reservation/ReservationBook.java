package org.kata.theater.domain.reservation;

import org.kata.theater.domain.allocation.PerformanceAllocation;

public interface ReservationBook {

    Reservation registerReservation(PerformanceAllocation performanceAllocation);

}
