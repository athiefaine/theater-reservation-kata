package org.kata.theater.domain.reservation;

import org.kata.theater.ReservationService;
import org.kata.theater.domain.allocation.Performance;
import org.kata.theater.domain.allocation.PerformanceAllocation;
import org.kata.theater.domain.allocation.SeatAllocator;
import org.kata.theater.domain.billing.Cashier;
import org.kata.theater.domain.customer.CustomerAccount;
import org.kata.theater.domain.pricing.Amount;
import org.kata.theater.infra.reservation.ReservationBookAdapter;

import java.util.List;

public class ReservationAgent {
    private final SeatAllocator seatAllocator;
    private final Cashier cashier;
    private final ReservationBookAdapter reservationBook;

    public ReservationAgent(SeatAllocator seatAllocator, Cashier cashier, ReservationBookAdapter reservationBook) {
        this.seatAllocator = seatAllocator;
        this.cashier = cashier;
        this.reservationBook = reservationBook;
    }

    public ReservationRequest reservation(CustomerAccount customerAccount, int reservationCount, String reservationCategory, Performance performance) {

        PerformanceAllocation performanceAllocation =
                seatAllocator.allocateSeats(reservationCount, reservationCategory, performance);
        List<ReservationSeat> reservedSeats = performanceAllocation.findSeatsForReservation();


        Amount totalBilling = cashier.calculateBilling(customerAccount, performance, reservedSeats);
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
        ReservationService.cancelReservation(Long.parseLong(reservationId));
    }
}
