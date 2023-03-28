package org.kata.theater.infra.customer;

import org.kata.theater.dao.CustomerSubscriptionDao;
import org.kata.theater.domain.customer.CustomerAccount;
import org.kata.theater.domain.customer.SubscriptionProgram;
import org.kata.theater.domain.pricing.Rate;

public class SubscriptionProgramAdapter implements SubscriptionProgram {

    CustomerSubscriptionDao customerSubscriptionDao = new CustomerSubscriptionDao();
    @Override
    public Rate fetchSubscriptionDiscount(CustomerAccount customerAccount) {
        boolean isSubscribed = customerSubscriptionDao.fetchCustomerSubscription(customerAccount.getId());
        return isSubscribed ? Rate.discountPercent("17.5") : Rate.fully();
    }
}
