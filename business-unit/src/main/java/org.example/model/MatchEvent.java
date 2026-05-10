package org.example.model;

public class MatchEvent {

    private String ts;
    private String ss;
    private String type;

    private String competition;
    private String match_date;
    private String home_team;
    private String away_team;
    private int    home_score;
    private int    away_score;

    private int    position;
    private String team_name;
    private int    points;
    private int    played_games;

    public MatchEvent() {}

    public static MatchEvent ofMatch(String ts, String competition, String matchDate,
                                     String homeTeam, String awayTeam,
                                     int homeScore, int awayScore) {
        MatchEvent e  = new MatchEvent();
        e.ts          = ts;
        e.ss          = "sports-scraper";
        e.type        = "match";
        e.competition = competition;
        e.match_date  = matchDate;
        e.home_team   = homeTeam;
        e.away_team   = awayTeam;
        e.home_score  = homeScore;
        e.away_score  = awayScore;
        return e;
    }

    public static MatchEvent ofStanding(String ts, int position, String teamName,
                                        int points, int playedGames) {
        MatchEvent e   = new MatchEvent();
        e.ts           = ts;
        e.ss           = "sports-scraper";
        e.type         = "standing";
        e.position     = position;
        e.team_name    = teamName;
        e.points       = points;
        e.played_games = playedGames;
        return e;
    }

    public String getTs()          { return ts; }
    public String getSs()          { return ss; }
    public String getType()        { return type; }
    public String getCompetition() { return competition; }
    public String getMatchDate()   { return match_date; }
    public String getHomeTeam()    { return home_team; }
    public String getAwayTeam()    { return away_team; }
    public int    getHomeScore()   { return home_score; }
    public int    getAwayScore()   { return away_score; }
    public int    getPosition()    { return position; }
    public String getTeamName()    { return team_name; }
    public int    getPoints()      { return points; }
    public int    getPlayedGames() { return played_games; }

    public void setTs(String ts)           { this.ts = ts; }
    public void setSs(String ss)           { this.ss = ss; }
    public void setType(String type)       { this.type = type; }
    public void setCompetition(String c)   { this.competition = c; }
    public void setMatch_date(String d)    { this.match_date = d; }
    public void setHome_team(String h)     { this.home_team = h; }
    public void setAway_team(String a)     { this.away_team = a; }
    public void setHome_score(int s)       { this.home_score = s; }
    public void setAway_score(int s)       { this.away_score = s; }
    public void setPosition(int p)         { this.position = p; }
    public void setTeam_name(String t)     { this.team_name = t; }
    public void setPoints(int p)           { this.points = p; }
    public void setPlayed_games(int g)     { this.played_games = g; }
}