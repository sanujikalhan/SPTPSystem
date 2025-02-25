package com.example.sptm_systerm;

public class MeterReading {
    private String date;
    private Object value; // Change to a more specific type (e.g., int, double, String) if needed

    public MeterReading(String date, Object value) {
        this.date = date;
        this.value = value;
    }

    public String getDate() {
        return date;
    }

    public Object getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "Date: " + date + ", Value: " + value;
    }
}

