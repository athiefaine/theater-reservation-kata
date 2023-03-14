package org.kata.theater.domain.reservation;

import lombok.Value;

@Value
public class ReservationSeat {

    String seatReference;

    String category;
}
