package org.example;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private static final Logger logger = LoggerFactory.getLogger(EventStoreBuilder.class);

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
        logger.info("Conectado a ActiveMQ en {}", BROKER_URL);
    }

    public void subscribeTo(String topicName, String ss) throws JMSException {
        Topic topic = session.createTopic(topicName);
        MessageConsumer consumer = session.createDurableSubscriber(topic, topicName + "-" + ss);

        consumer.setMessageListener(message -> {
            try {
                if (message instanceof TextMessage textMessage) {
                    String json = textMessage.getText();
                    saveEvent(topicName, ss, json);
                }
            } catch (JMSException e) {
                logger.error("Error al leer mensaje del topic {}: {}", topicName, e.getMessage());
            }
        });

        logger.info("Suscrito (durable) al topic: {}", topicName);
    }

    private void saveEvent(String topic, String ss, String json) {
        try {
            JsonObject event = JsonParser.parseString(json).getAsJsonObject();
            String ts = event.has("ts") ? event.get("ts").getAsString() : Instant.now().toString();

            String date = DateTimeFormatter.ofPattern("yyyyMMdd")
                    .format(Instant.parse(ts).atZone(ZoneOffset.UTC));

            Path dir = Paths.get("eventstore", topic, ss);
            Files.createDirectories(dir);

            Path file = dir.resolve(date + ".events");
            try (FileWriter fw = new FileWriter(file.toFile(), true)) {
                fw.write(json + "\n");
            }

            logger.info("Evento guardado en: {}", file);

        } catch (IOException e) {
            logger.error("Error al guardar evento: {}", e.getMessage());
        }
    }

    public void close() throws JMSException {
        session.close();
        connection.close();
        logger.info("Conexión con ActiveMQ cerrada.");
    }
}