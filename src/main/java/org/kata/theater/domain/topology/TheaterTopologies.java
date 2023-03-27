package org.kata.theater.domain.topology;

import org.kata.theater.domain.allocation.Performance;

public interface TheaterTopologies {
    TheaterTopology fetchTopologyForPerformance(Performance performance);
}
