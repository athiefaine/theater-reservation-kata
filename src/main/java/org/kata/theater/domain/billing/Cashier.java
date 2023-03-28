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

        Amount seatBasePrice = performanceCatalog.fetchPerformanceBasePrice(performance);

        // check and apply discounts and fidelity program

        Amount totalBilling = reservedSeats.stream()
                .map(seat -> seatPrice(seat, seatBasePrice))
                .reduce(Amount::add).orElse(Amount.nothing());
        if (isSubscribed) {
            totalBilling = totalBilling.apply(Rate.discountPercent("17.5"));
        }

        Rate discount = new Rate(voucherProgramDiscount); // nasty dependency of course
        Rate discountRatio = Rate.fully().subtract(discount);
        totalBilling = totalBilling.apply(discountRatio);
        return totalBilling;
    }

    private Amount seatPrice(ReservationSeat seat, Amount seatBasePrice) {
        Rate seatPriceRatio = performanceCatalog.fetchPriceRatioForSeat(seat);
        return seatBasePrice.apply(seatPriceRatio);
    }
}