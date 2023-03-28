package org.kata.theater.domain.price;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AmountRateArithmeticTest {


    @Test
    void apply_discount_rate_to_amount() {
        Amount discountOnAmount = new Amount("25.99").apply(Rate.discountPercent("20"));
        assertThat(discountOnAmount).isEqualTo(new Amount("20.79"));
    }

}
