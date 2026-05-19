package org.example.model;

public class StandingEvent {

    private String ts;
    private String ss;
    private String type;
    private int    position;
    private String team_name;
    private int    points;
    private int    played_games;

    public StandingEvent() {}

    public StandingEvent(String ts, String ss, int position, String teamName,
                         int points, int playedGames) {
        this.ts           = ts;
        this.ss           = ss;
        this.type         = "standing";
        this.position     = position;
        this.team_name    = teamName;
        this.points       = points;
        this.played_games = playedGames;
    }

    public String getTs()          { return ts; }
    public String getSs()          { return ss; }
    public String getType()        { return type; }
    public int    getPosition()    { return position; }
    public String getTeamName()    { return team_name; }
    public int    getPoints()      { return points; }
    public int    getPlayedGames() { return played_games; }

    public void setTs(String ts)              { this.ts = ts; }
    public void setSs(String ss)              { this.ss = ss; }
    public void setType(String type)          { this.type = type; }
    public void setPosition(int p)            { this.position = p; }
    public void setTeam_name(String t)        { this.team_name = t; }
    public void setPoints(int p)              { this.points = p; }
    public void setPlayed_games(int g)        { this.played_games = g; }
}