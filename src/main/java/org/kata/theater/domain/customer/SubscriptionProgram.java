package org.kata.theater.domain.customer;

import org.kata.theater.domain.pricing.Rate;

public interface SubscriptionProgram {

    Rate fetchSubscriptionDiscount(CustomerAccount customerAccount);
}
