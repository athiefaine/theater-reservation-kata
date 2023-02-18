package org.kata.theater;

import java.time.LocalDateTime;

public class Performance {

    long id;
    String play; // "The CICD - Corneille", "Les fourberies de Scala - Molière"

    LocalDateTime startTime;

    LocalDateTime endTime;

    String performanceNature; // can be "PREVIEW", "PREMIERE", etc
}
