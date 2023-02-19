package org.kata.theater.dao;

public class CustomerSubscriptionDao {
    // simulates fetching data from Customer advantages
    public boolean fetchCustomerSubscription(long customerId) {
        boolean isSubscribed = false;
        if (customerId == 1L) {
            isSubscribed = true;
        }
        return isSubscribed;
    }
}
