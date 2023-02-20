package org.kata.theater;

// ach, a good old singleton
public class ReservationIdService {

    private static long currentId = 123_455L;
    public static String initNewReservation() {
        currentId++;
        return String.valueOf(currentId);
    }
}
