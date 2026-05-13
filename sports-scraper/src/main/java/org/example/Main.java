package org.example;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.concurrent.*;

public class Main {

    public static void main(String[] args) throws Exception {

        if (args.length < 1) {
            System.err.println("Uso: java -jar sports-scraper.jar <FOOTBALLDATA_API_KEY>");
            System.exit(1);
        }

        String apiKey = args[0];

        FootballApiClient client = new FootballApiClient(apiKey);
        SqliteFootballStore store = new SqliteFootballStore("football.db");
        FootballEventPublisher publisher = new FootballEventPublisher();

        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

        System.out.println("--- Capturador de Futbol (UD Las Palmas) Activo ---");

        scheduler.scheduleAtFixedRate(() -> {
            System.out.println("\n[" + java.time.LocalDateTime.now() + "] Iniciando captura...");

            // --- 1. Ultimos partidos ---
            try {
                JsonArray matches = client.getLastMatches(10);
                System.out.println("  Partidos recibidos: " + matches.size());

                for (int i = 0; i < matches.size(); i++) {
                    JsonObject match = matches.get(i).getAsJsonObject();

                    int matchId        = match.get("id").getAsInt();
                    String status      = match.get("status").getAsString();
                    String matchDate   = match.get("utcDate").getAsString();
                    String competition = match.getAsJsonObject("competition")
                            .get("name").getAsString();
                    String homeTeam    = match.getAsJsonObject("homeTeam")
                            .get("name").getAsString();
                    String awayTeam    = match.getAsJsonObject("awayTeam")
                            .get("name").getAsString();

                    JsonObject score = match.getAsJsonObject("score")
                            .getAsJsonObject("fullTime");
                    int homeScore = score.get("home").isJsonNull() ? -1
                            : score.get("home").getAsInt();
                    int awayScore = score.get("away").isJsonNull() ? -1
                            : score.get("away").getAsInt();

                    // Guardamos en SQLite (Sprint 1)
                    store.insertMatch(matchId, competition, matchDate,
                            homeTeam, awayTeam, homeScore, awayScore, status);

                    // Publicamos en ActiveMQ (Sprint 2)
                    publisher.publishMatch(competition, matchDate,
                            homeTeam, awayTeam, homeScore, awayScore);

                    System.out.printf("  [Partido] %s vs %s  %d-%d  (%s)%n",
                            homeTeam, awayTeam, homeScore, awayScore,
                            matchDate.substring(0, 10));
                }

            } catch (Exception e) {
                System.err.println("  Error al obtener partidos: " + e.getMessage());
            }

            // --- 2. Clasificacion ---
            try {
                JsonArray table = client.getStandings();
                System.out.println("  Equipos en clasificacion: " + table.size());

                for (int i = 0; i < table.size(); i++) {
                    JsonObject row = table.get(i).getAsJsonObject();

                    int position     = row.get("position").getAsInt();
                    String teamName  = row.getAsJsonObject("team").get("name").getAsString();
                    int played       = row.get("playedGames").getAsInt();
                    int won          = row.get("won").getAsInt();
                    int draw         = row.get("draw").getAsInt();
                    int lost         = row.get("lost").getAsInt();
                    int goalsFor     = row.get("goalsFor").getAsInt();
                    int goalsAgainst = row.get("goalsAgainst").getAsInt();
                    int goalDiff     = row.get("goalDifference").getAsInt();
                    int points       = row.get("points").getAsInt();

                    // Guardamos en SQLite (Sprint 1)
                    store.insertStanding(position, teamName, played, won, draw,
                            lost, goalsFor, goalsAgainst, goalDiff, points);

                    // Publicamos en ActiveMQ (Sprint 2)
                    publisher.publishStanding(position, teamName, points, played);

                    if (teamName.contains("Las Palmas")) {
                        System.out.printf("  [Clasificacion] UD Las Palmas -> %do | %d pts | %d PJ%n",
                                position, points, played);
                    }
                }

            } catch (Exception e) {
                System.err.println("  Error al obtener clasificacion: " + e.getMessage());
            }

            System.out.println("[" + java.time.LocalDateTime.now() + "] Captura completada.");

        }, 0, 1, TimeUnit.HOURS);
    }
}