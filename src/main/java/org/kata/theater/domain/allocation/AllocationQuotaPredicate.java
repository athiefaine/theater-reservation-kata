package org.kata.theater.domain.allocation;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class AllocationQuotaPredicate {

    int totalSeatsCount;
    int availableSeatsCount;
    double shelvingQuota;

    public boolean canReserve() {
        return availableSeatsCount > shelvedSeatsCount();
    }

    private double shelvedSeatsCount() {
        return totalSeatsCount * shelvingQuota;
    }
}
