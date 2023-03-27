package org.kata.theater.domain.reservation;

import org.kata.theater.ReservationService;
import org.kata.theater.dao.CustomerSubscriptionDao;
import org.kata.theater.dao.PerformancePriceDao;
import org.kata.theater.dao.TheaterRoomDao;
import org.kata.theater.dao.VoucherProgramDao;
import org.kata.theater.data.PerformanceEntity;
import org.kata.theater.data.Reservation;
import org.kata.theater.data.Row;
import org.kata.theater.data.Seat;
import org.kata.theater.data.TheaterRoom;
import org.kata.theater.data.Zone;
import org.kata.theater.domain.allocation.AllocationQuotaSpecification;
import org.kata.theater.domain.allocation.AllocationQuotas;
import org.kata.theater.domain.allocation.Performance;
import org.kata.theater.domain.allocation.PerformanceAllocation;
import org.kata.theater.domain.allocation.PerformanceNature;
import org.kata.theater.domain.price.Amount;
import org.kata.theater.domain.price.Rate;
import org.kata.theater.domain.topology.TheaterTopologies;
import org.kata.theater.domain.topology.TheaterTopology;
import org.kata.theater.infra.mappers.PerformanceMapper;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class ReservationAgent {
    private final TheaterRoomDao theaterRoomDao = new TheaterRoomDao();
    private final PerformancePriceDao performancePriceDao = new PerformancePriceDao();

    private final AllocationQuotas allocationQuotas;
    private final TheaterTopologies theaterTopologies;

    public ReservationAgent(AllocationQuotas allocationQuotas, TheaterTopologies theaterTopologies) {
        this.allocationQuotas = allocationQuotas;
        this.theaterTopologies = theaterTopologies;
    }

    public ReservationRequest reservation(long customerId, int reservationCount, String reservationCategory, PerformanceEntity performanceEntity) {
        // Data fetching starts here
        String reservationId = ReservationService.initNewReservation();

        TheaterRoom room = theaterRoomDao.fetchTheaterRoom(performanceEntity.id);
        List<String> freeSeatsRefs = room.freeSeats();

        Performance performance = new PerformanceMapper().entityToBusiness(performanceEntity);
        BigDecimal performancePrice = performancePriceDao.fetchPerformancePrice(performanceEntity.id);
        PerformanceNature performanceNature = new PerformanceNature(performanceEntity.performanceNature);
        AllocationQuotaSpecification allocationQuota = allocationQuotas.find(performanceNature);
        CustomerSubscriptionDao customerSubscriptionDao = new CustomerSubscriptionDao();
        boolean isSubscribed = customerSubscriptionDao.fetchCustomerSubscription(customerId);
        BigDecimal voucherProgramDiscount = VoucherProgramDao.fetchVoucherProgram(LocalDate.now());
        // Data fetching ends here

        Reservation reservation = new Reservation();
        reservation.setReservationId(Long.parseLong(reservationId));
        reservation.setPerformanceId(performanceEntity.id);


        TheaterTopology theaterTopology = theaterTopologies.fetchTopologyForPerformance(performance);
        PerformanceAllocation performanceAllocation =
                new PerformanceAllocation(theaterTopology, freeSeatsRefs,
                        reservationCount, reservationCategory, allocationQuota);

        List<ReservationSeat> reservedSeats = performanceAllocation.findSeatsForReservation();

        reservation.setSeats(reservedSeats.stream()
                .map(ReservationSeat::getSeatReference)
                .toArray(String[]::new));

        // calculate raw price
        Amount rawPrice = Amount.nothing();
        Amount seatBasePrice = new Amount(performancePrice);
        for (ReservationSeat reservedSeat : reservedSeats) {
            Rate categoryRatio = reservedSeat.getCategory().equals("STANDARD") ? Rate.fully() : new Rate("1.5");
            rawPrice = rawPrice.add(seatBasePrice.apply(categoryRatio));
        }

        // check and apply discounts and fidelity program

        Amount totalBilling = new Amount(rawPrice);
        if (isSubscribed) {
            totalBilling = totalBilling.apply(Rate.discountPercent("17.5"));
        }

        Rate discount = new Rate(voucherProgramDiscount); // nasty dependency of course
        Rate discountRatio = Rate.fully().subtract(discount);
        totalBilling = totalBilling.apply(discountRatio);

        // Data updates start here
        if (!reservedSeats.isEmpty()) {
            // TODO :introduce repository that takes a domain object that contains reservedSeats
            // TODO : shouldn't be it saved at the end of the method ?
            theaterRoomDao.saveSeats(performanceEntity.id, reservedSeats.stream()
                    .map(ReservationSeat::getSeatReference)
                    .collect(Collectors.toList()), "BOOKING_PENDING");
        }
        // TODO : introduce a DAO that saves a ReservationRequest in front of ReservationRequest
        // TODO : shouldn't be it saved at the end of the method ?
        ReservationService.updateReservation(reservation);
        // Data updates end here

        return ReservationRequest.builder()
                .reservationId(reservationId)
                .theaterSession(TheaterSession.builder()
                        .title(performanceEntity.play)
                        .startDateTime(performanceEntity.startTime)
                        .endDateTime(performanceEntity.endTime)
                        .build())
                .reservationCategory(reservationCategory)
                .reservedSeats(reservedSeats)
                .totalBilling(totalBilling)
                .build();
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
}