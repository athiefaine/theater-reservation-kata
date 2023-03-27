package org.kata.theater.exposition.ticketing;

import org.kata.theater.domain.allocation.Performance;
import org.kata.theater.domain.reservation.ReservationAgent;
import org.kata.theater.domain.reservation.ReservationRequest;
import org.kata.theater.domain.reservation.ReservationSeat;
import org.kata.theater.exposition.catalog.PerformanceDto;
import org.kata.theater.exposition.mappers.PerformanceDtoMapper;

public class ReservationTickerPrinter {


    private final ReservationAgent reservationAgent;

    private final PerformanceDtoMapper performanceDtoMapper;

    public ReservationTickerPrinter(ReservationAgent reservationAgent, PerformanceDtoMapper performanceDtoMapper) {
        this.reservationAgent = reservationAgent;
        this.performanceDtoMapper = performanceDtoMapper;
    }

    public String printReservation(long customerId, int reservationCount, String reservationCategory, PerformanceDto performanceDto) {
        Performance performance = performanceDtoMapper.dtoToBusiness(performanceDto);
        ReservationRequest reservationRequest = reservationAgent.reservation(customerId, reservationCount, reservationCategory, performance);
        return printXml(reservationRequest);
    }


    private String printXml(ReservationRequest reservationRequest) {
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
            for (ReservationSeat reservedSeat : reservationRequest.reservedSeats()) {
                sb.append("\t\t\t<seat>\n");
                sb.append("\t\t\t\t<id>").append(reservedSeat.getSeatReference()).append("</id>\n");
                sb.append("\t\t\t\t<category>").append(reservedSeat.getCategory()).append("</category>\n");
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
}
