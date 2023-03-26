package org.kata.theater.domain.allocation;

import lombok.Value;
import org.kata.theater.domain.util.Specification;

@Value
public class AllocationQuotaSpecification implements Specification<PerformanceInventory> {
    double shelvingQuota;


    public boolean isSatisfiedBy(PerformanceInventory performanceInventory) {
        return  performanceInventory.getAvailableSeatsCount() >
                performanceInventory.getTotalSeatsCount() * shelvingQuota;
    }

}
