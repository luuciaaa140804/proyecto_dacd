package org.example;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public class EventStoreBuilder {

    private static final String BROKER_URL = "tcp://localhost:61616";
    private static final String CLIENT_ID  = "event-store-builder";

    private final Connection connection;
    private final Session session;

    public EventStoreBuilder() throws JMSException {
        ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(BROKER_URL);
        connection = factory.createConnection();
        connection.setClientID(CLIENT_ID);
        connection.start();
        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        System.out.println("[EventStoreBuilder] Conectado a ActiveMQ.");
    }

    /**
     * Se suscribe de forma durable a un topic y guarda cada evento en el fichero correspondiente.
     * @param topicName nombre del topic (p.ej. "Weather" o "Football")
     * @param ss        identificador de la fuente (p.ej. "weather-provider" o "sports-scraper")
     */
    public void subscribeTo(String topicName, String ss) throws JMSException {
        Topic topic = session.createTopic(topicName);
        // Suscripción durable: si el builder se cae, al volver recupera los mensajes perdidos
        MessageConsumer consumer = session.createDurableSubscriber(topic, topicName + "-" + ss);

        consumer.setMessageListener(message -> {
            try {
                if (message instanceof TextMessage textMessage) {
                    String json = textMessage.getText();
                    saveEvent(topicName, ss, json);
                }
            } catch (JMSException e) {
                System.err.println("[EventStoreBuilder] Error al leer mensaje: " + e.getMessage());
            }
        });

        System.out.println("[EventStoreBuilder] Suscrito al topic: " + topicName);
    }

    /**
     * Guarda el evento en el fichero correcto según la estructura:
     * eventstore/{topic}/{ss}/{YYYYMMDD}.events
     */
    private void saveEvent(String topic, String ss, String json) {
        try {
            // Extraemos el timestamp del evento para calcular la fecha del fichero
            JsonObject event = JsonParser.parseString(json).getAsJsonObject();
            String ts = event.has("ts") ? event.get("ts").getAsString() : Instant.now().toString();

            String date = DateTimeFormatter.ofPattern("yyyyMMdd")
                    .format(Instant.parse(ts).atZone(ZoneOffset.UTC));

            // Creamos el directorio si no existe
            Path dir = Paths.get("eventstore", topic, ss);
            Files.createDirectories(dir);

            // Añadimos el evento al fichero (una línea por evento)
            Path file = dir.resolve(date + ".events");
            try (FileWriter fw = new FileWriter(file.toFile(), true)) {
                fw.write(json + "\n");
            }

            System.out.println("[EventStoreBuilder] Evento guardado en: " + file);

        } catch (IOException e) {
            System.err.println("[EventStoreBuilder] Error al guardar evento: " + e.getMessage());
        }
    }

    public void close() throws JMSException {
        session.close();
        connection.close();
    }
}