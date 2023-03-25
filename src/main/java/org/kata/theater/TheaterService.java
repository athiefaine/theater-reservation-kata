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
import org.kata.theater.domain.allocation.AllocationQuotaRepository;
import org.kata.theater.domain.allocation.AllocationQuotaSpecification;
import org.kata.theater.domain.allocation.PerformanceInventory;
import org.kata.theater.domain.price.Amount;
import org.kata.theater.domain.price.Rate;
import org.kata.theater.domain.reservation.ReservationRequest;
import org.kata.theater.domain.reservation.ReservationSeat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TheaterService {
    private final TheaterRoomDao theaterRoomDao = new TheaterRoomDao();
    private final PerformancePriceDao performancePriceDao = new PerformancePriceDao();

    private final AllocationQuotaRepository allocationQuotaRepository;

    public TheaterService(AllocationQuotaRepository allocationQuotaRepository) {
        this.allocationQuotaRepository = allocationQuotaRepository;
    }

    public ReservationRequest reservation(long customerId, int reservationCount, String reservationCategory, Performance performance) {
        // Data fetching starts here
        String reservationId = ReservationService.initNewReservation();
        TheaterRoom room = theaterRoomDao.fetchTheaterRoom(performance.id);
        BigDecimal performancePrice = performancePriceDao.fetchPerformancePrice(performance.id);
        AllocationQuotaSpecification allocationQuota = getAllocationQuota(performance);
        CustomerSubscriptionDao customerSubscriptionDao = new CustomerSubscriptionDao();
        boolean isSubscribed = customerSubscriptionDao.fetchCustomerSubscription(customerId);
        BigDecimal voucherProgramDiscount = VoucherProgramDao.fetchVoucherProgram(LocalDate.now());
        // Data fetching ends here

        Reservation reservation = new Reservation();
        reservation.setReservationId(Long.parseLong(reservationId));
        reservation.setPerformanceId(performance.id);


        // find "reservationCount" first contiguous seats in any row
        int remainingSeats = 0;
        int totalSeats = 0;
        boolean foundAllSeats = false;
        ArrayList<ReservationSeat> reservedSeats = new ArrayList<>();
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
                                    reservedSeats.add(new ReservationSeat(seat, zoneCategory));
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

        PerformanceInventory performanceInventory =
                PerformanceInventory.builder()
                .totalSeatsCount(totalSeats)
                .availableSeatsCount(remainingSeats)
                .build();
        if (!allocationQuota.isSatisfiedBy(performanceInventory)) {
            reservedSeats = new ArrayList<>();
        }

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
        if (foundAllSeats) {
            // TODO :introduce repository that takes a domain object that contains reservedSeats
            // TODO : shouldn't be it saved at the end of the method ?
            theaterRoomDao.saveSeats(performance.id, reservedSeats.stream()
                    .map(ReservationSeat::getSeatReference)
                    .collect(Collectors.toList()), "BOOKING_PENDING");
        }
        // TODO : introduce a DAO that saves a ReservationRequest in front of ReservationRequest
        // TODO : shouldn't be it saved at the end of the method ?
        ReservationService.updateReservation(reservation);
        // Data updates end here

        return ReservationRequest.builder()
                .reservationId(reservationId)
                .performance(performance)
                .reservationCategory(reservationCategory)
                .reservedSeats(reservedSeats)
                .totalBilling(totalBilling)
                .build();
    }

    private static AllocationQuotaSpecification getAllocationQuota(Performance performance) {
        double vipQuota;
        switch (performance.performanceNature) {
            case "PREMIERE":
                vipQuota = 0.5;
                break;
            case "PREVIEW":
                vipQuota = 0.9;
                break;
            default:
                vipQuota = -1;
        }
        return new AllocationQuotaSpecification(vipQuota);
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
