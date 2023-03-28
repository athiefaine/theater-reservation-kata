package org.kata.theater.domain.billing;

import org.kata.theater.domain.allocation.Performance;
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

    public Amount calculateBilling(CustomerAccount customerAccount, Performance performance, List<ReservationSeat> reservedSeats) {
        Amount seatBasePrice = performanceCatalog.fetchPerformanceBasePrice(performance);

        Amount totalBilling = reservedSeats.stream()
                .map(seat -> seatPrice(seat, seatBasePrice))
                .reduce(Amount::add).orElse(Amount.nothing());

        Rate subscriptionDiscount = subscriptionProgram.fetchSubscriptionDiscount(customerAccount);
        totalBilling = totalBilling.apply(subscriptionDiscount);

        Rate discount = performanceCatalog.fetchPromotionalDiscountRate(performance);
        Rate discountRatio = Rate.fully().subtract(discount);
        totalBilling = totalBilling.apply(discountRatio);
        return totalBilling;
    }

    private Amount seatPrice(ReservationSeat seat, Amount seatBasePrice) {
        Rate seatPriceRatio = performanceCatalog.fetchPriceRatioForSeat(seat);
        return seatBasePrice.apply(seatPriceRatio);
    }
}
