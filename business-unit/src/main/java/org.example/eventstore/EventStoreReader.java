package org.example.eventstore;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.example.datamart.DatamartRepository;
import org.example.model.MatchEvent;
import org.example.model.StandingEvent;
import org.example.model.WeatherEvent;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

public class EventStoreReader {

    private static final String EVENTSTORE_PATH = "eventstore";
    private final Gson gson = new Gson();
    private final DatamartRepository datamart;

    public EventStoreReader(DatamartRepository datamart) {
        this.datamart = datamart;
    }

    public void loadHistoricalEvents() {
        System.out.println("[EventStoreReader] Cargando eventos históricos...");
        int total = 0;
        total += loadWeatherHistory();
        total += loadFootballHistory();
        System.out.println("[EventStoreReader] Total eventos históricos cargados: " + total);
    }

    private int loadWeatherHistory() {
        Path dir = Paths.get(EVENTSTORE_PATH, "Weather", "weather-provider");
        if (!Files.exists(dir)) {
            System.out.println("[EventStoreReader] No se encontró directorio Weather: " + dir);
            return 0;
        }
        int count = 0;
        for (String line : readAllLines(dir)) {
            try {
                WeatherEvent event = gson.fromJson(line, WeatherEvent.class);
                if (event.getCity() != null) {
                    datamart.upsertWeather(event);
                    count++;
                }
            } catch (Exception e) {
                System.err.println("[EventStoreReader] Línea Weather inválida: " + e.getMessage());
            }
        }
        System.out.println("[EventStoreReader] Eventos de clima cargados: " + count);
        return count;
    }

    private int loadFootballHistory() {
        Path dir = Paths.get(EVENTSTORE_PATH, "Football", "sports-scraper");
        if (!Files.exists(dir)) {
            System.out.println("[EventStoreReader] No se encontró directorio Football: " + dir);
            return 0;
        }
        int count = 0;
        for (String line : readAllLines(dir)) {
            try {
                JsonObject json = JsonParser.parseString(line).getAsJsonObject();
                String type = json.has("type") ? json.get("type").getAsString() : "";

                if ("match".equals(type)) {
                    MatchEvent event = gson.fromJson(json, MatchEvent.class);
                    if (isLasPalmasMatch(event)) {
                        datamart.insertMatch(event);
                        count++;
                    }
                } else if ("standing".equals(type)) {
                    StandingEvent event = gson.fromJson(json, StandingEvent.class);
                    datamart.upsertStanding(event);
                    count++;
                }
            } catch (Exception e) {
                System.err.println("[EventStoreReader] Línea Football inválida: " + e.getMessage());
            }
        }
        System.out.println("[EventStoreReader] Eventos de fútbol cargados: " + count);
        return count;
    }

    private List<String> readAllLines(Path directory) {
        List<String> lines = new ArrayList<>();
        try {
            List<Path> files = new ArrayList<>();
            Files.walkFileTree(directory, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    if (file.toString().endsWith(".events")) files.add(file);
                    return FileVisitResult.CONTINUE;
                }
            });
            files.sort(Path::compareTo);
            for (Path file : files) {
                try (BufferedReader reader = new BufferedReader(new FileReader(file.toFile()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        line = line.trim();
                        if (!line.isEmpty()) lines.add(line);
                    }
                } catch (IOException e) {
                    System.err.println("[EventStoreReader] Error leyendo " + file + ": " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("[EventStoreReader] Error al explorar directorio: " + e.getMessage());
        }
        return lines;
    }

    private boolean isLasPalmasMatch(MatchEvent event) {
        String home = event.getHomeTeam();
        String away = event.getAwayTeam();
        return (home != null && home.contains("Las Palmas"))
                || (away != null && away.contains("Las Palmas"));
    }
}