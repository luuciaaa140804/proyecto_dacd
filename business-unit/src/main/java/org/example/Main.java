package org.example;

import org.example.activemq.BusinessUnitSubscriber;
import org.example.api.RestApi;
import org.example.datamart.DatamartRepository;
import org.example.eventstore.EventStoreReader;

public class Main {

    private static final int API_PORT = 7000;

    public static void main(String[] args) throws Exception {

        System.out.println("=== Business Unit - UD Las Palmas ===");

        // 1. Datamart local
        DatamartRepository datamart = new DatamartRepository("datamart.db");

        // 2. Cargar eventos históricos del event store
        EventStoreReader historyReader = new EventStoreReader(datamart);
        historyReader.loadHistoricalEvents();

        // 3. Suscripción en tiempo real a ActiveMQ
        BusinessUnitSubscriber subscriber = new BusinessUnitSubscriber(datamart);
        try {
            subscriber.start();
        } catch (Exception e) {
            System.err.println("[Main] No se pudo conectar a ActiveMQ: " + e.getMessage());
            System.err.println("[Main] El sistema funcionará solo con datos históricos.");
        }

        // 4. API REST
        RestApi api = new RestApi(datamart);
        api.start(API_PORT);

        // Cierre limpio al pulsar Ctrl+C
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\n[Main] Cerrando business-unit...");
            api.stop();
            try { subscriber.close(); } catch (Exception ignored) {}
            System.out.println("[Main] Cerrado correctamente.");
        }));

        // Mantener el proceso vivo
        Thread.currentThread().join();
    }
}