package org.kata.theater.domain.allocation;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class AllocationQuotaSpecificationTest {

    @ParameterizedTest(name = "Can allocate with {0} available seats on {1} and shelving quota of {2}? {3}")
    @CsvSource({
            "15, 40, 0.5, false",
            "25, 40, 0.5, true",
            "19, 40, 0.5, false",
            "21, 40, 0.5, true",
            "20, 40, 0.5, false",
            "5, 40, -1, true"
    })
    void allocation_quota_specification(int availableSeatsCount, int totalSeatsCount, double shelvingQuota,
                                        boolean expected) {
        AllocationQuotaSpecification allocationQuotaSpecification = new AllocationQuotaSpecification(shelvingQuota);
        boolean satisfiedBy = allocationQuotaSpecification.isSatisfiedBy(
                PerformanceInventory.builder()
                        .availableSeatsCount(availableSeatsCount)
                        .totalSeatsCount(totalSeatsCount)
                        .build());

        assertThat(satisfiedBy).isEqualTo(expected);
    }

}
