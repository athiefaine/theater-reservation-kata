package org.kata.theater.domain.allocation;

import lombok.Value;
import org.kata.theater.domain.util.Specification;

@Value
public class AllocationQuotaSpecification implements Specification<PerformanceAllocationStatistics> {
    double shelvingQuota;


    public boolean isSatisfiedBy(PerformanceAllocationStatistics performanceAllocationStatistics) {
        return  performanceAllocationStatistics.getAvailableSeatsCount() >
                performanceAllocationStatistics.getTotalSeatsCount() * shelvingQuota;
    }

}
