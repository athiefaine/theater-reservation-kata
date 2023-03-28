package org.kata.theater.domain.billing;

import org.kata.theater.dao.CustomerSubscriptionDao;
import org.kata.theater.dao.VoucherProgramDao;
import org.kata.theater.domain.allocation.Performance;
import org.kata.theater.domain.customer.CustomerAccount;
import org.kata.theater.domain.pricing.Amount;
import org.kata.theater.domain.pricing.PerformanceCatalog;
import org.kata.theater.domain.pricing.Rate;
import org.kata.theater.domain.reservation.ReservationSeat;

import java.math.BigDecimal;
import java.util.List;

public class Cashier {

    private final PerformanceCatalog performanceCatalog;

    public Cashier(PerformanceCatalog performanceCatalog) {
        this.performanceCatalog = performanceCatalog;
    }

    public Amount calculateBilling(CustomerAccount customerAccount, Performance performance, List<ReservationSeat> reservedSeats) {
        // calculate raw price
        CustomerSubscriptionDao customerSubscriptionDao = new CustomerSubscriptionDao();
        boolean isSubscribed = customerSubscriptionDao.fetchCustomerSubscription(customerAccount.getId());
        BigDecimal voucherProgramDiscount = VoucherProgramDao.fetchVoucherProgram(performance.getTheaterSession().getStartDateTime().toLocalDate());

        Amount rawPrice = Amount.nothing();
        Amount seatBasePrice = performanceCatalog.fetchPerformanceBasePrice(performance);
        for (ReservationSeat reservedSeat : reservedSeats) {
            Rate seatPriceRatio = performanceCatalog.fetchPriceRatioForSeat(reservedSeat);
            rawPrice = rawPrice.add(seatBasePrice.apply(seatPriceRatio));
        }

        // check and apply discounts and fidelity program

        Amount totalBilling = new Amount(rawPrice);
        if (isSubscribed) {
            totalBilling = totalBilling.apply(Rate.discountPercent("17.5"));
        }

        Rate discount = new Rate(voucherProgramDiscount); // nasty dependency of course
        Rate discountRatio = Rate.fully().subtract(discount);
        totalBilling = totalBilling.apply(discountRatio);
        return totalBilling;
    }
}
