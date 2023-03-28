package org.kata.theater;

import org.kata.theater.dao.CustomerSubscriptionDao;
import org.kata.theater.dao.PerformancePriceDao;
import org.kata.theater.dao.TheaterRoomDao;
import org.kata.theater.dao.VoucherProgramDao;
import org.kata.theater.data.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TheaterService {
    private final TheaterRoomDao theaterRoomDao = new TheaterRoomDao();
    private final PerformancePriceDao performancePriceDao = new PerformancePriceDao();

    boolean debug = false;


    public String reservation(long customerId, int reservationCount, String reservationCategory, Performance performance) {
        Reservation reservation = new Reservation();
        StringBuilder sb = new StringBuilder();
        int bookedSeats = 0;
        List<String> foundSeats = new ArrayList<>();
        Map<String, String> seatsCategory = new HashMap<>();
        String zoneCategory;
        int remainingSeats = 0;
        int totalSeats = 0;
        boolean foundAllSeats = false;

        sb.append("<reservation>\n");
        sb.append("\t<performance>\n");
        sb.append("\t\t<play>").append(performance.play).append("</play>\n");
        sb.append("\t\t<date>").append(performance.startTime.toLocalDate()).append("</date>\n");
        sb.append("\t\t<time>").append(performance.startTime.toLocalTime()).append("</time>\n");
        sb.append("\t</performance>\n");

        String res_id = ReservationService.initNewReservation();
        reservation.setReservationId(Long.parseLong(res_id));
        reservation.setPerformanceId(performance.id);
        sb.append("\t<reservationId>").append(res_id).append("</reservationId>\n");

        TheaterRoom room = theaterRoomDao.fetchTheaterRoom(performance.id);

        // find "reservationCount" first contiguous seats in any row
        for (int i = 0; i < room.getZones().length; i++) {
            Zone zone = room.getZones()[i];
            zoneCategory = zone.getCategory();
            for (int j = 0; j < zone.getRows().length; j++) {
                Row row = zone.getRows()[j];
                List<String> seatsForRow = new ArrayList<>();
                int streakOfNotReservedSeats = 0;
                for (int k = 0; k < row.getSeats().length; k++) {
                    totalSeats++; // devrait être dans une série de boucles différentes mais ça permet qq ns
                    Seat aSeat = row.getSeats()[k];
                    if (!aSeat.getStatus().equals("BOOKED") && !aSeat.getStatus().equals("BOOKING_PENDING")) {
                        remainingSeats++;
                        if (!reservationCategory.equals(zoneCategory)) {
                            continue;
                        }
                        if (!foundAllSeats) {
                            seatsForRow.add(aSeat.getSeatId());
                            streakOfNotReservedSeats++;
                            if (streakOfNotReservedSeats >= reservationCount) {
                                for (String seat : seatsForRow) {
                                    foundSeats.add(seat);
                                    seatsCategory.put(seat, zoneCategory);
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
                if (foundAllSeats) {
                    for (int k = 0; k < row.getSeats().length; k++) {
                        Seat seat = row.getSeats()[k];
                        bookedSeats++;
                        if (foundSeats.contains(seat.getSeatId())) {
                            if (debug) {
                                System.out.println("MIAOU!!! : Seat " + seat.getSeatId() + " will be saved as PENDING");
                            }
                        }
                    }

                    theaterRoomDao.saveSeats(performance.id, foundSeats, "BOOKING_PENDING");
                }
            }
        }
        reservation.setSeats(foundSeats.toArray(new String[0]));

        System.out.println(remainingSeats);
        System.out.println(totalSeats);
        if (foundAllSeats) {
            reservation.setStatus("PENDING");
        } else {
            reservation.setStatus("ABORTED");
        }

        ReservationService.updateReservation(reservation);

        if (performance.performanceNature.equals("PREMIERE") && remainingSeats < totalSeats * 0.5) {
            // keep 50% seats for VIP
            foundSeats = new ArrayList<>();
            System.out.println("Not enough VIP seats available for Premiere");
        } else if (performance.performanceNature.equals("PREVIEW") && remainingSeats < totalSeats * 0.9) {
            // keep 10% seats for VIP
            foundSeats = new ArrayList<>();
            System.out.println("Not enough VIP seats available for Preview");
        }


        if (!foundSeats.isEmpty()) {
            sb.append("\t<reservationStatus>FULFILLABLE</reservationStatus>\n");
            sb.append("\t\t<seats>\n");
            for (String s : foundSeats) {
                sb.append("\t\t\t<seat>\n");
                sb.append("\t\t\t\t<id>").append(s).append("</id>\n");
                sb.append("\t\t\t\t<category>").append(seatsCategory.get(s)).append("</category>\n");
                sb.append("\t\t\t</seat>\n");
            }
            sb.append("\t\t</seats>\n");
        } else {
            sb.append("\t<reservationStatus>ABORTED</reservationStatus>\n");
        }

        BigDecimal adjustedPrice = BigDecimal.ZERO;

        // calculate raw price
        BigDecimal myPrice = performancePriceDao.fetchPerformancePrice(performance.id);

        BigDecimal intialprice = BigDecimal.ZERO.setScale(2, RoundingMode.DOWN);
        for (String foundSeat : foundSeats) {
            BigDecimal categoryRatio = seatsCategory.get(foundSeat).equals("STANDARD") ? BigDecimal.ONE : new BigDecimal("1.5");
            intialprice = intialprice.add(myPrice.multiply(categoryRatio));
        }

        // check and apply discounts and fidelity program
        BigDecimal discountTime = VoucherProgramDao.fetchVoucherProgram(performance.startTime.toLocalDate());

        // has he subscribed or not
        CustomerSubscriptionDao customerSubscriptionDao = new CustomerSubscriptionDao();
        boolean isSubscribed = customerSubscriptionDao.fetchCustomerSubscription(customerId);

        BigDecimal totalBilling = intialprice;
        if (isSubscribed) {
            // apply a 25% discount when the user is subscribed
            BigDecimal removePercent = new BigDecimal("0.175").setScale(3, RoundingMode.DOWN);
            totalBilling = BigDecimal.ONE.subtract(removePercent).multiply(intialprice);
        }
        BigDecimal discountRatio = BigDecimal.ONE.subtract(discountTime);
        String total = totalBilling.multiply(discountRatio).setScale(2, RoundingMode.DOWN).toString() + "€";

        sb.append("\t<seatCategory>").append(reservationCategory).append("</seatCategory>\n");
        sb.append("\t<totalAmountDue>").append(total).append("</totalAmountDue>\n");
        sb.append("</reservation>\n");
        return sb.toString();
    }

    public void cancelReservation(String reservationId, Long performanceId, List<String> seats) {
        TheaterRoom theaterRoom = theaterRoomDao.fetchTheaterRoom(performanceId);
        for (int i = 0; i < theaterRoom.getZones().length; i++) {
            Zone zone = theaterRoom.getZones()[i];
            for (int j = 0; j < zone.getRows().length; j++) {
                Row row = zone.getRows()[j];
                for (int k = 0; k < row.getSeats().length; k++) {
                    Seat seat = row.getSeats()[k];
                    if (seats.contains(seat.getSeatId())) {
                        seat.setStatus("FREE");
                    }
                }
            }
        }
        theaterRoomDao.save(performanceId, theaterRoom);
        ReservationService.cancelReservation(Long.parseLong(reservationId));
    }


    public static void main(String[] args) {
        Performance performance = new Performance();
        performance.id = 1L;
        performance.play = "The CICD by Corneille";
        performance.startTime = LocalDate.of(2023, Month.APRIL, 22).atTime(21, 0);
        performance.performanceNature = "PREMIERE";
        TheaterService theaterService = new TheaterService();
        System.out.println(theaterService.reservation(1L, 4, "STANDARD",
                performance));

        System.out.println(theaterService.reservation(1L, 5, "STANDARD",
                performance));

        Performance performance2 = new Performance();
        performance2.id = 2L;
        performance2.play = "Les fourberies de Scala - Molière";
        performance2.startTime = LocalDate.of(2023, Month.MARCH, 21).atTime(21, 0);
        performance2.performanceNature = "PREVIEW";
        System.out.println(theaterService.reservation(2L, 4, "STANDARD",
                performance2));
    }
}
