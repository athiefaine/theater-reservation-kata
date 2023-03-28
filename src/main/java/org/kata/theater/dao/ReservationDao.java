package org.kata.theater.dao;

import org.kata.theater.data.ReservationEntity;

import java.util.HashMap;
import java.util.Map;

public class ReservationDao {

    private static Map<Long, ReservationEntity> reservationMap = new HashMap<>();
    public void update(ReservationEntity reservationEntity) {
        reservationMap.put(reservationEntity.getReservationId(), reservationEntity);
    }

    public ReservationEntity find(long reservationId) {
        return reservationMap.get(reservationId);
    }
}
