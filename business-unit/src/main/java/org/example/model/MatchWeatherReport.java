package org.example.model;

public class MatchWeatherReport {

    private String matchDate;
    private String homeTeam;
    private String awayTeam;
    private String competition;
    private int    homeScore;
    private int    awayScore;

    private String city;
    private double temp;
    private int    humidity;

    private String weatherCondition;
    private String conditionDetail;
    private String capturedAt;

    public MatchWeatherReport() {}

    public void setMatchDate(String matchDate)        { this.matchDate = matchDate; }
    public void setHomeTeam(String homeTeam)          { this.homeTeam = homeTeam; }
    public void setAwayTeam(String awayTeam)          { this.awayTeam = awayTeam; }
    public void setCompetition(String competition)    { this.competition = competition; }
    public void setHomeScore(int homeScore)           { this.homeScore = homeScore; }
    public void setAwayScore(int awayScore)           { this.awayScore = awayScore; }
    public void setCity(String city)                  { this.city = city; }
    public void setTemp(double temp)                  { this.temp = temp; }
    public void setHumidity(int humidity)             { this.humidity = humidity; }
    public void setWeatherCondition(String condition) { this.weatherCondition = condition; }
    public void setConditionDetail(String detail)     { this.conditionDetail = detail; }
    public void setCapturedAt(String capturedAt)      { this.capturedAt = capturedAt; }

    public String getMatchDate()        { return matchDate; }
    public String getHomeTeam()         { return homeTeam; }
    public String getAwayTeam()         { return awayTeam; }
    public String getCompetition()      { return competition; }
    public int    getHomeScore()        { return homeScore; }
    public int    getAwayScore()        { return awayScore; }
    public String getCity()             { return city; }
    public double getTemp()             { return temp; }
    public int    getHumidity()         { return humidity; }
    public String getWeatherCondition() { return weatherCondition; }
    public String getConditionDetail()  { return conditionDetail; }
    public String getCapturedAt()       { return capturedAt; }

    public void evaluateConditions() {
        if (temp < 5.0 || humidity > 90) {
            this.weatherCondition = "RIESGO";
            this.conditionDetail  = "Condiciones extremas: posible cancelación o mal juego.";
        } else if (temp < 10.0 || humidity > 75) {
            this.weatherCondition = "ADVERSO";
            this.conditionDetail  = "Condiciones difíciles: puede afectar al rendimiento.";
        } else {
            this.weatherCondition = "FAVORABLE";
            this.conditionDetail  = "Buenas condiciones para el partido.";
        }
    }
}