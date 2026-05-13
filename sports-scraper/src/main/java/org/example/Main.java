package org.example;

import org.example.model.MatchData;
import org.example.model.StandingData;

import java.util.List;
import java.util.concurrent.*;

public class Main {

    public static void main(String[] args) throws Exception {

        if (args.length < 1) {
            System.err.println("Uso: java -jar sports-scraper.jar <FOOTBALLDATA_API_KEY>");
            System.exit(1);
        }

        String apiKey = args[0];

        FootballApiClient client = new FootballApiClient(apiKey);
        FootballStore store = new SqliteFootballStore("football.db");
        FootballEventPublisher publisher = new FootballEventPublisher();

        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

        System.out.println("--- Capturador de Futbol (UD Las Palmas) Activo ---");

        scheduler.scheduleAtFixedRate(() -> {
            System.out.println("\n[" + java.time.LocalDateTime.now() + "] Iniciando captura...");

            // --- 1. Ultimos partidos ---
            try {
                List<MatchData> matches = client.getLastMatches(10);
                System.out.println("  Partidos recibidos: " + matches.size());

                for (MatchData m : matches) {
                    store.insertMatch(m.matchId, m.competition, m.matchDate,
                            m.homeTeam, m.awayTeam, m.homeScore, m.awayScore, m.status);

                    publisher.publishMatch(m.competition, m.matchDate,
                            m.homeTeam, m.awayTeam, m.homeScore, m.awayScore);

                    System.out.printf("  [Partido] %s vs %s  %d-%d  (%s)%n",
                            m.homeTeam, m.awayTeam, m.homeScore, m.awayScore,
                            m.matchDate.substring(0, 10));
                }

            } catch (Exception e) {
                System.err.println("  Error al obtener partidos: " + e.getMessage());
            }

            // --- 2. Clasificacion ---
            try {
                List<StandingData> standings = client.getStandings();
                System.out.println("  Equipos en clasificacion: " + standings.size());

                for (StandingData s : standings) {
                    store.insertStanding(s.position, s.teamName, s.played,
                            s.won, s.draw, s.lost,
                            s.goalsFor, s.goalsAgainst, s.goalDiff, s.points);

                    publisher.publishStanding(s.position, s.teamName, s.points, s.played);

                    if (s.teamName.contains("Las Palmas")) {
                        System.out.printf("  [Clasificacion] UD Las Palmas -> %do | %d pts | %d PJ%n",
                                s.position, s.points, s.played);
                    }
                }

            } catch (Exception e) {
                System.err.println("  Error al obtener clasificacion: " + e.getMessage());
            }

            System.out.println("[" + java.time.LocalDateTime.now() + "] Captura completada.");

        }, 0, 1, TimeUnit.HOURS);
    }
}