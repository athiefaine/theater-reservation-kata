package org.kata.theater;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.List;

public class TheaterService {
    // pattern sandwich ?
// agrégats : TheatreTopology, Reservation
// bounded contexts différents : Seat (topology, seat contains category)
// vs Seat (reservation aka "Location", associated with a performance)

    public String reservation(int reservationCount, String reservationCategory) {
        StringBuilder sb = new StringBuilder();
        sb.append("<reservation>\n");

        // get reservation id
        callDatabaseOrApi("getReservationId");
        String res_id = "123456";
        sb.append("\t<reservationId>").append(res_id).append("</reservationId>\n");

        // get seats (based on avaialble, count and category)
        callDatabaseOrApi("availablesSeats");

        // algo
        List<String> seatNames = Arrays.asList("1A", "2A", "3A", "4A");
        sb.append("\t<seats>\n");
        for (String s : seatNames) {
            sb.append("\t\t<seat>").append(s).append("</seat>\n");
        }

        // calculate raw price

        BigDecimal intialprice = new BigDecimal("150.00").setScale(2, RoundingMode.DOWN);

        // check and apply discounts and fidelity program
        callDatabaseOrApi("checkDiscountForDate");
        callDatabaseOrApi("checkCustomerFidelityProgram");

        // est-ce qu'il a un abonnement ou pas ?
        BigDecimal removePercent = new BigDecimal("0.175").setScale(3, RoundingMode.DOWN);
        BigDecimal totalBilling = BigDecimal.ONE.subtract(removePercent).multiply(intialprice);
        String total = totalBilling.setScale(2, RoundingMode.DOWN).toString() + "€";

        // emit reservation summary
        sb.append("\t<seatCategory>").append(reservationCategory).append("</seatCategory>\n");
        sb.append("\t</seats>\n");
        sb.append("\t<totalAmountDue>").append(total).append("</totalAmountDue>\n");
        sb.append("</reservation>\n");
        return sb.toString();
    }

    private Object callDatabaseOrApi(String usecase) {
        return null;
    }

    public static void main(String[] args) {
        System.out.println(new TheaterService().reservation(4, "STANDARD"));
    }
}