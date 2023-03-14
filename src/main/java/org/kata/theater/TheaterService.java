package org.kata.theater;

import org.kata.theater.dao.CustomerSubscriptionDao;
import org.kata.theater.dao.PerformancePriceDao;
import org.kata.theater.dao.TheaterRoomDao;
import org.kata.theater.dao.VoucherProgramDao;
import org.kata.theater.data.Performance;
import org.kata.theater.data.Reservation;
import org.kata.theater.data.Row;
import org.kata.theater.data.Seat;
import org.kata.theater.data.TheaterRoom;
import org.kata.theater.data.Zone;
import org.kata.theater.domain.price.Amount;
import org.kata.theater.domain.price.Rate;
import org.kata.theater.domain.reservation.ReservationRequest;

import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TheaterService {
    private final TheaterRoomDao theaterRoomDao = new TheaterRoomDao();
    private final PerformancePriceDao performancePriceDao = new PerformancePriceDao();


    public String reservation(long customerId, int reservationCount, String reservationCategory, Performance performance) {


        String reservationId = ReservationService.initNewReservation();
        Reservation reservation = new Reservation();
        reservation.setReservationId(Long.parseLong(reservationId));
        reservation.setPerformanceId(performance.id);

        TheaterRoom room = theaterRoomDao.fetchTheaterRoom(performance.id);

        // find "reservationCount" first contiguous seats in any row
        int remainingSeats = 0;
        int totalSeats = 0;
        boolean foundAllSeats = false;
        List<String> foundSeats = new ArrayList<>();
        Map<String, String> seatsCategory = new HashMap<>();
        for (int i = 0; i < room.getZones().length; i++) {
            Zone zone = room.getZones()[i];
            String zoneCategory = zone.getCategory();
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
                    theaterRoomDao.saveSeats(performance.id, foundSeats, "BOOKING_PENDING");
                }
            }
        }
        reservation.setSeats(foundSeats.toArray(new String[0]));

        if (foundAllSeats) {
            reservation.setStatus("PENDING");
        } else {
            reservation.setStatus("ABORTED");
        }

        ReservationService.updateReservation(reservation);

        if (performance.performanceNature.equals("PREMIERE") && remainingSeats < totalSeats * 0.5) {
            // keep 50% seats for VIP
            foundSeats = new ArrayList<>();
        } else if (performance.performanceNature.equals("PREVIEW") && remainingSeats < totalSeats * 0.9) {
            // keep 10% seats for VIP
            foundSeats = new ArrayList<>();
        }



        // calculate raw price
        Amount rawPrice = Amount.nothing();
        Amount seatBasePrice = new Amount(performancePriceDao.fetchPerformancePrice(performance.id));
        for (String foundSeat : foundSeats) {
            Rate categoryRatio = seatsCategory.get(foundSeat).equals("STANDARD") ? Rate.fully() : new Rate("1.5");
            rawPrice = rawPrice.add(seatBasePrice.apply(categoryRatio));
        }

        // check and apply discounts and fidelity program

        CustomerSubscriptionDao customerSubscriptionDao = new CustomerSubscriptionDao();
        boolean isSubscribed = customerSubscriptionDao.fetchCustomerSubscription(customerId);
        Amount totalBilling = new Amount(rawPrice);
        if (isSubscribed) {
            totalBilling = totalBilling.apply(Rate.discountPercent("17.5"));
        }

        Rate discount = new Rate(VoucherProgramDao.fetchVoucherProgram(LocalDate.now())); // nasty dependency of course
        Rate discountRatio = Rate.fully().subtract(discount);
        totalBilling = totalBilling.apply(discountRatio);

        // TODO : define builder for ReservationRequest
        return toXml(new ReservationRequest(reservationCategory, performance, reservationId, foundSeats, seatsCategory, totalBilling));
    }

    // TODO : move to an exposition layer with something like ReservationTicketPrinter
    private static String toXml(ReservationRequest reservationRequest) {
        StringBuilder sb = new StringBuilder();
        sb.append("<reservation>\n");
        sb.append("\t<performance>\n");
        sb.append("\t\t<play>").append(reservationRequest.performanceTitle()).append("</play>\n");
        sb.append("\t\t<date>").append(reservationRequest.date()).append("</date>\n");
        sb.append("\t\t<time>").append(reservationRequest.time()).append("</time>\n");
        sb.append("\t</performance>\n");
        sb.append("\t<reservationId>").append(reservationRequest.reservationId()).append("</reservationId>\n");
        if (reservationRequest.isFulfillable()) {
            sb.append("\t<reservationStatus>FULFILLABLE</reservationStatus>\n");
            sb.append("\t\t<seats>\n");
            for (String seatReference : reservationRequest.reservedSeats()) {
                // TODO : model seatReference and seatCategory in a same Seat class
                sb.append("\t\t\t<seat>\n");
                sb.append("\t\t\t\t<id>").append(seatReference).append("</id>\n");
                sb.append("\t\t\t\t<category>").append(reservationRequest.seatCategory(seatReference)).append("</category>\n");
                sb.append("\t\t\t</seat>\n");
            }
            sb.append("\t\t</seats>\n");
        } else {
            sb.append("\t<reservationStatus>ABORTED</reservationStatus>\n");
        }
        sb.append("\t<seatCategory>").append(reservationRequest.reservationCategory()).append("</seatCategory>\n");
        sb.append("\t<totalAmountDue>").append(reservationRequest.totalBilling().asString()).append("€").append("</totalAmountDue>\n");
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
