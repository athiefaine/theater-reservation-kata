package org.kata.theater.domain.reservation;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Reservation {

    private String reservationId;

    private Long performanceId;

    private String[] seats;

}
