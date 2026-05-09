package org.example;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class FootballApiClient {

    // Código de LaLiga en football-data.org (tier gratuito incluido)
    private static final String BASE_URL = "https://api.football-data.org/v4";
    private static final String LALIGA_CODE = "PD";   // Primera División
    private static final int SEASON = 2024;           // Temporada 2024/25

    private final String apiKey;
    private final OkHttpClient client;

    public FootballApiClient(String apiKey) {
        this.apiKey = apiKey;
        this.client = new OkHttpClient();
    }

    /**
     * Devuelve los partidos de LaLiga 2024/25 donde jugó la UD Las Palmas.
     */
    public JsonArray getLastMatches(int limit) throws Exception {
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

            // Filtramos solo los partidos donde jugó Las Palmas
            JsonArray laspalmasMatches = new JsonArray();
            for (int i = 0; i < allMatches.size(); i++) {
                JsonObject match = allMatches.get(i).getAsJsonObject();
                String home = match.getAsJsonObject("homeTeam").get("name").getAsString();
                String away = match.getAsJsonObject("awayTeam").get("name").getAsString();
                if (home.contains("Las Palmas") || away.contains("Las Palmas")) {
                    laspalmasMatches.add(match);
                    if (laspalmasMatches.size() >= limit) break;
                }
            }
            return laspalmasMatches;
        }
    }

    /**
     * Devuelve la clasificación final de LaLiga 2024/25.
     */
    public JsonArray getStandings() throws Exception {
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
            return body.getAsJsonArray("standings")
                    .get(0).getAsJsonObject()
                    .getAsJsonArray("table");
        }
    }
}