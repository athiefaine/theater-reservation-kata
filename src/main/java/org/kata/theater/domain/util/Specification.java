package org.kata.theater.domain.util;

public interface Specification<T> {

    boolean isSatisfiedBy(T t);
}
