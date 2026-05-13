package org.example;

import com.google.gson.JsonObject;

public interface WeatherProvider {
    JsonObject getSelectedData(String city) throws Exception;
}