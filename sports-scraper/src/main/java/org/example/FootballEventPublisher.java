package org.example;

import com.google.gson.JsonObject;
import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;

public class FootballEventPublisher {

    private static final String BROKER_URL = "tcp://localhost:61616";
    private static final String TOPIC_NAME = "Football";

    private final Connection connection;
    private final Session session;
    private final MessageProducer producer;

    public FootballEventPublisher() throws JMSException {
        ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(BROKER_URL);
        connection = factory.createConnection();
        connection.start();
        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Topic topic = session.createTopic(TOPIC_NAME);
        producer = session.createProducer(topic);
        System.out.println("[FootballPublisher] Conectado a ActiveMQ.");
    }

    /**
     * Publica un evento de partido en el topic Football.
     * Estructura mínima requerida por el enunciado:
     *   ts: timestamp UTC de la captura
     *   ss: identificador de la fuente
     *   + payload con los datos del partido
     */
    public void publishMatch(String competition, String matchDate,
                             String homeTeam, String awayTeam,
                             int homeScore, int awayScore) throws JMSException {
        JsonObject event = new JsonObject();
        event.addProperty("ts", java.time.Instant.now().toString());
        event.addProperty("ss", "sports-scraper");
        event.addProperty("type", "match");
        event.addProperty("competition", competition);
        event.addProperty("match_date", matchDate);
        event.addProperty("home_team", homeTeam);
        event.addProperty("away_team", awayTeam);
        event.addProperty("home_score", homeScore);
        event.addProperty("away_score", awayScore);

        TextMessage message = session.createTextMessage(event.toString());
        producer.send(message);
        System.out.println("[FootballPublisher] Partido publicado: "
                + homeTeam + " vs " + awayTeam);
    }

    /**
     * Publica un evento de clasificación en el topic Football.
     */
    public void publishStanding(int position, String teamName,
                                int points, int playedGames) throws JMSException {
        JsonObject event = new JsonObject();
        event.addProperty("ts", java.time.Instant.now().toString());
        event.addProperty("ss", "sports-scraper");
        event.addProperty("type", "standing");
        event.addProperty("position", position);
        event.addProperty("team_name", teamName);
        event.addProperty("points", points);
        event.addProperty("played_games", playedGames);

        TextMessage message = session.createTextMessage(event.toString());
        producer.send(message);
    }

    public void close() throws JMSException {
        session.close();
        connection.close();
    }
}