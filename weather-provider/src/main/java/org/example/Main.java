package org.example;

import com.google.gson.JsonObject;
import java.util.concurrent.*;

public class Main {
    public static void main(String[] args) {
        SqliteWeatherStore store = new SqliteWeatherStore("weather.db");
        // SUSTITUYE "TU_API_KEY_AQUI" por tu código de OpenWeatherMap
        WeatherSupplier supplier = new WeatherSupplier("TU_API_KEY_AQUI");

        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

        System.out.println("--- Capturador de Clima Activo ---");

        // Tarea que se ejecuta periódicamente
        scheduler.scheduleAtFixedRate(() -> {
            try {
                String city = "Las Palmas";
                JsonObject data = supplier.getSelectedData(city);

                double temp = data.getAsJsonObject("main").get("temp").getAsDouble();
                int humidity = data.getAsJsonObject("main").get("humidity").getAsInt();

                // Guardamos en la base de datos [cite: 29]
                store.insertWeather(city, temp, humidity);

                System.out.println("[" + java.time.LocalTime.now() + "] Datos capturados para " + city);
            } catch (Exception e) {
                System.err.println("Error en la captura: " + e.getMessage());
            }
        }, 0, 1, TimeUnit.HOURS); // Ejecuta ahora y luego cada 1 hora [cite: 30]
    }
}