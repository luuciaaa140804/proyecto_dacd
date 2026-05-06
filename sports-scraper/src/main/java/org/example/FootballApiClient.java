package org.example;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class FootballApiClient {

    private static final int UD_LAS_PALMAS_ID = 275;
    private static final String BASE_URL = "https://api.football-data.org/v4";

    private final String apiKey;
    private final OkHttpClient client;

    public FootballApiClient(String apiKey) {
        this.apiKey = apiKey;
        this.client = new OkHttpClient();
    }

    public JsonArray getLastMatches(int limit) throws Exception {
        String url = BASE_URL + "/teams/" + UD_LAS_PALMAS_ID + "/matches"
                + "?status=FINISHED&limit=" + limit;

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
            return body.getAsJsonArray("matches");
        }
    }

    public JsonArray getStandings() throws Exception {
        String url = BASE_URL + "/competitions/PD2/standings";

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