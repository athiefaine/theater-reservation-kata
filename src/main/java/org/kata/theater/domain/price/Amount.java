package org.kata.theater.domain.price;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public class Amount {

    private final BigDecimal value;

    public Amount(String value) {
        this.value = new BigDecimal(value).setScale(2, RoundingMode.HALF_DOWN);
    }


    public Amount(BigDecimal value) {
        this.value = value.setScale(2, RoundingMode.HALF_DOWN);
    }

    public Amount(Amount other) {
        this.value = BigDecimal.ZERO.add(other.value);
    }

    public Amount apply(Rate rate) {
        return new Amount(this.value.multiply(rate.asBigDecimal()));
    }

    public static Amount nothing() {
        return new Amount(BigDecimal.ZERO);
    }

    public Amount add(Amount other) {
        return new Amount(this.value.add(other.value));
    }

    public BigDecimal asBigDecimal() {
        return value;
    }

    public String asString() {
        return value.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Amount amount = (Amount) o;
        return Objects.equals(value, amount.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return "Amount{" +
                "value=" + value +
                '}';
    }
}
