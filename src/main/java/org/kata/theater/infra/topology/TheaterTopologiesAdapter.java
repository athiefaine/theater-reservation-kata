package org.kata.theater.infra.topology;

import org.kata.theater.dao.TheaterRoomDao;
import org.kata.theater.domain.allocation.Performance;
import org.kata.theater.domain.topology.TheaterTopology;
import org.kata.theater.infra.mappers.TheaterTopologyMapper;

public class TheaterTopologiesAdapter implements org.kata.theater.domain.topology.TheaterTopologies {
    private final TheaterRoomDao theaterRoomDao = new TheaterRoomDao();

    private final TheaterTopologyMapper theaterTopologyMapper = new TheaterTopologyMapper();

    @Override
    public TheaterTopology fetchTopologyForPerformance(Performance performance) {
        return theaterTopologyMapper.entityToBusiness(theaterRoomDao.fetchTheaterRoom(performance.getId()));
    }

}
