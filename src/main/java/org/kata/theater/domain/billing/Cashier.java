package org.kata.theater.domain.billing;

import org.kata.theater.dao.CustomerSubscriptionDao;
import org.kata.theater.dao.PerformancePriceDao;
import org.kata.theater.dao.VoucherProgramDao;
import org.kata.theater.domain.allocation.Performance;
import org.kata.theater.domain.customer.CustomerAccount;
import org.kata.theater.domain.price.Amount;
import org.kata.theater.domain.price.Rate;
import org.kata.theater.domain.reservation.ReservationSeat;

import java.math.BigDecimal;
import java.util.List;

public class Cashier {

    // FIXME : use repository
    private final PerformancePriceDao performancePriceDao;

    public Cashier(PerformancePriceDao performancePriceDao) {
        this.performancePriceDao = performancePriceDao;
    }

    public Amount calculateBilling(CustomerAccount customerAccount, Performance performance, List<ReservationSeat> reservedSeats) {
        // calculate raw price
        BigDecimal performancePrice = performancePriceDao.fetchPerformancePrice(performance.getId());
        CustomerSubscriptionDao customerSubscriptionDao = new CustomerSubscriptionDao();
        boolean isSubscribed = customerSubscriptionDao.fetchCustomerSubscription(customerAccount.getId());
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
        return totalBilling;
    }
}
