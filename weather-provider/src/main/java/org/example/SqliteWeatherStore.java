package org.example;

import java.sql.*;
import java.time.Instant;

public class SqliteWeatherStore {
    private final String dbPath;

    public SqliteWeatherStore(String dbName) {
        // Esto creará el archivo .db enhttps://github.com/luuciaaa140804/proyecto_dacd la raíz de tu proyecto
        this.dbPath = "jdbc:sqlite:" + dbName;
        initDatabase();
    }

    private void initDatabase() {
        try (Connection conn = DriverManager.getConnection(dbPath);
             Statement stmt = conn.createStatement()) {

            // Creamos la tabla si no existe.
            // 'captured_at' es fundamental para cumplir el Sprint 1.
            String sql = "CREATE TABLE IF NOT EXISTS weather (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "location TEXT," +
                    "temp REAL," +
                    "humidity INTEGER," +
                    "captured_at TEXT" +
                    ")";
            stmt.execute(sql);
        } catch (SQLException e) {
            System.out.println("Error al inicializar SQLite: " + e.getMessage());
        }
    }

    public void insertWeather(String location, double temp, int humidity) {
        String sql = "INSERT INTO weather(location, temp, humidity, captured_at) VALUES(?,?,?,?)";

        try (Connection conn = DriverManager.getConnection(dbPath);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, location);
            pstmt.setDouble(2, temp);
            pstmt.setInt(3, humidity);
            pstmt.setString(4, Instant.now().toString()); // Marca temporal automática

            pstmt.executeUpdate();
            System.out.println("Datos guardados en SQLite correctamente.");
        } catch (SQLException e) {
            System.out.println("Error al insertar datos: " + e.getMessage());
        }
    }
}