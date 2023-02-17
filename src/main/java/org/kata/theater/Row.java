package org.kata.theater;

public class Row {

    private Seat[] seats;

    public Row(Seat[] seats) {
        this.seats = seats;
    }

    public Seat[] getSeats() {
        return seats;
    }

    public void setSeats(Seat[] seats) {
        this.seats = seats;
    }
}
