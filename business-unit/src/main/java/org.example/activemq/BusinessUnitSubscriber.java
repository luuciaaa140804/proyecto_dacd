package org.example.activemq;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.example.datamart.DatamartRepository;
import org.example.model.MatchEvent;
import org.example.model.MatchWeatherReport;
import org.example.model.WeatherEvent;

import javax.jms.*;
import java.time.Instant;

public class BusinessUnitSubscriber {

    private static final String BROKER_URL = "tcp://localhost:61616";
    private static final String CLIENT_ID  = "business-unit";

    private final DatamartRepository datamart;
    private final Gson gson = new Gson();

    private Connection connection;
    private Session    session;

    public BusinessUnitSubscriber(DatamartRepository datamart) {
        this.datamart = datamart;
    }

    public void start() throws JMSException {
        ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(BROKER_URL);
        connection = factory.createConnection();
        connection.setClientID(CLIENT_ID);
        connection.start();
        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        subscribeToWeather();
        subscribeToFootball();
        System.out.println("[BusinessUnit] Suscrito a los topics Weather y Football.");
    }

    private void subscribeToWeather() throws JMSException {
        Topic topic = session.createTopic("Weather");
        MessageConsumer consumer = session.createDurableSubscriber(topic, "bu-weather");
        consumer.setMessageListener(message -> {
            try {
                if (message instanceof TextMessage textMessage) {
                    processWeatherEvent(textMessage.getText());
                }
            } catch (JMSException e) {
                System.err.println("[BusinessUnit] Error al leer Weather: " + e.getMessage());
            }
        });
    }

    private void subscribeToFootball() throws JMSException {
        Topic topic = session.createTopic("Football");
        MessageConsumer consumer = session.createDurableSubscriber(topic, "bu-football");
        consumer.setMessageListener(message -> {
            try {
                if (message instanceof TextMessage textMessage) {
                    processFootballEvent(textMessage.getText());
                }
            } catch (JMSException e) {
                System.err.println("[BusinessUnit] Error al leer Football: " + e.getMessage());
            }
        });
    }

    private void processWeatherEvent(String json) {
        try {
            WeatherEvent event = gson.fromJson(json, WeatherEvent.class);
            datamart.upsertWeather(event);
            System.out.println("[BusinessUnit] Clima recibido: " + event.getCity()
                    + " → " + event.getTemp() + "°C");
            tryGenerateReport(event.getCity());
        } catch (Exception e) {
            System.err.println("[BusinessUnit] Error al procesar Weather: " + e.getMessage());
        }
    }

    private void processFootballEvent(String json) {
        try {
            JsonObject obj = JsonParser.parseString(json).getAsJsonObject();
            String type = obj.has("type") ? obj.get("type").getAsString() : "";
            if ("match".equals(type)) {
                MatchEvent event = gson.fromJson(obj, MatchEvent.class);
                if (isLasPalmasMatch(event)) {
                    datamart.insertMatch(event);
                    System.out.println("[BusinessUnit] Partido recibido: "
                            + event.getHomeTeam() + " vs " + event.getAwayTeam());
                    tryGenerateReport("Las Palmas");
                }
            } else if ("standing".equals(type)) {
                MatchEvent event = gson.fromJson(obj, MatchEvent.class);
                datamart.upsertStanding(event);
            }
        } catch (Exception e) {
            System.err.println("[BusinessUnit] Error al procesar Football: " + e.getMessage());
        }
    }

    private void tryGenerateReport(String city) {
        WeatherEvent weather = datamart.getLatestWeather(city);
        MatchEvent   match   = datamart.getLatestLasPalmasMatch();
        if (weather == null || match == null) return;

        MatchWeatherReport report = new MatchWeatherReport();
        report.setMatchDate(match.getMatchDate());
        report.setHomeTeam(match.getHomeTeam());
        report.setAwayTeam(match.getAwayTeam());
        report.setCompetition(match.getCompetition());
        report.setHomeScore(match.getHomeScore());
        report.setAwayScore(match.getAwayScore());
        report.setCity(weather.getCity());
        report.setTemp(weather.getTemp());
        report.setHumidity(weather.getHumidity());
        report.setCapturedAt(Instant.now().toString());
        report.evaluateConditions();
        datamart.insertReport(report);
        System.out.println("[BusinessUnit] Informe generado → " + report.getWeatherCondition());
    }

    private boolean isLasPalmasMatch(MatchEvent event) {
        String home = event.getHomeTeam();
        String away = event.getAwayTeam();
        return (home != null && home.contains("Las Palmas"))
                || (away != null && away.contains("Las Palmas"));
    }

    public void close() throws JMSException {
        if (session != null)    session.close();
        if (connection != null) connection.close();
    }
}