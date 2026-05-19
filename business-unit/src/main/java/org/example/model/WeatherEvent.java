package org.example.model;

public class WeatherEvent {
    private String ts;
    private String ss;
    private String city;
    private double temp;
    private int humidity;

    public WeatherEvent() {}

    public WeatherEvent(String ts, String ss, String city, double temp, int humidity) {
        this.ts = ts;
        this.ss = ss;
        this.city = city;
        this.temp = temp;
        this.humidity = humidity;
    }

    public String getTs()       { return ts; }
    public String getSs()       { return ss; }
    public String getCity()     { return city; }
    public double getTemp()     { return temp; }
    public int    getHumidity() { return humidity; }
}