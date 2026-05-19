package org.example;

import com.google.gson.JsonObject;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.*;

public class WeatherEventPublisher {

    private static final Logger logger = LoggerFactory.getLogger(WeatherEventPublisher.class);

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
        logger.info("Conectado a ActiveMQ en {}", BROKER_URL);
    }

    public void publish(String city, double temp, int humidity) throws JMSException {
        JsonObject event = new JsonObject();
        event.addProperty("ts", java.time.Instant.now().toString());
        event.addProperty("ss", "weather-provider");
        event.addProperty("city", city);
        event.addProperty("temp", temp);
        event.addProperty("humidity", humidity);

        TextMessage message = session.createTextMessage(event.toString());
        producer.send(message);
        logger.info("Evento publicado → ciudad={} temp={}°C humedad={}%", city, temp, humidity);
    }

    public void close() throws JMSException {
        session.close();
        connection.close();
        logger.info("Conexión con ActiveMQ cerrada.");
    }
}