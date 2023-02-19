package org.kata.theater.data;

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
}
