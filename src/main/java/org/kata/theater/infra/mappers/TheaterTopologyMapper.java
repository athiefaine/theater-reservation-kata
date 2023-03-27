package org.kata.theater.infra.mappers;

import org.kata.theater.data.TheaterRoom;
import org.kata.theater.domain.topology.RowTopology;
import org.kata.theater.domain.topology.SeatTopology;
import org.kata.theater.domain.topology.TheaterTopology;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

public class TheaterTopologyMapper {

    public TheaterTopology entityToBusiness(TheaterRoom theaterRoom) {
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
