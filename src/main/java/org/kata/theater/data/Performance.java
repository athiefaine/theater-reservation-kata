package org.kata.theater.data;

import java.time.LocalDateTime;

public class Performance {

    public long id;
    public String play; // "The CICD - Corneille", "Les fourberies de Scala - Molière"

    public LocalDateTime startTime;

    public LocalDateTime endTime;

    public String performanceNature; // can be "PREVIEW", "PREMIERE", etc
}
