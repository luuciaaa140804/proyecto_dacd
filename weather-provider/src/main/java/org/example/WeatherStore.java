package org.example;

public interface WeatherStore {
    void insertWeather(String city, double temp, int humidity);
}