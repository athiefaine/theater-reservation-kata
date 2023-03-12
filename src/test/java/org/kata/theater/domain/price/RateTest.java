package org.kata.theater.domain.price;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RateTest {

    @Test
    void add_two_rates() {
        Rate rateAddition = new Rate("0.15").add(new Rate("0.2"));
        assertThat(rateAddition).isEqualTo(new Rate("0.35"));
    }

    @Test
    void multiply_two_rates() {
        Rate rateAddition = new Rate("0.15").multiply(new Rate("0.5"));
        assertThat(rateAddition).isEqualTo(new Rate("0.075"));
    }

    @Test
    void discount_percent() {
        Rate discountPercent = Rate.discountPercent("25");
        assertThat(discountPercent).isEqualTo(new Rate("0.75"));
    }

}
