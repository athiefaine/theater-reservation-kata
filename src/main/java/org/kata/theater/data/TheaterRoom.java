package org.kata.theater.data;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class TheaterRoom {

    private Zone[] zones;

    public TheaterRoom(Zone[] zones) {
        this.zones = zones;
    }

    public Zone[] getZones() {
        return zones;
    }

    public void setZones(Zone[] zones) {
        this.zones = zones;
    }

    public List<Seat> allSeats() {
        return Arrays.stream(zones)
                .map(Zone::seats)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    public List<String> freeSeats() {
        return allSeats().stream()
                .filter(Seat::isFree)
                .map(Seat::getSeatId)
                .collect(Collectors.toList());
    }
}
