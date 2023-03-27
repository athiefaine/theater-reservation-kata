package org.kata.theater.domain.reservation;

import org.kata.theater.ReservationService;
import org.kata.theater.dao.CustomerSubscriptionDao;
import org.kata.theater.dao.PerformancePriceDao;
import org.kata.theater.dao.VoucherProgramDao;
import org.kata.theater.data.PerformanceEntity;
import org.kata.theater.data.Reservation;
import org.kata.theater.domain.allocation.AllocationQuotaSpecification;
import org.kata.theater.domain.allocation.AllocationQuotas;
import org.kata.theater.domain.allocation.Performance;
import org.kata.theater.domain.allocation.PerformanceAllocation;
import org.kata.theater.domain.allocation.PerformanceInventory;
import org.kata.theater.domain.allocation.PerformanceNature;
import org.kata.theater.domain.price.Amount;
import org.kata.theater.domain.price.Rate;
import org.kata.theater.domain.topology.TheaterTopologies;
import org.kata.theater.domain.topology.TheaterTopology;
import org.kata.theater.infra.mappers.PerformanceMapper;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class ReservationAgent {
    private final PerformancePriceDao performancePriceDao = new PerformancePriceDao();

    private final AllocationQuotas allocationQuotas;
    private final TheaterTopologies theaterTopologies;

    private final PerformanceInventory performanceInventory;

    public ReservationAgent(AllocationQuotas allocationQuotas, TheaterTopologies theaterTopologies, PerformanceInventory performanceInventory) {
        this.allocationQuotas = allocationQuotas;
        this.theaterTopologies = theaterTopologies;
        this.performanceInventory = performanceInventory;
    }

    public ReservationRequest reservation(long customerId, int reservationCount, String reservationCategory, PerformanceEntity performanceEntity) {
        // Data fetching starts here
        String reservationId = ReservationService.initNewReservation();
        Reservation reservation = new Reservation();
        reservation.setReservationId(Long.parseLong(reservationId));
        reservation.setPerformanceId(performanceEntity.id);
        // Data fetching ends here

        List<ReservationSeat> reservedSeats = allocateSeats(reservationCount, reservationCategory, performanceEntity).findSeatsForReservation();

        reservation.setSeats(reservedSeats.stream()
                .map(ReservationSeat::getSeatReference)
                .toArray(String[]::new));
        // calculate raw price
        BigDecimal performancePrice = performancePriceDao.fetchPerformancePrice(performanceEntity.id);
        CustomerSubscriptionDao customerSubscriptionDao = new CustomerSubscriptionDao();
        boolean isSubscribed = customerSubscriptionDao.fetchCustomerSubscription(customerId);
        BigDecimal voucherProgramDiscount = VoucherProgramDao.fetchVoucherProgram(LocalDate.now());

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

    private PerformanceAllocation allocateSeats(int reservationCount, String reservationCategory, PerformanceEntity performanceEntity) {
        // Data fetching starts here
        Performance performance = new PerformanceMapper().entityToBusiness(performanceEntity);
        List<String> freeSeatsRefs = performanceInventory.fetchFreeSeatsForPerformance(performance);

        PerformanceNature performanceNature = new PerformanceNature(performanceEntity.performanceNature);
        AllocationQuotaSpecification allocationQuota = allocationQuotas.find(performanceNature);
        // Data fetching ends here


        TheaterTopology theaterTopology = theaterTopologies.fetchTopologyForPerformance(performance);
        PerformanceAllocation performanceAllocation =
                new PerformanceAllocation(performance, theaterTopology, freeSeatsRefs,
                        reservationCount, reservationCategory, allocationQuota);

        performanceInventory.allocateSeats(performanceAllocation);
        return performanceAllocation;
    }

    public void cancelReservation(String reservationId, Long performanceId, List<String> seats) {
        performanceInventory.deallocateSeats(performanceId, seats);
        ReservationService.cancelReservation(Long.parseLong(reservationId));
    }
}
