package org.kata.theater.data;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class Zone {
    private Row[] rows;

    private String category; // STANDARD, PREMIUM

    public Zone(Row[] rows, String category) {
        this.rows = rows;
        this.category = category;
    }

    public Row[] getRows() {
        return rows;
    }

    public void setRows(Row[] rows) {
        this.rows = rows;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public List<Seat> seats() {
        return Arrays.stream(rows)
                .map(row -> Arrays.asList(row.getSeats()))
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }
}
