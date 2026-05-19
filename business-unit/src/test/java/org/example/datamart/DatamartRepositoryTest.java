package org.example.datamart;

import org.example.model.MatchEvent;
import org.example.model.MatchWeatherReport;
import org.example.model.StandingEvent;
import org.example.model.WeatherEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DatamartRepositoryTest {

    @TempDir
    Path tempDir;

    private DatamartRepository repo;

    @BeforeEach
    void setUp() {
        String dbPath = tempDir.resolve("test_datamart.db").toString();
        repo = new DatamartRepository(dbPath);
    }

    @Test
    void upsertWeather_guardaYRecuperaClima() {
        WeatherEvent event = new WeatherEvent(
                "2026-05-10T10:00:00Z", "weather-provider", "Las Palmas", 22.5, 65);
        repo.upsertWeather(event);
        WeatherEvent result = repo.getLatestWeather("Las Palmas");
        assertNotNull(result);
        assertEquals("Las Palmas", result.getCity());
        assertEquals(22.5, result.getTemp(), 0.01);
        assertEquals(65, result.getHumidity());
    }

    @Test
    void upsertWeather_actualizaEnVezDeDuplicar() {
        WeatherEvent primera = new WeatherEvent(
                "2026-05-10T10:00:00Z", "weather-provider", "Las Palmas", 20.0, 60);
        WeatherEvent segunda = new WeatherEvent(
                "2026-05-10T11:00:00Z", "weather-provider", "Las Palmas", 25.0, 70);
        repo.upsertWeather(primera);
        repo.upsertWeather(segunda);
        WeatherEvent result = repo.getLatestWeather("Las Palmas");
        assertNotNull(result);
        assertEquals(25.0, result.getTemp(), 0.01);
        assertEquals(70, result.getHumidity());
    }

    @Test
    void getLatestWeather_devuelveNullSiNoExiste() {
        WeatherEvent result = repo.getLatestWeather("CiudadQueNoExiste");
        assertNull(result);
    }

    @Test
    void insertMatch_yGetLatestLasPalmasMatch() {
        MatchEvent event = new MatchEvent(
                "2026-05-10T20:00:00Z", "sports-scraper",
                "LaLiga", "2026-05-10T20:00:00Z",
                "UD Las Palmas", "FC Barcelona", 1, 2);
        repo.insertMatch(event);
        MatchEvent result = repo.getLatestLasPalmasMatch();
        assertNotNull(result);
        assertEquals("UD Las Palmas", result.getHomeTeam());
        assertEquals("FC Barcelona", result.getAwayTeam());
        assertEquals(1, result.getHomeScore());
        assertEquals(2, result.getAwayScore());
    }

    @Test
    void getAllLasPalmasMatches_filtroOtrosEquipos() {
        repo.insertMatch(new MatchEvent(
                "2026-05-09T20:00:00Z", "sports-scraper",
                "LaLiga", "2026-05-09T20:00:00Z",
                "UD Las Palmas", "Real Madrid", 0, 1));
        repo.insertMatch(new MatchEvent(
                "2026-05-09T18:00:00Z", "sports-scraper",
                "LaLiga", "2026-05-09T18:00:00Z",
                "Atletico Madrid", "Sevilla", 2, 0));
        List<MatchEvent> result = repo.getAllLasPalmasMatches();
        assertEquals(1, result.size());
        assertTrue(result.get(0).getHomeTeam().contains("Las Palmas"));
    }

    @Test
    void upsertStanding_guardaYDevuelveClasificacion() {
        StandingEvent standing = new StandingEvent(
                "2026-05-10T10:00:00Z", "sports-scraper",
                14, "UD Las Palmas", 38, 36);
        repo.upsertStanding(standing);
        List<StandingEvent> result = repo.getStandings();
        assertEquals(1, result.size());
        assertEquals("UD Las Palmas", result.get(0).getTeamName());
        assertEquals(14, result.get(0).getPosition());
        assertEquals(38, result.get(0).getPoints());
    }

    @Test
    void insertReport_yGetLatestReport() {
        MatchWeatherReport report = new MatchWeatherReport();
        report.setMatchDate("2026-05-10T20:00:00Z");
        report.setHomeTeam("UD Las Palmas");
        report.setAwayTeam("Real Madrid");
        report.setCompetition("LaLiga");
        report.setHomeScore(1);
        report.setAwayScore(1);
        report.setCity("Las Palmas");
        report.setTemp(22.0);
        report.setHumidity(65);
        report.setCapturedAt("2026-05-10T21:00:00Z");
        report.evaluateConditions();
        repo.insertReport(report);
        MatchWeatherReport result = repo.getLatestReport();
        assertNotNull(result);
        assertEquals("UD Las Palmas", result.getHomeTeam());
        assertEquals("FAVORABLE", result.getWeatherCondition());
    }

    @Test
    void evaluateConditions_clasificaCorrectamente() {
        MatchWeatherReport riesgo = new MatchWeatherReport();
        riesgo.setTemp(3.0);
        riesgo.setHumidity(50);
        riesgo.evaluateConditions();
        assertEquals("RIESGO", riesgo.getWeatherCondition());

        MatchWeatherReport adverso = new MatchWeatherReport();
        adverso.setTemp(15.0);
        adverso.setHumidity(80);
        adverso.evaluateConditions();
        assertEquals("ADVERSO", adverso.getWeatherCondition());

        MatchWeatherReport favorable = new MatchWeatherReport();
        favorable.setTemp(20.0);
        favorable.setHumidity(60);
        favorable.evaluateConditions();
        assertEquals("FAVORABLE", favorable.getWeatherCondition());
    }
}
