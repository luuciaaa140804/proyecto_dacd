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

    public MatchEvent() {}

    public MatchEvent(String ts, String ss, String competition, String matchDate,
                      String homeTeam, String awayTeam, int homeScore, int awayScore) {
        this.ts          = ts;
        this.ss          = ss;
        this.type        = "match";
        this.competition = competition;
        this.match_date  = matchDate;
        this.home_team   = homeTeam;
        this.away_team   = awayTeam;
        this.home_score  = homeScore;
        this.away_score  = awayScore;
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

    public void setTs(String ts)              { this.ts = ts; }
    public void setSs(String ss)              { this.ss = ss; }
    public void setType(String type)          { this.type = type; }
    public void setCompetition(String c)      { this.competition = c; }
    public void setMatch_date(String d)       { this.match_date = d; }
    public void setHome_team(String h)        { this.home_team = h; }
    public void setAway_team(String a)        { this.away_team = a; }
    public void setHome_score(int s)          { this.home_score = s; }
    public void setAway_score(int s)          { this.away_score = s; }
}