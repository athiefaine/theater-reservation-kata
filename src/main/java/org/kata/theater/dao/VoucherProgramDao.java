package org.kata.theater.dao;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;

public class VoucherProgramDao {
    public static BigDecimal fetchVoucherProgram(LocalDate now) {
        // applies from reservation date, not performance date
        BigDecimal voucher = BigDecimal.ZERO;
        if (now.isBefore(LocalDate.of(2023, Month.APRIL, 30))) {
            voucher = new BigDecimal("0.20");
        }

        return voucher;
    }
}
