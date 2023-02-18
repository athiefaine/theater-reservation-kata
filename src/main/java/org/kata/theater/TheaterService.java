package org.kata.theater;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;

public class TheaterService {

    // pattern sandwich ?
// agrégats : TheatreTopology, Reservation
// bounded contexts différents : Seat (topology, seat contains category)
// vs Seat (reservation aka "Location", associated with a performance)


    /* business rules :
     * - for finding seats
     *    - not booked seats
     *    - adjacent seats for members of the same reservation (UL: Party)
     *    - if the Performance is PREMIERE, half the seats are set apart for VIP (and not reservable)
     */
    public String reservation(int reservationCount, String reservationCategory, Performance performance) {
        StringBuilder sb = new StringBuilder();
        sb.append("<reservation>\n");
        sb.append("\t<performance>\n");
        sb.append("\t\t<play>").append(performance.play).append("</play>\n");
        sb.append("\t\t<date>").append(performance.startTime.toLocalDate()).append("</date>\n");
        sb.append("\t\t<time>").append(performance.startTime.toLocalTime()).append("</time>\n");
        sb.append("\t</performance>\n");

        // get reservation id
        callDatabaseOrApi("getReservationId");
        String res_id = "123456";
        sb.append("\t<reservationId>").append(res_id).append("</reservationId>\n");

        // get theater topology and all seats status ("reserved", "free") for the performance
        callDatabaseOrApi("theaterTopology", performance);
        TheaterRoom room = fetchRoomMap();

        // find "reservationCount" first contiguous seats in any row
        List<String> foundSeats = new ArrayList<>();
        int remainingSeats = 0;
        int totalSeats = 0;
        boolean foundAllSeats = false;
        for (int i = 0; i < room.getZones().length; i++) {
            Zone zone = room.getZones()[i];
            for (int j = 0; j < zone.getRows().length; j++) {
                Row row = zone.getRows()[j];
                List<String> seatsForRow = new ArrayList<>();
                int streakOfNotReservedSeats = 0;
                for (int k = 0; k < row.getSeats().length; k++) {
                    totalSeats++;
                    Seat aSeat = row.getSeats()[k];
                    if (aSeat.getStatus().equals("FREE")) {
                        remainingSeats++;
                        if (!foundAllSeats) {
                            seatsForRow.add(aSeat.getSeatId());
                            streakOfNotReservedSeats++;
                            if (streakOfNotReservedSeats >= reservationCount) {
                                for (String seat : seatsForRow) {
                                    foundSeats.add(seat);
                                }
                                foundAllSeats = true;
                                remainingSeats -= streakOfNotReservedSeats;
                            }
                        }
                    } else {
                        seatsForRow = new ArrayList<>();
                        streakOfNotReservedSeats = 0;
                    }
                }
            }
        }
        System.out.println(remainingSeats);
        System.out.println(totalSeats);
        if (performance.performanceNature.equals("PREMIERE") && remainingSeats < totalSeats *0.5) {
            foundSeats = new ArrayList<>();
            System.out.println("Not enough VIP seats available for Premiere");
        } else if (performance.performanceNature.equals("PREVIEW") && remainingSeats < totalSeats * 0.9) {
            foundSeats = new ArrayList<>();
            System.out.println("Not enough VIP seats available for Preview");
        }

        if (!foundSeats.isEmpty()) {
            sb.append("\t<reservationStatus>FULFILLED</reservationStatus>\n");
            sb.append("\t\t<seats>\n");
            for (String s : foundSeats) {
                sb.append("\t\t\t<seat>").append(s).append("</seat>\n");
            }
            sb.append("\t\t</seats>\n");
        } else {
            sb.append("\t<reservationStatus>ABORTED</reservationStatus>\n");
        }

        // calculate raw price
        BigDecimal myPrice = fetchPerformancePrice(performance.id);

        BigDecimal intialprice = myPrice.multiply(BigDecimal.valueOf(reservationCount)).setScale(2, RoundingMode.DOWN);

        // check and apply discounts and fidelity program
        callDatabaseOrApi("checkDiscountForDate");

        callDatabaseOrApi("checkCustomerFidelityProgram");

        // est-ce qu'il a un abonnement ou pas ?
        BigDecimal removePercent = new BigDecimal("0.175").setScale(3, RoundingMode.DOWN);
        BigDecimal totalBilling = BigDecimal.ONE.subtract(removePercent).multiply(intialprice);
        String total = totalBilling.setScale(2, RoundingMode.DOWN).toString() + "€";

        // emit reservation summary
        sb.append("\t<seatCategory>").

                append(reservationCategory).

                append("</seatCategory>\n");
        sb.append("\t<totalAmountDue>").

                append(total).

                append("</totalAmountDue>\n");
        sb.append("</reservation>\n");
        return sb.toString();
    }

    // simulates a room map/topology repository
    private static TheaterRoom fetchRoomMap() {
        // ici on sent venir l'utilité forte d'un TestDataBuilder, auquel on devrait passer pour chaque zone :
        // - la liste des préfixes de noms de rangées
        // - la liste du nombre de sièges par rangée
        // - la liste des noms de sièges réservés
        // - ex : Zone.builder()
        //            .withRows ("A", "B", "C", "D", "E", "F", "G")
        //             .withSeatCountPerRow(7, 8, 9, 9, 10, 10, 10)
        //              .withBookedSeats("A1", "A3", "A4", "B2")
        //              .build();

        // remarque : nous ne sommes pas la seule appli à pouvoir booker des places ce qui expliques les trous
        // entre des places réservées
        return new TheaterRoom(
                new Zone[]{new Zone(new Row[]{
                        new Row(new Seat[]{
                                new Seat("A1", "BOOKED"),
                                new Seat("A2", "FREE"),
                                new Seat("A3", "BOOKED"),
                                new Seat("A4", "BOOKED"),
                                new Seat("A5", "FREE"),
                                new Seat("A6", "FREE"),
                                new Seat("A7", "FREE")
                        }),
                        new Row(new Seat[]{
                                new Seat("B1", "FREE"),
                                new Seat("B2", "BOOKED"),
                                new Seat("B3", "FREE"),
                                new Seat("B4", "FREE"),
                                new Seat("B5", "FREE"),
                                new Seat("B6", "FREE"),
                                new Seat("B7", "FREE"),
                                new Seat("B8", "FREE")
                        }),
                        new Row(new Seat[]{
                                new Seat("C1", "FREE"),
                                new Seat("C2", "FREE"),
                                new Seat("C3", "FREE"),
                                new Seat("C4", "FREE"),
                                new Seat("C5", "FREE"),
                                new Seat("C6", "FREE"),
                                new Seat("C7", "FREE"),
                                new Seat("C8", "FREE"),
                                new Seat("C9", "FREE")
                        }),
                        new Row(new Seat[]{
                                new Seat("D1", "FREE"),
                                new Seat("D2", "FREE"),
                                new Seat("D3", "FREE"),
                                new Seat("D4", "FREE"),
                                new Seat("D5", "FREE"),
                                new Seat("D6", "FREE"),
                                new Seat("D7", "FREE"),
                                new Seat("D8", "FREE"),
                                new Seat("D9", "FREE")
                        }),
                        new Row(new Seat[]{
                                new Seat("E1", "FREE"),
                                new Seat("E2", "FREE"),
                                new Seat("E3", "FREE"),
                                new Seat("E4", "FREE"),
                                new Seat("E5", "FREE"),
                                new Seat("E6", "FREE"),
                                new Seat("E7", "FREE"),
                                new Seat("E8", "FREE"),
                                new Seat("E9", "FREE"),
                                new Seat("E10", "FREE")
                        }),
                        new Row(new Seat[]{
                                new Seat("F1", "FREE"),
                                new Seat("F2", "FREE"),
                                new Seat("F3", "FREE"),
                                new Seat("F4", "FREE"),
                                new Seat("F5", "FREE"),
                                new Seat("F6", "FREE"),
                                new Seat("F7", "FREE"),
                                new Seat("F8", "FREE"),
                                new Seat("F9", "FREE"),
                                new Seat("F10", "FREE")
                        }),
                        new Row(new Seat[]{
                                new Seat("G1", "FREE"),
                                new Seat("G2", "FREE"),
                                new Seat("G3", "FREE"),
                                new Seat("G4", "FREE"),
                                new Seat("G5", "FREE"),
                                new Seat("G6", "FREE"),
                                new Seat("G7", "FREE"),
                                new Seat("G8", "FREE"),
                                new Seat("G9", "FREE"),
                                new Seat("G10", "FREE")
                        }
                        )
                })
                });
    }

    // simulates a performance pricing repository
    private static BigDecimal fetchPerformancePrice(long performanceId) {
        if (performanceId == 1L) {
            return new BigDecimal("35.00");
        } else {
            return new BigDecimal("28.50");
        }
    }

    private Object callDatabaseOrApi(String usecase, Object... parameters) {
        return null;
    }

    public static void main(String[] args) {
        Performance performance = new Performance();
        performance.id = 1L;
        performance.play = "The CICD by Corneille";
        performance.startTime = LocalDate.of(2023, Month.APRIL, 22).atTime(21, 0);
        performance.performanceNature = "PREMIERE";
        System.out.println(new TheaterService().reservation(4, "STANDARD",
                performance));

        Performance performance2 = new Performance();
        performance2.id = 2L;
        performance2.play = "Les fourberies de Scala - Molière";
        performance2.startTime = LocalDate.of(2023, Month.MAY, 21).atTime(21, 0);
        performance2.performanceNature = "PREVIEW";
        System.out.println(new TheaterService().reservation(4, "STANDARD",
                performance2));
    }
}