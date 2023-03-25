package org.kata.theater.infra.allocation;

import org.kata.theater.domain.allocation.AllocationQuotaCriteria;
import org.kata.theater.domain.allocation.AllocationQuotaRepository;

public class AllocationQuotaRepositoryAdapter implements AllocationQuotaRepository {
    @Override
    public double allocationQuota(AllocationQuotaCriteria criteria) {
        switch (criteria.getPerformanceNature()) {
            case "PREMIERE":
                return 0.5;
            case "PREVIEW":
                return 0.9;
            default:
                return -1;
        }
    }
}
