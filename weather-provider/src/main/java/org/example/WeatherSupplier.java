package org.example;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class WeatherSupplier implements WeatherProvider {
    private final String apiKey;
    private final OkHttpClient client;

    public WeatherSupplier(String apiKey) {
        this.apiKey = apiKey;
        this.client = new OkHttpClient();
    }

    public JsonObject getSelectedData(String city) throws Exception {
        // Usamos unidades métricas para Celsius
        String url = "https://api.openweathermap.org/data/2.5/weather?q=" + city + "&appid=" + apiKey + "&units=metric";

        Request request = new Request.Builder().url(url).build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new Exception("Error en API: " + response);
            return JsonParser.parseString(response.body().string()).getAsJsonObject();
        }
    }
}