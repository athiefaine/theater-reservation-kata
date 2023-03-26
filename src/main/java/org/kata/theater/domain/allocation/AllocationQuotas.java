package org.kata.theater.domain.allocation;

public interface AllocationQuotas {

    AllocationQuotaSpecification allocationQuota(PerformanceNature criteria);
}
