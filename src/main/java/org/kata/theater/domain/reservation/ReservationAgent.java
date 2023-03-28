package org.kata.theater.domain.reservation;

import org.kata.theater.ReservationService;
import org.kata.theater.dao.CustomerSubscriptionDao;
import org.kata.theater.dao.PerformancePriceDao;
import org.kata.theater.dao.VoucherProgramDao;
import org.kata.theater.data.Reservation;
import org.kata.theater.domain.allocation.Performance;
import org.kata.theater.domain.allocation.PerformanceAllocation;
import org.kata.theater.domain.allocation.SeatAllocator;
import org.kata.theater.domain.price.Amount;
import org.kata.theater.domain.price.Rate;

import java.math.BigDecimal;
import java.util.List;

public class ReservationAgent {
    private final PerformancePriceDao performancePriceDao = new PerformancePriceDao();
    private final SeatAllocator seatAllocator;

    public ReservationAgent(SeatAllocator seatAllocator) {
        this.seatAllocator = seatAllocator;
    }

    public ReservationRequest reservation(long customerId, int reservationCount, String reservationCategory, Performance performance) {
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
        // calculate raw price
        BigDecimal performancePrice = performancePriceDao.fetchPerformancePrice(performance.getId());
        CustomerSubscriptionDao customerSubscriptionDao = new CustomerSubscriptionDao();
        boolean isSubscribed = customerSubscriptionDao.fetchCustomerSubscription(customerId);
        BigDecimal voucherProgramDiscount = VoucherProgramDao.fetchVoucherProgram(performance.getTheaterSession().getStartDateTime().toLocalDate());

        Amount rawPrice = Amount.nothing();
        Amount seatBasePrice = new Amount(performancePrice);
        for (ReservationSeat reservedSeat : reservedSeats) {
            Rate categoryRatio = reservedSeat.getCategory().equals("STANDARD") ? Rate.fully() : new Rate("1.5");
            rawPrice = rawPrice.add(seatBasePrice.apply(categoryRatio));
        }

        // check and apply discounts and fidelity program

        Amount totalBilling = new Amount(rawPrice);
        if (isSubscribed) {
            totalBilling = totalBilling.apply(Rate.discountPercent("17.5"));
        }

        Rate discount = new Rate(voucherProgramDiscount); // nasty dependency of course
        Rate discountRatio = Rate.fully().subtract(discount);
        totalBilling = totalBilling.apply(discountRatio);

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
