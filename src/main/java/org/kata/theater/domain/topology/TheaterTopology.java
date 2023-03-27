package org.kata.theater.domain.topology;

import lombok.Builder;
import lombok.Value;
import org.kata.theater.data.TheaterRoom;

import java.util.Arrays;
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

    public static TheaterTopology from(TheaterRoom theaterRoom) {
        return TheaterTopology.builder()
                .rows(Arrays.stream(theaterRoom.getZones())
                        .map(zone -> Arrays.stream(zone.getRows())
                                .map(row -> new RowTopology(Arrays.stream(row.getSeats())
                                        .map(seat -> new SeatTopology(seat.getSeatId(), zone.getCategory()))
                                        .collect(Collectors.toList())))
                                .collect(Collectors.toList()))
                        .flatMap(Collection::stream)
                        .collect(Collectors.toList()))
                .build();
    }
}
