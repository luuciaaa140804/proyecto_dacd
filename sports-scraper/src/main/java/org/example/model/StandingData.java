package org.example.model;

public class StandingData {
    public final int position;
    public final String teamName;
    public final int played, won, draw, lost;
    public final int goalsFor, goalsAgainst, goalDiff, points;

    public StandingData(int position, String teamName, int played,
                        int won, int draw, int lost,
                        int goalsFor, int goalsAgainst, int goalDiff, int points) {
        this.position = position;
        this.teamName = teamName;
        this.played = played; this.won = won; this.draw = draw; this.lost = lost;
        this.goalsFor = goalsFor; this.goalsAgainst = goalsAgainst;
        this.goalDiff = goalDiff; this.points = points;
    }
}