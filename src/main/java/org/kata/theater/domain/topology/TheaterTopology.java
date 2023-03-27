package org.kata.theater.domain.topology;

import lombok.Builder;
import lombok.Value;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Value
@Builder
public class TheaterTopology {

    List<RowTopology> rows;

    public List<SeatTopology> allSeats() {
        return rows.stream()
                .map(RowTopology::getSeats)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    public int totalSeatCount() {
        return allSeats().size();
    }

}
