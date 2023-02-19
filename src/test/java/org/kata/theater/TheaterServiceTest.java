package org.kata.theater;

import org.approvaltests.Approvals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.Month;

class TheaterServiceTest {

    private TheaterService theaterService;

    @BeforeEach
    void setUp() {
        theaterService = new TheaterService();
    }

    @Test
    void reserve_once_on_premiere_performance() {
        Performance performance = new Performance();
        performance.id = 1L;
        performance.play = "The CICD by Corneille";
        performance.startTime = LocalDate.of(2023, Month.APRIL, 22).atTime(21, 0);
        performance.performanceNature = "PREMIERE";
        String reservation = theaterService.reservation(1L, 4, "STANDARD",
                performance);
        Approvals.verify(reservation);
    }

    @Test
    void reserve_twice_on_premiere_performance() {
        Performance performance = new Performance();
        performance.id = 1L;
        performance.play = "The CICD by Corneille";
        performance.startTime = LocalDate.of(2023, Month.APRIL, 22).atTime(21, 0);
        performance.performanceNature = "PREMIERE";
        String reservation1 = theaterService.reservation(1L, 4, "STANDARD",
                performance);
        String reservation2 = theaterService.reservation(1L, 5, "STANDARD",
                performance);
        Approvals.verify(reservation2);
    }

    @Test
    void reservation_failed_on_preview_performance() {
        Performance performance = new Performance();
        performance.id = 2L;
        performance.play = "Les fourberies de Scala - Molière";
        performance.startTime = LocalDate.of(2023, Month.MARCH, 21).atTime(21, 0);
        performance.performanceNature = "PREVIEW";

        String reservation = theaterService.reservation(2L, 4, "STANDARD",
                performance);
        Approvals.verify(reservation);
    }

}