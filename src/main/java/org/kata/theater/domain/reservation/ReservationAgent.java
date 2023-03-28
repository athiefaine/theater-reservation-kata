package org.kata.theater.domain.reservation;

import org.kata.theater.ReservationService;
import org.kata.theater.data.Reservation;
import org.kata.theater.domain.allocation.Performance;
import org.kata.theater.domain.allocation.PerformanceAllocation;
import org.kata.theater.domain.allocation.SeatAllocator;
import org.kata.theater.domain.billing.Cashier;
import org.kata.theater.domain.customer.CustomerAccount;
import org.kata.theater.domain.pricing.Amount;

import java.util.List;

public class ReservationAgent {
    private final SeatAllocator seatAllocator;
    private final Cashier cashier;

    public ReservationAgent(SeatAllocator seatAllocator, Cashier cashier) {
        this.seatAllocator = seatAllocator;
        this.cashier = cashier;
    }

    public ReservationRequest reservation(CustomerAccount customerAccount, int reservationCount, String reservationCategory, Performance performance) {
        // Data fetching starts here
        String reservationId = ReservationService.initNewReservation();
        Reservation reservation = new Reservation();
        reservation.setReservationId(Long.parseLong(reservationId));
        reservation.setPerformanceId(performance.getId());
        // Data fetching ends here

        PerformanceAllocation performanceAllocation =
                seatAllocator.allocateSeats(reservationCount, reservationCategory, performance);
        List<ReservationSeat> reservedSeats = performanceAllocation.findSeatsForReservation();

        reservation.setSeats(reservedSeats.stream()
                .map(ReservationSeat::getSeatReference)
                .toArray(String[]::new));

        Amount totalBilling = cashier.calculateBilling(customerAccount, performance, reservedSeats);

        // Data updates start here
        // TODO : introduce a DAO that saves a ReservationRequest in front of ReservationRequest
        // TODO : shouldn't be it saved at the end of the method ?
        ReservationService.updateReservation(reservation);
        // Data updates end here

        return ReservationRequest.builder()
                .reservationId(reservationId)
                .theaterSession(performance.getTheaterSession())
                .reservationCategory(reservationCategory)
                .reservedSeats(reservedSeats)
                .totalBilling(totalBilling)
                .build();
    }

    public void cancelReservation(String reservationId, Long performanceId, List<String> seats) {
        seatAllocator.cancelAllocation(performanceId, seats);
        ReservationService.cancelReservation(Long.parseLong(reservationId));
    }
}
