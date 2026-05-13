package org.example;

public interface FootballStore {
    void insertMatch(int matchId, String competition, String matchDate,
                     String homeTeam, String awayTeam,
                     int homeScore, int awayScore, String status);
    void insertStanding(int position, String teamName, int played,
                        int won, int draw, int lost,
                        int goalsFor, int goalsAgainst,
                        int goalDiff, int points);
}