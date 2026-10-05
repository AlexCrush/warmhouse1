package warmhouse.telemetry;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class TelemetryServer {
    private static final Pattern LATEST_PATH =
            Pattern.compile("^/api/v1/telemetry/devices/([^/]+)/latest$");

    private final TelemetryStore store;
    private final int port;
    private final Gson gson = new Gson();

    public TelemetryServer(TelemetryStore store, int port) {
        this.store = store;
        this.port = port;
    }

    public void start() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/health", this::health);
        server.createContext("/api/v1/telemetry/samples", this::samples);
        server.createContext("/api/v1/telemetry/devices", this::devices);
        server.setExecutor(null);
        server.start();
        System.out.println("telemetry-service listening on :" + port);
    }

    private void health(HttpExchange exchange) throws IOException {
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            writeJson(exchange, 405, Map.of("error", "method_not_allowed"));
            return;
        }
        writeJson(exchange, 200, Map.of("status", "ok", "service", "telemetry-service"));
    }

    private void samples(HttpExchange exchange) throws IOException {
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            writeJson(exchange, 405, Map.of("error", "method_not_allowed"));
            return;
        }

        IngestRequest req;
        try {
            req = gson.fromJson(readBody(exchange), IngestRequest.class);
        } catch (JsonSyntaxException e) {
            writeJson(exchange, 400, Map.of("error", "bad_request", "message", "invalid JSON"));
            return;
        }

        if (req == null || req.deviceId == null || req.metric == null || req.value == null) {
            writeJson(exchange, 400, Map.of(
                    "error", "bad_request",
                    "message", "device_id, metric, value required"
            ));
            return;
        }

        TelemetrySample sample = new TelemetrySample(
                UUID.randomUUID().toString(),
                req.deviceId,
                req.metric,
                req.value,
                req.unit != null ? req.unit : "",
                req.recordedAt != null ? req.recordedAt : Instant.now().toString()
        );
        store.add(sample);
        writeJson(exchange, 201, sample);
    }

    private void devices(HttpExchange exchange) throws IOException {
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            writeJson(exchange, 405, Map.of("error", "method_not_allowed"));
            return;
        }

        Matcher matcher = LATEST_PATH.matcher(exchange.getRequestURI().getPath());
        if (!matcher.matches()) {
            writeJson(exchange, 404, Map.of("error", "not_found"));
            return;
        }

        String deviceId = matcher.group(1);
        Optional<String> metric = queryParam(exchange, "metric");
        List<TelemetrySample> latest = store.latest(deviceId, metric);
        Optional<String> lastSeen = store.lastSeen(deviceId);

        if (latest.isEmpty() && lastSeen.isEmpty()) {
            writeJson(exchange, 404, Map.of("error", "not_found", "message", "No telemetry for device"));
            return;
        }

        List<Map<String, Object>> samples = latest.stream().map(s -> {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("metric", s.getMetric());
            item.put("value", s.getValue());
            item.put("unit", s.getUnit());
            item.put("recorded_at", s.getRecordedAt());
            return item;
        }).collect(Collectors.toList());

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("device_id", deviceId);
        body.put("last_seen_at", lastSeen.orElse(""));
        body.put("samples", samples);
        writeJson(exchange, 200, body);
    }

    private static Optional<String> queryParam(HttpExchange exchange, String name) {
        String query = exchange.getRequestURI().getQuery();
        if (query == null) {
            return Optional.empty();
        }
        for (String part : query.split("&")) {
            String[] kv = part.split("=", 2);
            if (kv.length == 2 && name.equals(kv[0])) {
                return Optional.of(kv[1]);
            }
        }
        return Optional.empty();
    }

    private static String readBody(HttpExchange exchange) throws IOException {
        try (InputStream in = exchange.getRequestBody()) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private void writeJson(HttpExchange exchange, int status, Object body) throws IOException {
        byte[] bytes = gson.toJson(body).getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream out = exchange.getResponseBody()) {
            out.write(bytes);
        }
    }
}
