package org.kata.theater.domain.price;

import org.junit.jupiter.api.Test;
import org.kata.theater.domain.pricing.Amount;

import static org.assertj.core.api.Assertions.assertThat;

class AmountTest {


    @Test
    void add_two_amounts() {
        Amount amountAddition = new Amount("25.99").add(new Amount("59.99"));
        assertThat(amountAddition).isEqualTo(new Amount("85.98"));
    }

    @Test
    void amount_nothing_is_addition_neutral_element() {
        Amount amount = new Amount("25.99");
        Amount amountAddition = amount.add(Amount.nothing());
        assertThat(amountAddition).isEqualTo(amount);
    }

}
