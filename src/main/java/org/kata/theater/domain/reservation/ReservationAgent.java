package org.kata.theater.domain.reservation;

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
    private final ReservationBook reservationBook;

    public ReservationAgent(SeatAllocator seatAllocator, Cashier cashier, ReservationBook reservationBook) {
        this.seatAllocator = seatAllocator;
        this.cashier = cashier;
        this.reservationBook = reservationBook;
    }

    public ReservationRequest reserve(Performance performance, int reservationCount, Category reservationCategory, CustomerAccount customerAccount) {

        PerformanceAllocation performanceAllocation =
                seatAllocator.allocateSeats(reservationCount, reservationCategory, performance);
        List<ReservationSeat> reservedSeats = performanceAllocation.findSeatsForReservation();


        Amount totalBilling = cashier.calculateBilling(performanceAllocation, customerAccount);
        Reservation reservation = reservationBook.registerReservation(performanceAllocation);

        return ReservationRequest.builder()
                .reservationId(reservation.getReservationId())
                .theaterSession(performance.getTheaterSession())
                .reservationCategory(reservationCategory)
                .reservedSeats(reservedSeats)
                .totalBilling(totalBilling)
                .build();
    }

    public void cancelReservation(String reservationId, Long performanceId, List<String> seats) {
        seatAllocator.cancelAllocation(performanceId, seats);
        reservationBook.cancelReservation(reservationId);
    }
}
