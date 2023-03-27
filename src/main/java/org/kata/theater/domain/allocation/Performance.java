package org.kata.theater.domain.allocation;

import lombok.Value;
import org.kata.theater.domain.reservation.TheaterSession;

@Value
public class Performance {

    long id;

    TheaterSession theaterSession;

    PerformanceNature nature;
}
