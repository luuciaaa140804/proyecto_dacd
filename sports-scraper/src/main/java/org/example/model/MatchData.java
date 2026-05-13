package org.example.model;

public class MatchData {
    public final int matchId;
    public final String competition;
    public final String matchDate;
    public final String homeTeam;
    public final String awayTeam;
    public final int homeScore;
    public final int awayScore;
    public final String status;

    public MatchData(int matchId, String competition, String matchDate,
                     String homeTeam, String awayTeam,
                     int homeScore, int awayScore, String status) {
        this.matchId = matchId;
        this.competition = competition;
        this.matchDate = matchDate;
        this.homeTeam = homeTeam;
        this.awayTeam = awayTeam;
        this.homeScore = homeScore;
        this.awayScore = awayScore;
        this.status = status;
    }
}