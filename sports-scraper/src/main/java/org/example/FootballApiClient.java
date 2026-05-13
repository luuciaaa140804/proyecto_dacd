package org.example;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.example.model.MatchData;
import org.example.model.StandingData;

import java.util.ArrayList;
import java.util.List;

public class FootballApiClient {

    private static final String BASE_URL = "https://api.football-data.org/v4";
    private static final String LALIGA_CODE = "PD";
    private static final int SEASON = 2024;

    private final String apiKey;
    private final OkHttpClient client;

    public FootballApiClient(String apiKey) {
        this.apiKey = apiKey;
        this.client = new OkHttpClient();
    }

    public List<MatchData> getLastMatches(int limit) throws Exception {
        String url = BASE_URL + "/competitions/" + LALIGA_CODE
                + "/matches?season=" + SEASON + "&status=FINISHED";

        Request request = new Request.Builder()
                .url(url)
                .addHeader("X-Auth-Token", apiKey)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new Exception("Error en API football-data.org: "
                        + response.code() + " - " + response.message());
            }
            JsonObject body = JsonParser.parseString(response.body().string()).getAsJsonObject();
            JsonArray allMatches = body.getAsJsonArray("matches");

            List<MatchData> result = new ArrayList<>();
            for (int i = 0; i < allMatches.size(); i++) {
                JsonObject m = allMatches.get(i).getAsJsonObject();
                String home = m.getAsJsonObject("homeTeam").get("name").getAsString();
                String away = m.getAsJsonObject("awayTeam").get("name").getAsString();

                if (home.contains("Las Palmas") || away.contains("Las Palmas")) {
                    JsonObject score = m.getAsJsonObject("score").getAsJsonObject("fullTime");
                    result.add(new MatchData(
                            m.get("id").getAsInt(),
                            m.getAsJsonObject("competition").get("name").getAsString(),
                            m.get("utcDate").getAsString(),
                            home,
                            away,
                            score.get("home").isJsonNull() ? -1 : score.get("home").getAsInt(),
                            score.get("away").isJsonNull() ? -1 : score.get("away").getAsInt(),
                            m.get("status").getAsString()
                    ));
                    if (result.size() >= limit) break;
                }
            }
            return result;
        }
    }

    public List<StandingData> getStandings() throws Exception {
        String url = BASE_URL + "/competitions/" + LALIGA_CODE
                + "/standings?season=" + SEASON;

        Request request = new Request.Builder()
                .url(url)
                .addHeader("X-Auth-Token", apiKey)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new Exception("Error al obtener clasificacion: "
                        + response.code() + " - " + response.message());
            }
            JsonObject body = JsonParser.parseString(response.body().string()).getAsJsonObject();
            JsonArray table = body.getAsJsonArray("standings")
                    .get(0).getAsJsonObject()
                    .getAsJsonArray("table");

            List<StandingData> result = new ArrayList<>();
            for (int i = 0; i < table.size(); i++) {
                JsonObject r = table.get(i).getAsJsonObject();
                result.add(new StandingData(
                        r.get("position").getAsInt(),
                        r.getAsJsonObject("team").get("name").getAsString(),
                        r.get("playedGames").getAsInt(),
                        r.get("won").getAsInt(),
                        r.get("draw").getAsInt(),
                        r.get("lost").getAsInt(),
                        r.get("goalsFor").getAsInt(),
                        r.get("goalsAgainst").getAsInt(),
                        r.get("goalDifference").getAsInt(),
                        r.get("points").getAsInt()
                ));
            }
            return result;
        }
    }
}