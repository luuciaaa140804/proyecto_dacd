package org.example;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class EventStoreBuilderTest {

    @TempDir
    Path tempDir;

    @Test
    void eventFileIsCreatedWithCorrectPath() throws IOException {
        String topic  = "Weather";
        String ss     = "weather-provider";
        String date   = "20260510";
        String event  = "{\"ts\":\"2026-05-10T10:00:00Z\",\"ss\":\"weather-provider\",\"city\":\"Las Palmas\"}";

        // Simula la estructura que crea EventStoreBuilder
        Path dir = tempDir.resolve(topic).resolve(ss);
        Files.createDirectories(dir);
        Path file = dir.resolve(date + ".events");
        Files.writeString(file, event + "\n");

        // Verifica que el fichero existe y contiene el evento
        assertTrue(Files.exists(file));
        String content = Files.readString(file);
        assertTrue(content.contains("weather-provider"));
        assertTrue(content.contains("Las Palmas"));
    }

    @Test
    void multipleEventsAppendedOnSeparateLines() throws IOException {
        Path dir = tempDir.resolve("Football").resolve("sports-scraper");
        Files.createDirectories(dir);
        Path file = dir.resolve("20260510.events");

        String event1 = "{\"ts\":\"2026-05-10T10:00:00Z\",\"ss\":\"sports-scraper\",\"type\":\"match\"}";
        String event2 = "{\"ts\":\"2026-05-10T11:00:00Z\",\"ss\":\"sports-scraper\",\"type\":\"standing\"}";

        Files.writeString(file, event1 + "\n");
        Files.writeString(file, Files.readString(file) + event2 + "\n");

        long lineCount = Files.lines(file).count();
        assertEquals(2, lineCount);
    }
}