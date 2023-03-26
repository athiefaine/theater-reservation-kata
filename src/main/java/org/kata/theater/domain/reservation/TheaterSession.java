package org.kata.theater.domain.reservation;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;

@Value
@Builder
public class TheaterSession {

    String title;

    LocalDateTime startDateTime;

    LocalDateTime endDateTime;

}
