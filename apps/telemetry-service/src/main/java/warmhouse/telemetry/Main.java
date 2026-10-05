package warmhouse.telemetry;

public class Main {
    public static void main(String[] args) throws Exception {
        int port = Integer.parseInt(System.getenv().getOrDefault("PORT", "8083"));
        TelemetryStore store = new TelemetryStore();
        TelemetryServer server = new TelemetryServer(store, port);
        server.start();
    }
}
