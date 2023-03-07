package org.kata.theater.dao;

import org.kata.theater.data.Row;
import org.kata.theater.data.Seat;
import org.kata.theater.data.TheaterRoom;
import org.kata.theater.data.Zone;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TheaterRoomDao {
    // simulates a room map/topology repository

    private Map<Long, TheaterRoom> theaterRoomMaps = new HashMap<>();

    public TheaterRoomDao() {
        theaterRoomMaps.put(1L, fetchRoomForPerformance1());
        theaterRoomMaps.put(2L, fetchRoomForPerformance1());
        theaterRoomMaps.put(3L, fetchRoomForPerformance2());
    }

    public TheaterRoom fetchTheaterRoom(long performanceId) {
        return theaterRoomMaps.get(performanceId);
    }

    public void save(long id, TheaterRoom room) {
        theaterRoomMaps.put(id, room);
    }

    public void saveSeats(long id, List<String> seatsID, String status) {
        TheaterRoom room = theaterRoomMaps.get(id);
        for (int i = 0; i < room.getZones().length; i++) {
            Zone zone = room.getZones()[i];
            for (int j = 0; j < zone.getRows().length; j++) {
                Row row = zone.getRows()[j];
                for (int k = 0; k < row.getSeats().length; k++) {
                    Seat seat = row.getSeats()[k];
                    if (seatsID.contains(seat.getSeatId())) {
                        seat.setStatus(status);
                    }
                }
            }
        }
    }


    private static TheaterRoom fetchRoomForPerformance1() {
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
        return new TheaterRoom( // pourrait être un Agregate
                new Zone[]{new Zone(new Row[]{
                        new Row(new Seat[]{
                                new Seat("A1", "BOOKED"), // smell : le statut est mélé à la topologie
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
                }, "STANDARD"),
                        new Zone(new Row[]{
                                new Row(new Seat[]{
                                        new Seat("H1", "BOOKED"), // smell : le statut est mélé à la topologie
                                        new Seat("H2", "FREE"),
                                        new Seat("H3", "BOOKED"),
                                        new Seat("H4", "BOOKED"),
                                        new Seat("H5", "FREE"),
                                        new Seat("H6", "FREE"),
                                        new Seat("H7", "FREE")
                                }),
                                new Row(new Seat[]{
                                        new Seat("I1", "FREE"),
                                        new Seat("I2", "BOOKED"),
                                        new Seat("I3", "FREE"),
                                        new Seat("I4", "FREE"),
                                        new Seat("I5", "FREE"),
                                        new Seat("I6", "FREE"),
                                        new Seat("I7", "FREE"),
                                        new Seat("I8", "FREE")
                                })
                        }, "PREMIUM")
                });
    }

    private static TheaterRoom fetchRoomForPerformance2() {

        return new TheaterRoom( // pourrait être un Agregate
                new Zone[]{new Zone(new Row[]{
                        new Row(new Seat[]{
                                new Seat("R1-1", "BOOKED"), // smell : le statut est mélé à la topologie
                                new Seat("R1-2", "FREE"),
                                new Seat("R1-3", "BOOKED"),
                                new Seat("R1-4", "BOOKED"),
                                new Seat("R1-5", "FREE"),
                                new Seat("R1-6", "FREE"),
                                new Seat("R1-7", "FREE")
                        }),
                        new Row(new Seat[]{
                                new Seat("R2-1", "FREE"),
                                new Seat("R2-2", "BOOKED"),
                                new Seat("R2-3", "FREE"),
                                new Seat("R2-4", "FREE"),
                                new Seat("R2-5", "FREE"),
                                new Seat("R2-6", "FREE"),
                                new Seat("R2-7", "FREE"),
                                new Seat("R2-8", "FREE")
                        }
                        )
                }, "STANDARD")
                });
    }
}
