package org.kata.theater.domain.allocation;

public interface AllocationQuotas {

    AllocationQuotaSpecification find(PerformanceNature criteria);
}
