package org.kata.theater.data;

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
}
