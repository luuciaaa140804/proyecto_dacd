package org.example;

public class Main {

    public static void main(String[] args) throws Exception {

        EventStoreBuilder builder = new EventStoreBuilder();

        // Nos suscribimos a los dos topics
        builder.subscribeTo("Weather", "weather-provider");
        builder.subscribeTo("Football", "sports-scraper");

        System.out.println("--- Event Store Builder Activo. Esperando eventos... ---");

        // Mantenemos el programa vivo indefinidamente
        Thread.currentThread().join();
    }
}