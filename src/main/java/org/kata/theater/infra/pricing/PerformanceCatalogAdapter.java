package org.kata.theater.infra.pricing;

import org.kata.theater.dao.PerformancePriceDao;
import org.kata.theater.dao.VoucherProgramDao;
import org.kata.theater.domain.allocation.Performance;
import org.kata.theater.domain.pricing.Amount;
import org.kata.theater.domain.pricing.PerformanceCatalog;
import org.kata.theater.domain.pricing.Rate;
import org.kata.theater.domain.reservation.ReservationSeat;

import java.math.BigDecimal;

import static org.kata.theater.domain.reservation.Category.STANDARD;

public class PerformanceCatalogAdapter implements PerformanceCatalog {

    private final PerformancePriceDao performancePriceDao = new PerformancePriceDao();

    @Override
    public Amount fetchPerformanceBasePrice(Performance performance) {
        return new Amount(performancePriceDao.fetchPerformancePrice(performance.getId()));
    }

    @Override
    public Rate fetchPriceRatioForSeat(ReservationSeat seat) {
        return STANDARD.equals(seat.getCategory()) ? Rate.fully() : new Rate("1.5");
    }

    @Override
    public Rate fetchPromotionalDiscountRate(Performance performance) {
        BigDecimal promotionalDiscountRate = VoucherProgramDao.fetchVoucherProgram(performance.date());
        return new Rate(promotionalDiscountRate);
    }

}
