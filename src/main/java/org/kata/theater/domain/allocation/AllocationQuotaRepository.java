package org.kata.theater.domain.allocation;

public interface AllocationQuotaRepository {

    double allocationQuota(AllocationQuotaCriteria criteria);
}
