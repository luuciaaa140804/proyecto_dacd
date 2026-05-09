package org.example;

import com.google.gson.JsonObject;
import java.util.concurrent.*;

public class Main {

    public static void main(String[] args) throws Exception {

        SqliteWeatherStore store = new SqliteWeatherStore("weather.db");
        WeatherSupplier supplier = new WeatherSupplier("860d63050d134ad7dcfc5c1d57c5f081");
        WeatherEventPublisher publisher = new WeatherEventPublisher();

        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

        System.out.println("--- Capturador de Clima Activo ---");

        scheduler.scheduleAtFixedRate(() -> {
            try {
                String city = "Las Palmas";
                JsonObject data = supplier.getSelectedData(city);

                double temp     = data.getAsJsonObject("main").get("temp").getAsDouble();
                int humidity    = data.getAsJsonObject("main").get("humidity").getAsInt();

                // Guardamos en SQLite (Sprint 1)
                store.insertWeather(city, temp, humidity);

                // Publicamos en ActiveMQ (Sprint 2)
                publisher.publish(city, temp, humidity);

                System.out.println("[" + java.time.LocalTime.now() + "] Datos capturados para " + city);

            } catch (Exception e) {
                System.err.println("Error en la captura: " + e.getMessage());
            }
        }, 0, 1, TimeUnit.HOURS);
    }
}