package org.kata.theater.domain.pricing;

import org.kata.theater.domain.allocation.Performance;
import org.kata.theater.domain.reservation.ReservationSeat;

public interface PerformanceCatalog {

    Amount fetchPerformanceBasePrice(Performance performance);

    Rate fetchPriceRatioForSeat(ReservationSeat seat);

    Rate fetchPromotionalDiscountRate(Performance performance);
}
