package org.kata.theater.infra.allocation;

import org.kata.theater.domain.allocation.PerformanceNature;
import org.kata.theater.domain.allocation.AllocationQuotas;
import org.kata.theater.domain.allocation.AllocationQuotaSpecification;

public class AllocationQuotasAdapter implements AllocationQuotas {
    @Override
    public AllocationQuotaSpecification find(PerformanceNature criteria) {
        double result;
        switch (criteria.getValue()) {
            case "PREMIERE":
                result = 0.5;
                break;
            case "PREVIEW":

                result = 0.9;
                break;
            default:
                result = -1;
                break;
        }
        return new AllocationQuotaSpecification(result);
    }

}
