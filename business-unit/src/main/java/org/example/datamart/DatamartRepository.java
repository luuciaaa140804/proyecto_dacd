package org.example.datamart;

import org.example.model.MatchEvent;
import org.example.model.MatchWeatherReport;
import org.example.model.StandingEvent;
import org.example.model.WeatherEvent;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DatamartRepository {

    private final String dbUrl;

    public DatamartRepository(String dbPath) {
        this.dbUrl = "jdbc:sqlite:" + dbPath;
        initDatabase();
    }

    private void initDatabase() {
        try (Connection conn = DriverManager.getConnection(dbUrl);
             Statement stmt = conn.createStatement()) {

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS weather_latest (
                    city        TEXT PRIMARY KEY,
                    temp        REAL,
                    humidity    INTEGER,
                    captured_at TEXT
                )
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS match_history (
                    id          INTEGER PRIMARY KEY AUTOINCREMENT,
                    competition TEXT,
                    match_date  TEXT,
                    home_team   TEXT,
                    away_team   TEXT,
                    home_score  INTEGER,
                    away_score  INTEGER,
                    captured_at TEXT,
                    UNIQUE(match_date, home_team, away_team)
                )
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS standings (
                    position     INTEGER PRIMARY KEY,
                    team_name    TEXT,
                    points       INTEGER,
                    played_games INTEGER,
                    captured_at  TEXT
                )
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS match_weather_report (
                    id                INTEGER PRIMARY KEY AUTOINCREMENT,
                    match_date        TEXT,
                    home_team         TEXT,
                    away_team         TEXT,
                    competition       TEXT,
                    home_score        INTEGER,
                    away_score        INTEGER,
                    city              TEXT,
                    temp              REAL,
                    humidity          INTEGER,
                    weather_condition TEXT,
                    condition_detail  TEXT,
                    captured_at       TEXT
                )
            """);

            System.out.println("[Datamart] Base de datos inicializada.");

        } catch (SQLException e) {
            System.err.println("[Datamart] Error al inicializar: " + e.getMessage());
        }
    }

    // ── Weather ───────────────────────────────────────────────────────────

    public void upsertWeather(WeatherEvent event) {
        String sql = """
            INSERT INTO weather_latest (city, temp, humidity, captured_at)
            VALUES (?, ?, ?, ?)
            ON CONFLICT(city) DO UPDATE SET
                temp        = excluded.temp,
                humidity    = excluded.humidity,
                captured_at = excluded.captured_at
        """;
        try (Connection conn = DriverManager.getConnection(dbUrl);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, event.getCity());
            ps.setDouble(2, event.getTemp());
            ps.setInt(3, event.getHumidity());
            ps.setString(4, event.getTs());
            ps.executeUpdate();
            System.out.println("[Datamart] Clima actualizado: " + event.getCity()
                    + " → " + event.getTemp() + "°C");
        } catch (SQLException e) {
            System.err.println("[Datamart] Error al guardar clima: " + e.getMessage());
        }
    }

    public WeatherEvent getLatestWeather(String city) {
        String sql = "SELECT * FROM weather_latest WHERE city = ?";
        try (Connection conn = DriverManager.getConnection(dbUrl);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, city);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new WeatherEvent(
                        rs.getString("captured_at"),
                        "weather-provider",
                        rs.getString("city"),
                        rs.getDouble("temp"),
                        rs.getInt("humidity")
                );
            }
        } catch (SQLException e) {
            System.err.println("[Datamart] Error al leer clima: " + e.getMessage());
        }
        return null;
    }

    // ── Matches ───────────────────────────────────────────────────────────

    public void insertMatch(MatchEvent event) {
        String sql = """
            INSERT OR IGNORE INTO match_history
                (competition, match_date, home_team, away_team,
                 home_score, away_score, captured_at)
            VALUES (?, ?, ?, ?, ?, ?, ?)
        """;
        try (Connection conn = DriverManager.getConnection(dbUrl);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, event.getCompetition());
            ps.setString(2, event.getMatchDate());
            ps.setString(3, event.getHomeTeam());
            ps.setString(4, event.getAwayTeam());
            ps.setInt(5, event.getHomeScore());
            ps.setInt(6, event.getAwayScore());
            ps.setString(7, event.getTs());
            ps.executeUpdate();
            System.out.println("[Datamart] Partido guardado: "
                    + event.getHomeTeam() + " vs " + event.getAwayTeam());
        } catch (SQLException e) {
            System.err.println("[Datamart] Error al guardar partido: " + e.getMessage());
        }
    }

    public MatchEvent getLatestLasPalmasMatch() {
        String sql = """
            SELECT * FROM match_history
            WHERE home_team LIKE '%Las Palmas%' OR away_team LIKE '%Las Palmas%'
            ORDER BY match_date DESC
            LIMIT 1
        """;
        try (Connection conn = DriverManager.getConnection(dbUrl);
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return new MatchEvent(
                        rs.getString("captured_at"),
                        "sports-scraper",
                        rs.getString("competition"),
                        rs.getString("match_date"),
                        rs.getString("home_team"),
                        rs.getString("away_team"),
                        rs.getInt("home_score"),
                        rs.getInt("away_score")
                );
            }
        } catch (SQLException e) {
            System.err.println("[Datamart] Error al leer partido: " + e.getMessage());
        }
        return null;
    }

    public List<MatchEvent> getAllLasPalmasMatches() {
        List<MatchEvent> list = new ArrayList<>();
        String sql = """
            SELECT * FROM match_history
            WHERE home_team LIKE '%Las Palmas%' OR away_team LIKE '%Las Palmas%'
            ORDER BY match_date DESC
        """;
        try (Connection conn = DriverManager.getConnection(dbUrl);
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new MatchEvent(
                        rs.getString("captured_at"),
                        "sports-scraper",
                        rs.getString("competition"),
                        rs.getString("match_date"),
                        rs.getString("home_team"),
                        rs.getString("away_team"),
                        rs.getInt("home_score"),
                        rs.getInt("away_score")
                ));
            }
        } catch (SQLException e) {
            System.err.println("[Datamart] Error al leer partidos: " + e.getMessage());
        }
        return list;
    }

    // ── Standings ─────────────────────────────────────────────────────────

    public void upsertStanding(StandingEvent event) {
        String sql = """
            INSERT INTO standings (position, team_name, points, played_games, captured_at)
            VALUES (?, ?, ?, ?, ?)
            ON CONFLICT(position) DO UPDATE SET
                team_name    = excluded.team_name,
                points       = excluded.points,
                played_games = excluded.played_games,
                captured_at  = excluded.captured_at
        """;
        try (Connection conn = DriverManager.getConnection(dbUrl);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, event.getPosition());
            ps.setString(2, event.getTeamName());
            ps.setInt(3, event.getPoints());
            ps.setInt(4, event.getPlayedGames());
            ps.setString(5, event.getTs());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[Datamart] Error al guardar clasificación: " + e.getMessage());
        }
    }

    public List<StandingEvent> getStandings() {
        List<StandingEvent> list = new ArrayList<>();
        String sql = "SELECT * FROM standings ORDER BY position ASC";
        try (Connection conn = DriverManager.getConnection(dbUrl);
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new StandingEvent(
                        rs.getString("captured_at"),
                        "sports-scraper",
                        rs.getInt("position"),
                        rs.getString("team_name"),
                        rs.getInt("points"),
                        rs.getInt("played_games")
                ));
            }
        } catch (SQLException e) {
            System.err.println("[Datamart] Error al leer clasificación: " + e.getMessage());
        }
        return list;
    }

    // ── Reports ───────────────────────────────────────────────────────────

    public void insertReport(MatchWeatherReport report) {
        String sql = """
            INSERT INTO match_weather_report
                (match_date, home_team, away_team, competition, home_score, away_score,
                 city, temp, humidity, weather_condition, condition_detail, captured_at)
            VALUES (?,?,?,?,?,?,?,?,?,?,?,?)
        """;
        try (Connection conn = DriverManager.getConnection(dbUrl);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, report.getMatchDate());
            ps.setString(2, report.getHomeTeam());
            ps.setString(3, report.getAwayTeam());
            ps.setString(4, report.getCompetition());
            ps.setInt(5, report.getHomeScore());
            ps.setInt(6, report.getAwayScore());
            ps.setString(7, report.getCity());
            ps.setDouble(8, report.getTemp());
            ps.setInt(9, report.getHumidity());
            ps.setString(10, report.getWeatherCondition());
            ps.setString(11, report.getConditionDetail());
            ps.setString(12, report.getCapturedAt());
            ps.executeUpdate();
            System.out.println("[Datamart] Informe guardado → " + report.getWeatherCondition());
        } catch (SQLException e) {
            System.err.println("[Datamart] Error al guardar informe: " + e.getMessage());
        }
    }

    public MatchWeatherReport getLatestReport() {
        String sql = "SELECT * FROM match_weather_report ORDER BY captured_at DESC LIMIT 1";
        try (Connection conn = DriverManager.getConnection(dbUrl);
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return buildReport(rs);
        } catch (SQLException e) {
            System.err.println("[Datamart] Error al leer informe: " + e.getMessage());
        }
        return null;
    }

    public List<MatchWeatherReport> getAllReports() {
        List<MatchWeatherReport> list = new ArrayList<>();
        String sql = "SELECT * FROM match_weather_report ORDER BY captured_at DESC";
        try (Connection conn = DriverManager.getConnection(dbUrl);
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(buildReport(rs));
        } catch (SQLException e) {
            System.err.println("[Datamart] Error al leer informes: " + e.getMessage());
        }
        return list;
    }

    private MatchWeatherReport buildReport(ResultSet rs) throws SQLException {
        MatchWeatherReport r = new MatchWeatherReport();
        r.setMatchDate(rs.getString("match_date"));
        r.setHomeTeam(rs.getString("home_team"));
        r.setAwayTeam(rs.getString("away_team"));
        r.setCompetition(rs.getString("competition"));
        r.setHomeScore(rs.getInt("home_score"));
        r.setAwayScore(rs.getInt("away_score"));
        r.setCity(rs.getString("city"));
        r.setTemp(rs.getDouble("temp"));
        r.setHumidity(rs.getInt("humidity"));
        r.setWeatherCondition(rs.getString("weather_condition"));
        r.setConditionDetail(rs.getString("condition_detail"));
        r.setCapturedAt(rs.getString("captured_at"));
        return r;
    }

    // ── Correlación de Pearson ────────────────────────────────────────────

    public Map<String, Object> getPearsonCorrelation() {
        String sql = """
            SELECT temp, home_score, away_score, home_team
            FROM match_weather_report
        """;

        List<Double> temps = new ArrayList<>();
        List<Double> goals = new ArrayList<>();

        try (Connection conn = DriverManager.getConnection(dbUrl);
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                double temp = rs.getDouble("temp");
                int homeScore = rs.getInt("home_score");
                int awayScore = rs.getInt("away_score");
                String homeTeam = rs.getString("home_team");

                double lasPalmasGoals = homeTeam.contains("Las Palmas") ? homeScore : awayScore;

                temps.add(temp);
                goals.add(lasPalmasGoals);
            }
        } catch (SQLException e) {
            System.err.println("[Datamart] Error al calcular correlación: " + e.getMessage());
        }

        if (temps.size() < 2) {
            return Map.of(
                    "error", "No hay suficientes datos para calcular la correlación.",
                    "partidos_analizados", temps.size()
            );
        }

        double pearson = calcularPearson(temps, goals);
        String interpretacion = interpretarPearson(pearson);

        return Map.of(
                "indice_pearson", Math.round(pearson * 1000.0) / 1000.0,
                "interpretacion", interpretacion,
                "variable_x", "temperatura (°C)",
                "variable_y", "goles de UD Las Palmas",
                "partidos_analizados", temps.size()
        );
    }

    private double calcularPearson(List<Double> x, List<Double> y) {
        int n = x.size();
        double mediaX = x.stream().mapToDouble(Double::doubleValue).average().orElse(0);
        double mediaY = y.stream().mapToDouble(Double::doubleValue).average().orElse(0);

        double numerador = 0;
        double denomX = 0;
        double denomY = 0;

        for (int i = 0; i < n; i++) {
            double dx = x.get(i) - mediaX;
            double dy = y.get(i) - mediaY;
            numerador += dx * dy;
            denomX += dx * dx;
            denomY += dy * dy;
        }

        double denominador = Math.sqrt(denomX * denomY);
        return denominador == 0 ? 0 : numerador / denominador;
    }

    private String interpretarPearson(double r) {
        if (r >= 0.7)  return "Correlación positiva fuerte: a mayor temperatura, más goles marca Las Palmas.";
        if (r >= 0.4)  return "Correlación positiva moderada: la temperatura parece favorecer el rendimiento.";
        if (r >= 0.1)  return "Correlación positiva débil: ligera tendencia pero no concluyente.";
        if (r > -0.1)  return "Sin correlación apreciable entre temperatura y goles.";
        if (r > -0.4)  return "Correlación negativa débil: ligera tendencia a menos goles con más calor.";
        if (r > -0.7)  return "Correlación negativa moderada: el calor parece perjudicar el rendimiento.";
        return "Correlación negativa fuerte: a mayor temperatura, menos goles marca Las Palmas.";
    }
}