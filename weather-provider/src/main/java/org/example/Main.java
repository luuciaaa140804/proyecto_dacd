package org.example;

import java.util.concurrent.*;

public class Main {

    public static void main(String[] args) throws Exception {

        if (args.length < 1) {
            System.err.println("Uso: java -jar weather-provider.jar <OPENWEATHER_API_KEY>");
            System.exit(1);
        }

        String apiKey = args[0];

        WeatherStore store = new SqliteWeatherStore("weather.db");
        WeatherSupplier supplier = new WeatherSupplier(apiKey);
        WeatherEventPublisher publisher = new WeatherEventPublisher();

        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

        System.out.println("--- Capturador de Clima Activo ---");

        scheduler.scheduleAtFixedRate(() -> {
            try {
                String city = "Las Palmas";
                var data = supplier.getSelectedData(city);

                double temp  = data.getAsJsonObject("main").get("temp").getAsDouble();
                int humidity = data.getAsJsonObject("main").get("humidity").getAsInt();

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