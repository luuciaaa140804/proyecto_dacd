package org.example;

import com.google.gson.JsonObject;
import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;

public class WeatherEventPublisher {

    private static final String BROKER_URL = "tcp://localhost:61616";
    private static final String TOPIC_NAME = "Weather";

    private final Connection connection;
    private final Session session;
    private final MessageProducer producer;

    public WeatherEventPublisher() throws JMSException {
        ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(BROKER_URL);
        connection = factory.createConnection();
        connection.start();
        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Topic topic = session.createTopic(TOPIC_NAME);
        producer = session.createProducer(topic);
        System.out.println("[WeatherPublisher] Conectado a ActiveMQ.");
    }

    /**
     * Publica un evento de clima en el topic Weather.
     * Estructura mínima requerida por el enunciado:
     *   ts: timestamp UTC de la captura
     *   ss: identificador de la fuente
     *   + payload con los datos del clima
     */
    public void publish(String city, double temp, int humidity) throws JMSException {
        JsonObject event = new JsonObject();
        event.addProperty("ts", java.time.Instant.now().toString());
        event.addProperty("ss", "weather-provider");
        event.addProperty("city", city);
        event.addProperty("temp", temp);
        event.addProperty("humidity", humidity);

        TextMessage message = session.createTextMessage(event.toString());
        producer.send(message);
        System.out.println("[WeatherPublisher] Evento publicado: " + event);
    }

    public void close() throws JMSException {
        session.close();
        connection.close();
    }
}