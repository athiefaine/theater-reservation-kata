package org.kata.theater.infra.allocation;

import org.kata.theater.dao.TheaterRoomDao;
import org.kata.theater.data.Row;
import org.kata.theater.data.Seat;
import org.kata.theater.data.TheaterRoom;
import org.kata.theater.data.Zone;
import org.kata.theater.domain.allocation.Performance;
import org.kata.theater.domain.allocation.PerformanceAllocation;
import org.kata.theater.domain.allocation.PerformanceInventory;
import org.kata.theater.domain.reservation.ReservationSeat;

import java.util.List;
import java.util.stream.Collectors;

public class PerformanceInventoryAdapter implements PerformanceInventory {

    private final TheaterRoomDao theaterRoomDao = new TheaterRoomDao();

    @Override
    public List<String> fetchFreeSeatsForPerformance(Performance performance) {
        TheaterRoom room = theaterRoomDao.fetchTheaterRoom(performance.getId());
        return room.freeSeats();
    }

    @Override
    public void allocateSeats(PerformanceAllocation performanceAllocation) {
        if (!performanceAllocation.findSeatsForReservation().isEmpty()) {
            theaterRoomDao.saveSeats(performanceAllocation.getPerformance().getId(),
                    performanceAllocation.findSeatsForReservation().stream()
                    .map(ReservationSeat::getSeatReference)
                    .collect(Collectors.toList()), "BOOKING_PENDING");
        }
    }

    @Override
    public void deallocateSeats(long performanceId, List<String> deallocatedSeats) {

        // FIXME : refactor to code more domain-oriented
        TheaterRoom theaterRoom = theaterRoomDao.fetchTheaterRoom(performanceId);
        for (int i = 0; i < theaterRoom.getZones().length; i++) {
            Zone zone = theaterRoom.getZones()[i];
            for (int j = 0; j < zone.getRows().length; j++) {
                Row row = zone.getRows()[j];
                for (int k = 0; k < row.getSeats().length; k++) {
                    Seat seat = row.getSeats()[k];
                    if (deallocatedSeats.contains(seat.getSeatId())) {
                        seat.setStatus("FREE");
                    }
                }
            }
        }
        theaterRoomDao.save(performanceId, theaterRoom);
    }
}
