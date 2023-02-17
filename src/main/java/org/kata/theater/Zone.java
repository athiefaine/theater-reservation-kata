package org.kata.theater;

public class Zone {
    private Row[] rows;

    public Zone(Row[] rows) {
        this.rows = rows;
    }

    public Row[] getRows() {
        return rows;
    }

    public void setRows(Row[] rows) {
        this.rows = rows;
    }
}
