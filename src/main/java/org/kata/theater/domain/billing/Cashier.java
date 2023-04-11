package org.kata.theater.domain.billing;

import org.kata.theater.domain.allocation.PerformanceAllocation;
import org.kata.theater.domain.customer.CustomerAccount;
import org.kata.theater.domain.customer.SubscriptionProgram;
import org.kata.theater.domain.pricing.Amount;
import org.kata.theater.domain.pricing.PerformanceCatalog;
import org.kata.theater.domain.pricing.Rate;
import org.kata.theater.domain.reservation.ReservationSeat;

import java.util.List;

public class Cashier {

    private final PerformanceCatalog performanceCatalog;
    private final SubscriptionProgram subscriptionProgram;

    public Cashier(PerformanceCatalog performanceCatalog, SubscriptionProgram subscriptionProgram) {
        this.performanceCatalog = performanceCatalog;
        this.subscriptionProgram = subscriptionProgram;
    }

    public Amount calculateBilling(PerformanceAllocation performanceAllocation, CustomerAccount customerAccount) {
        Amount seatBasePrice = performanceCatalog.fetchPerformanceBasePrice(performanceAllocation.getPerformance());
        Rate subscriptionDiscount = subscriptionProgram.fetchSubscriptionDiscount(customerAccount);
        Rate promotionalDiscount = performanceCatalog.fetchPromotionalDiscountRate(performanceAllocation.getPerformance());

        return calculateBasePrice(performanceAllocation.findSeatsForReservation(), seatBasePrice)
                .apply(subscriptionDiscount)
                .apply(promotionalDiscount);
    }

    private Amount calculateBasePrice(List<ReservationSeat> reservedSeats, Amount seatBasePrice) {
        return reservedSeats.stream()
                .map(seat -> seatPrice(seat, seatBasePrice))
                .reduce(Amount::add).orElse(Amount.nothing());
    }

    private Amount seatPrice(ReservationSeat seat, Amount seatBasePrice) {
        Rate seatPriceRatio = performanceCatalog.fetchPriceRatioForSeat(seat);
        return seatBasePrice.apply(seatPriceRatio);
    }
}
