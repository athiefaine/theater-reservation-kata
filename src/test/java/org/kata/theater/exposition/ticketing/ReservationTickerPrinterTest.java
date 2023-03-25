package org.kata.theater.exposition.ticketing;

import org.approvaltests.Approvals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.kata.theater.ReservationService;
import org.kata.theater.TheaterService;
import org.kata.theater.data.Performance;
import org.kata.theater.data.Reservation;

import java.time.LocalDate;
import java.time.Month;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ReservationTickerPrinterTest {

    private ReservationTickerPrinter reservationTickerPrinter;
    private TheaterService theaterService;


    @BeforeEach
    void setUp() {
        theaterService = new TheaterService();
        reservationTickerPrinter = new ReservationTickerPrinter(theaterService);
    }

    @Test
    @Order(5)
    void reserve_once_on_premiere_performance() {
        Performance performance = new Performance();
        performance.id = 1L;
        performance.play = "The CICD by Corneille";
        performance.startTime = LocalDate.of(2023, Month.APRIL, 22).atTime(21, 0);
        performance.performanceNature = "PREMIERE";
        String reservation = reservationTickerPrinter.printReservation(1L, 4, "STANDARD",
                performance);
        Approvals.verify(reservation);

        assertThat(ReservationService.findReservation(123460).getSeats())
                .containsExactly("B3", "B4", "B5", "B6");

    }

    @Test
    @Order(4)
    void reserve_once_on_premiere_performance_with_premium_category() {
        Performance performance = new Performance();
        performance.id = 1L;
        performance.play = "The CICD by Corneille";
        performance.startTime = LocalDate.of(2023, Month.APRIL, 22).atTime(21, 0);
        performance.performanceNature = "PREMIERE";
        String reservation = reservationTickerPrinter.printReservation(1L, 4, "PREMIUM",
                performance);
        Approvals.verify(reservation);

        assertThat(ReservationService.findReservation(123459).getSeats())
                .containsExactly("I3", "I4", "I5", "I6");

    }

    @Test
    @Order(2)
    void cancel_then_reserve_on_premiere_performance_with_standard_category() {
        theaterService.cancelReservation("123456", 1L, List.of("B2"));
        Performance performance = new Performance();
        performance.id = 1L;
        performance.play = "The CICD by Corneille";
        performance.startTime = LocalDate.of(2023, Month.APRIL, 22).atTime(21, 0);
        performance.performanceNature = "PREMIERE";
        String reservation = reservationTickerPrinter.printReservation(1L, 4, "STANDARD",
                performance);
        Approvals.verify(reservation);

        assertThat(ReservationService.findReservation(123457).getSeats())
                .containsExactly("B1", "B2", "B3", "B4");

    }

    @Test
    @Order(6)
    void reserve_twice_on_premiere_performance() {
        Performance performance = new Performance();
        performance.id = 1L;
        performance.play = "The CICD by Corneille";
        performance.startTime = LocalDate.of(2023, Month.APRIL, 22).atTime(21, 0);
        performance.performanceNature = "PREMIERE";
        String reservation1 = reservationTickerPrinter.printReservation(1L, 4, "STANDARD",
                performance);
        String reservation2 = reservationTickerPrinter.printReservation(1L, 5, "STANDARD",
                performance);
        Approvals.verify(reservation2);

        assertThat(ReservationService.findReservation(123461).getSeats())
                .containsExactly("B3", "B4", "B5", "B6");

    }

    @Test
    @Order(3)
    void reservation_failed_on_preview_performance() {
        Performance performance = new Performance();
        performance.id = 2L;
        performance.play = "Les fourberies de Scala - Molière";
        performance.startTime = LocalDate.of(2023, Month.MARCH, 21).atTime(21, 0);
        performance.performanceNature = "PREVIEW";

        String reservation = reservationTickerPrinter.printReservation(2L, 4, "STANDARD",
                performance);
        Approvals.verify(reservation);

        Reservation savedReservation = ReservationService.findReservation(123458);
        assertThat(savedReservation.getSeats()).isEmpty();
        assertThat(savedReservation.getStatus()).isEqualTo("ABORTED");

    }

    @Test
    @Order(1)
    void reservation_failed_on_premiere_performance() {
        Performance performance = new Performance();
        performance.id = 3L;
        performance.play = "DOM JSON - Molière";
        performance.startTime = LocalDate.of(2023, Month.MARCH, 21).atTime(21, 0);
        performance.performanceNature = "PREMIERE";

        String reservation = reservationTickerPrinter.printReservation(2L, 4, "STANDARD",
                performance);
        Approvals.verify(reservation);

        Reservation savedReservation = ReservationService.findReservation(123456);
        assertThat(savedReservation.getSeats()).isEmpty();
        assertThat(savedReservation.getStatus()).isEqualTo("ABORTED");

    }

    @Test
    @Order(7)
    void reserve_once_on_derniere_performance_with_premium_category() {
        Performance performance = new Performance();
        performance.id = 1L;
        performance.play = "The CICD by Corneille";
        performance.startTime = LocalDate.of(2023, Month.APRIL, 22).atTime(21, 0);
        performance.performanceNature = "DERNIERE";
        String reservation = reservationTickerPrinter.printReservation(1L, 4, "PREMIUM",
                performance);
        Approvals.verify(reservation);

        assertThat(ReservationService.findReservation(123463).getSeats())
                .containsExactly("I3", "I4", "I5", "I6");

    }

}
