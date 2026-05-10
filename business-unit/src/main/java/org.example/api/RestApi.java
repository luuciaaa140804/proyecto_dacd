package org.example.api;

import com.google.gson.Gson;
import io.javalin.Javalin;
import io.javalin.http.Context;
import org.example.datamart.DatamartRepository;
import org.example.model.MatchEvent;
import org.example.model.MatchWeatherReport;
import org.example.model.WeatherEvent;

import java.util.List;
import java.util.Map;

/**
 * API REST del business-unit usando Javalin.
 *
 * Endpoints disponibles:
 *
 *   GET /report/laspalmas       → Informe clima + último partido de UD Las Palmas
 *   GET /report/history         → Todos los informes generados
 *   GET /standings              → Clasificación actual de LaLiga
 *   GET /weather/current        → Último dato de clima capturado para Las Palmas
 *   GET /matches/laspalmas      → Historial de partidos de Las Palmas
 */
public class RestApi {

    private final DatamartRepository datamart;
    private final Gson gson = new Gson();
    private Javalin app;

    public RestApi(DatamartRepository datamart) {
        this.datamart = datamart;
    }

    /** Arranca el servidor en el puerto indicado. */
    public void start(int port) {
        app = Javalin.create(config -> {
            config.bundledPlugins.enableCors(cors -> cors.addRule(it -> it.anyHost()));
        }).start(port);

        registerRoutes();

        System.out.println("[RestApi] Servidor iniciado en http://localhost:" + port);
        System.out.println("[RestApi] Endpoints disponibles:");
        System.out.println("  GET /report/laspalmas  → Informe clima + partido");
        System.out.println("  GET /report/history    → Historial de informes");
        System.out.println("  GET /standings         → Clasificación LaLiga");
        System.out.println("  GET /weather/current   → Clima actual Las Palmas");
        System.out.println("  GET /matches/laspalmas → Partidos de Las Palmas");
    }

    // ── Registro de rutas ──────────────────────────────────────────────────

    private void registerRoutes() {

        // Raíz: ayuda rápida
        app.get("/", ctx -> {
            ctx.json(Map.of(
                    "service", "Business Unit - UD Las Palmas",
                    "endpoints", List.of(
                            "GET /report/laspalmas  - Informe clima + último partido",
                            "GET /report/history    - Historial de informes",
                            "GET /standings         - Clasificación LaLiga",
                            "GET /weather/current   - Clima actual en Las Palmas",
                            "GET /matches/laspalmas - Partidos de Las Palmas"
                    )
            ));
        });

        // Informe combinado más reciente
        app.get("/report/laspalmas", this::getLatestReport);

        // Historial de todos los informes
        app.get("/report/history", this::getReportHistory);

        // Clasificación de LaLiga
        app.get("/standings", this::getStandings);

        // Clima actual
        app.get("/weather/current", this::getCurrentWeather);

        // Partidos de Las Palmas
        app.get("/matches/laspalmas", this::getLasPalmasMatches);
    }

    // ── Handlers ──────────────────────────────────────────────────────────

    /**
     * GET /report/laspalmas
     * Devuelve el informe más reciente con datos de clima y partido de Las Palmas.
     */
    private void getLatestReport(Context ctx) {
        MatchWeatherReport report = datamart.getLatestReport();

        if (report == null) {
            ctx.status(404).json(Map.of(
                    "error", "No hay informes disponibles aún.",
                    "hint", "Espera a que los feeders publiquen al menos un evento de clima y uno de partido."
            ));
            return;
        }

        ctx.json(Map.of(
                "partido", Map.of(
                        "fecha",       report.getMatchDate(),
                        "local",       report.getHomeTeam(),
                        "visitante",   report.getAwayTeam(),
                        "competicion", report.getCompetition(),
                        "resultado",   report.getHomeScore() + " - " + report.getAwayScore()
                ),
                "clima", Map.of(
                        "ciudad",    report.getCity(),
                        "temp_c",    report.getTemp(),
                        "humedad_%", report.getHumidity()
                ),
                "valoracion", Map.of(
                        "condicion", report.getWeatherCondition(),
                        "detalle",   report.getConditionDetail()
                ),
                "generado_en", report.getCapturedAt()
        ));
    }

    /**
     * GET /report/history
     * Devuelve todos los informes generados.
     */
    private void getReportHistory(Context ctx) {
        List<MatchWeatherReport> reports = datamart.getAllReports();

        if (reports.isEmpty()) {
            ctx.json(Map.of("informes", List.of(), "total", 0));
            return;
        }

        List<Map<String, Object>> result = reports.stream().map(r -> Map.<String, Object>of(
                "fecha_partido", r.getMatchDate(),
                "local",         r.getHomeTeam(),
                "visitante",     r.getAwayTeam(),
                "temp_c",        r.getTemp(),
                "humedad_%",     r.getHumidity(),
                "condicion",     r.getWeatherCondition(),
                "generado_en",   r.getCapturedAt()
        )).toList();

        ctx.json(Map.of("informes", result, "total", result.size()));
    }

    /**
     * GET /standings
     * Devuelve la clasificación actual de LaLiga.
     */
    private void getStandings(Context ctx) {
        List<MatchEvent> standings = datamart.getStandings();

        if (standings.isEmpty()) {
            ctx.status(404).json(Map.of(
                    "error", "Clasificación no disponible aún.",
                    "hint", "Espera a que el feeder de fútbol publique datos de clasificación."
            ));
            return;
        }

        List<Map<String, Object>> result = standings.stream().map(s -> Map.<String, Object>of(
                "posicion",     s.getPosition(),
                "equipo",       s.getTeamName(),
                "puntos",       s.getPoints(),
                "partidos_jug", s.getPlayedGames()
        )).toList();

        ctx.json(Map.of("clasificacion", result, "total_equipos", result.size()));
    }

    /**
     * GET /weather/current
     * Devuelve el último dato de clima para Las Palmas.
     */
    private void getCurrentWeather(Context ctx) {
        WeatherEvent weather = datamart.getLatestWeather("Las Palmas");

        if (weather == null) {
            ctx.status(404).json(Map.of(
                    "error", "No hay datos de clima disponibles aún.",
                    "hint", "Espera a que el feeder de clima publique al menos un evento."
            ));
            return;
        }

        ctx.json(Map.of(
                "ciudad",       weather.getCity(),
                "temperatura_c",weather.getTemp(),
                "humedad_%",    weather.getHumidity(),
                "capturado_en", weather.getTs()
        ));
    }

    /**
     * GET /matches/laspalmas
     * Devuelve el historial de partidos de UD Las Palmas.
     */
    private void getLasPalmasMatches(Context ctx) {
        List<MatchEvent> matches = datamart.getAllLasPalmasMatches();

        if (matches.isEmpty()) {
            ctx.json(Map.of("partidos", List.of(), "total", 0));
            return;
        }

        List<Map<String, Object>> result = matches.stream().map(m -> Map.<String, Object>of(
                "fecha",       m.getMatchDate(),
                "local",       m.getHomeTeam(),
                "visitante",   m.getAwayTeam(),
                "resultado",   m.getHomeScore() + " - " + m.getAwayScore(),
                "competicion", m.getCompetition()
        )).toList();

        ctx.json(Map.of("partidos", result, "total", result.size()));
    }

    /** Detiene el servidor. */
    public void stop() {
        if (app != null) app.stop();
    }
}