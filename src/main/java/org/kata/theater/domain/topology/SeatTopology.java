package org.kata.theater.domain.topology;

import lombok.Value;
import org.kata.theater.domain.reservation.Category;

@Value
public class SeatTopology {

    String seatReference;

    Category category;
}
