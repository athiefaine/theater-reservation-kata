package org.kata.theater.dao;

import java.math.BigDecimal;

public class PerformancePriceDao {
    // simulates a performance pricing repository
    public BigDecimal fetchPerformancePrice(long performanceId) {
        if (performanceId == 1L) {
            return new BigDecimal("35.00");
        } else {
            return new BigDecimal("28.50");
        }
    }
}
