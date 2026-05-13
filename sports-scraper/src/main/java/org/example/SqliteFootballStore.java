package org.example;

import java.sql.*;
import java.time.Instant;

public class SqliteFootballStore implements FootballStore {

    private final String dbPath;

    public SqliteFootballStore(String dbName) {
        this.dbPath = "jdbc:sqlite:" + dbName;
        initDatabase();
    }

    private void initDatabase() {
        try (Connection conn = DriverManager.getConnection(dbPath);
             Statement stmt = conn.createStatement()) {

            stmt.execute(
                    "CREATE TABLE IF NOT EXISTS matches (" +
                            "    id             INTEGER PRIMARY KEY AUTOINCREMENT," +
                            "    match_id       INTEGER NOT NULL," +
                            "    competition    TEXT," +
                            "    match_date     TEXT," +
                            "    home_team      TEXT," +
                            "    away_team      TEXT," +
                            "    home_score     INTEGER," +
                            "    away_score     INTEGER," +
                            "    status         TEXT," +
                            "    captured_at    TEXT NOT NULL" +
                            ")"
            );

            stmt.execute(
                    "CREATE TABLE IF NOT EXISTS standings (" +
                            "    id             INTEGER PRIMARY KEY AUTOINCREMENT," +
                            "    position       INTEGER," +
                            "    team_name      TEXT," +
                            "    played_games   INTEGER," +
                            "    won            INTEGER," +
                            "    draw           INTEGER," +
                            "    lost           INTEGER," +
                            "    goals_for      INTEGER," +
                            "    goals_against  INTEGER," +
                            "    goal_diff      INTEGER," +
                            "    points         INTEGER," +
                            "    captured_at    TEXT NOT NULL" +
                            ")"
            );

            System.out.println("[SQLite] Tablas inicializadas correctamente.");

        } catch (SQLException e) {
            System.err.println("[SQLite] Error al inicializar: " + e.getMessage());
        }
    }

    public void insertMatch(int matchId, String competition, String matchDate,
                            String homeTeam, String awayTeam,
                            int homeScore, int awayScore, String status) {
        String sql =
                "INSERT INTO matches " +
                        "(match_id, competition, match_date, home_team, away_team, " +
                        " home_score, away_score, status, captured_at) " +
                        "VALUES (?,?,?,?,?,?,?,?,?)";

        try (Connection conn = DriverManager.getConnection(dbPath);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, matchId);
            pstmt.setString(2, competition);
            pstmt.setString(3, matchDate);
            pstmt.setString(4, homeTeam);
            pstmt.setString(5, awayTeam);
            pstmt.setInt(6, homeScore);
            pstmt.setInt(7, awayScore);
            pstmt.setString(8, status);
            pstmt.setString(9, Instant.now().toString());
            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("[SQLite] Error al insertar partido: " + e.getMessage());
        }
    }

    public void insertStanding(int position, String teamName, int playedGames,
                               int won, int draw, int lost,
                               int goalsFor, int goalsAgainst,
                               int goalDiff, int points) {
        String sql =
                "INSERT INTO standings " +
                        "(position, team_name, played_games, won, draw, lost, " +
                        " goals_for, goals_against, goal_diff, points, captured_at) " +
                        "VALUES (?,?,?,?,?,?,?,?,?,?,?)";

        try (Connection conn = DriverManager.getConnection(dbPath);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, position);
            pstmt.setString(2, teamName);
            pstmt.setInt(3, playedGames);
            pstmt.setInt(4, won);
            pstmt.setInt(5, draw);
            pstmt.setInt(6, lost);
            pstmt.setInt(7, goalsFor);
            pstmt.setInt(8, goalsAgainst);
            pstmt.setInt(9, goalDiff);
            pstmt.setInt(10, points);
            pstmt.setString(11, Instant.now().toString());
            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("[SQLite] Error al insertar clasificacion: " + e.getMessage());
        }
    }
}