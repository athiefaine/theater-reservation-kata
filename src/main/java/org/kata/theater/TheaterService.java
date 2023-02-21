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

    // pattern sandwich ?
// agrégats : TheatreTopology, Reservation
// bounded contexts différents : Seat (topology, seat contains category)
// vs Seat (reservation aka "Location", associated with a performance)


    /* business rules :
     * - for finding seats
     *    - not booked seats
     *    - adjacent seats for members of the same reservation (UL: Party)
     *    - if the Performance is PREMIERE, half the seats are set apart for VIP (and not reservable)
     *    - if the Performance is PREVIEW, 90% of the seats are set apart for VIP (and not reservable)
     * - customer can have a subscription program allowing a 17.5% discount
     */
    /* règle supplémentaires
     * - pricing différent en fonction de la zone (balcon, aile, centre etc.)
     *    - ou alors chaque siège a sa catégorie de pricing ?
     *    - en terme de tell don't ask, c'est le Seat qui expose sa catégorie de pricing (peu importe si c'est
     *       une info portée par le seat ou si ça dépend de la zone dans laquelle le seat est ?
     *  => ça peut servir à illustrer la notion d'ACL et de upstream/downstream
     * - upstream : ils modélisent la category pricing par zone mais ils vont faire une v2 de leur modèle avec
     *     un pricing individuel par Seat (et on a de la chance ils nous préviennent à l'avance ;-) )
     * - downstream : ce qui nous intéresse c'est le pricing de chaque siège, donc autant avoir une ACL qui pour
     *    l'instant le dérive depuis la zone et après coup le prendra directement du Seat
     */

    /*
     * pour le pricing pur :
     * - BC Catalogue : performances avec date, pièce, categoryRepresentation (BC Marketing plutôt ?) et salle
     * - BC Topology : catégorie de Seat
     * - BC Reservation : attribution des Seats, et l'état actuel de réservation de chaque Seat
     * - BC Pricing : prix de base pour la Performance (soit prix de base  + ratio ou somme forfaitaire par catégorie de Seat)
     * - BC Marketing : applique discount (ratio et/ou réduction forfaitaire)
     *       - paying subscription (17.5% de discount)
     *       - campagne temporaire offrant 20% à partir de 4 personnes
     *    - design cible : on envoie le pricing brut au BC Marketing qui nous renvoie un pricing ajusté
     *    - design cracra de départ : tout est au même endroit
     */


    public String reservation(long customerId, int reservationCount, String reservationCategory, Performance performance) {
        Reservation reservation = new Reservation();
        StringBuilder sb = new StringBuilder();
        sb.append("<reservation>\n"); // should be named ReservationRequest
        sb.append("\t<performance>\n");
        sb.append("\t\t<play>").append(performance.play).append("</play>\n");
        sb.append("\t\t<date>").append(performance.startTime.toLocalDate()).append("</date>\n");
        sb.append("\t\t<time>").append(performance.startTime.toLocalTime()).append("</time>\n");
        sb.append("\t</performance>\n");

        // get reservation id, notion entité/agrégat ? (faire un objet plus complexe ?)
        String res_id = ReservationService.initNewReservation();
        reservation.setReservationId(Long.parseLong(res_id));
        reservation.setPerformanceId(performance.id);
        sb.append("\t<reservationId>").append(res_id).append("</reservationId>\n");

        TheaterRoom room = theaterRoomDao.fetchTheaterRoom(performance.id);

        // find "reservationCount" first contiguous seats in any row
        List<String> foundSeats = new ArrayList<>();
        Map<String, String> seatsCategory = new HashMap<>();
        String zoneCategory;
        int remainingSeats = 0;
        int totalSeats = 0; // devrait être porté par l'agrégat TheaterRoom, pour illustrer le Tell/Don't Ask
        boolean foundAllSeats = false;
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
                            // TODO : changer l'état du seat à "BOOKED"
                            // boite de pandore agrégat/entity/value object
                            // au global, notion de ReservationRequest/ReservationAttempt
                            // état FREE ou BOOKING_PENDING/BOOKED devrait être dérivé à partir d'un bookingRef porté par le l'entity Seat
                            // BC Topology : Seat = ValueObject
                            // BC Booking/Reservation : Seat = Entity, la partie mouvante c'est la bookingRef
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
                        if (foundSeats.contains(seat.getSeatId())) {
                            seat.setStatus("BOOKING_PENDING");
                        }
                    }
                    theaterRoomDao.save(performance.id, room);
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
        // en vrai ce qui suit : BC Marketing
        // il le renvoie dans une Map<PerformanceCategory, VIPRate>
        if (performance.performanceNature.equals("PREMIERE") && remainingSeats < totalSeats * 0.5) {
            // TODO : branche pas couverte
            foundSeats = new ArrayList<>();
            System.out.println("Not enough VIP seats available for Premiere");
        } else if (performance.performanceNature.equals("PREVIEW") && remainingSeats < totalSeats * 0.9) {
            foundSeats = new ArrayList<>();
            System.out.println("Not enough VIP seats available for Preview");
        }

        // FULFILLABLE, ABORTED : on a un worklfow implicite
        // workflow : par Entity ou succession de ValueObjects
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

        // calculate raw price
        BigDecimal myPrice = performancePriceDao.fetchPerformancePrice(performance.id);

        BigDecimal intialprice = BigDecimal.ZERO.setScale(2, RoundingMode.DOWN);
        for (String foundSeat : foundSeats) {
            BigDecimal categoryRatio = seatsCategory.get(foundSeat).equals("STANDARD") ? BigDecimal.ONE : new BigDecimal("1.5");
            intialprice = intialprice.add(myPrice.multiply(categoryRatio));
        }

        // check and apply discounts and fidelity program
        BigDecimal discountTime = VoucherProgramDao.fetchVoucherProgram(LocalDate.now()); // nasty dependency of course

        // est-ce qu'il a un abonnement ou pas ?
        CustomerSubscriptionDao customerSubscriptionDao = new CustomerSubscriptionDao();
        boolean isSubscribed = customerSubscriptionDao.fetchCustomerSubscription(customerId);

        BigDecimal totalBilling = intialprice;
        if (isSubscribed) {
            BigDecimal removePercent = new BigDecimal("0.175").setScale(3, RoundingMode.DOWN);
            totalBilling = BigDecimal.ONE.subtract(removePercent).multiply(intialprice);
        }
        BigDecimal discountRatio = BigDecimal.ONE.subtract(discountTime);
        String total = totalBilling.multiply(discountRatio).setScale(2, RoundingMode.DOWN).toString() + "€";
        // € ou $ => BC Billing, en dehors c'est la banque qui fait la conversion

        // emit reservation summary
        // pas d'agrégat métier, juste une string
        sb.append("\t<seatCategory>").append(reservationCategory).append("</seatCategory>\n");
        sb.append("\t<totalAmountDue>").append(total).append("</totalAmountDue>\n");
        sb.append("</reservation>\n");
        return sb.toString();
    }

    // TODO : implement Reservation + ReservationDAO
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
