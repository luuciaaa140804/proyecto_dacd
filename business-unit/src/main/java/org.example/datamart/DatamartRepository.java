package org.example.datamart;

import org.example.model.MatchEvent;
import org.example.model.MatchWeatherReport;
import org.example.model.WeatherEvent;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

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
                    captured_at TEXT
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

    public void insertMatch(MatchEvent event) {
        String sql = """
            INSERT INTO match_history
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
            ORDER BY captured_at DESC
            LIMIT 1
        """;
        try (Connection conn = DriverManager.getConnection(dbUrl);
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return MatchEvent.ofMatch(
                        rs.getString("captured_at"),
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
                list.add(MatchEvent.ofMatch(
                        rs.getString("captured_at"),
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

    public void upsertStanding(MatchEvent event) {
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

    public List<MatchEvent> getStandings() {
        List<MatchEvent> list = new ArrayList<>();
        String sql = "SELECT * FROM standings ORDER BY position ASC";
        try (Connection conn = DriverManager.getConnection(dbUrl);
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(MatchEvent.ofStanding(
                        rs.getString("captured_at"),
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
            if (rs.next()) {
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
            while (rs.next()) {
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
                list.add(r);
            }
        } catch (SQLException e) {
            System.err.println("[Datamart] Error al leer informes: " + e.getMessage());
        }
        return list;
    }
}