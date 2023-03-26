package org.kata.theater.domain.allocation;

public interface AllocationQuotaRepository {

    AllocationQuotaSpecification allocationQuota(PerformanceNature criteria);
}
