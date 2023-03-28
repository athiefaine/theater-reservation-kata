package org.kata.theater.domain.allocation;

import lombok.Value;
import org.kata.theater.domain.reservation.TheaterSession;

import java.time.LocalDate;

@Value
public class Performance {

    long id;

    TheaterSession theaterSession;

    PerformanceNature nature;

    public LocalDate date() {
        return getTheaterSession().getStartDateTime().toLocalDate();
    }
}
