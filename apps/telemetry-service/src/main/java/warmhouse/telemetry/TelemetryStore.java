package warmhouse.telemetry;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class TelemetryStore {
    private final Map<String, List<TelemetrySample>> samplesByDevice = new ConcurrentHashMap<>();
    private final Map<String, String> lastSeenByDevice = new ConcurrentHashMap<>();

    public TelemetrySample add(TelemetrySample sample) {
        samplesByDevice
                .computeIfAbsent(sample.getDeviceId(), ignored -> new ArrayList<>())
                .add(sample);
        lastSeenByDevice.put(sample.getDeviceId(), sample.getRecordedAt());
        return sample;
    }

    public List<TelemetrySample> latest(String deviceId, Optional<String> metric) {
        List<TelemetrySample> samples = samplesByDevice.getOrDefault(deviceId, List.of());
        return samples.stream()
                .filter(s -> metric.map(m -> m.equals(s.getMetric())).orElse(true))
                .collect(Collectors.groupingBy(TelemetrySample::getMetric))
                .values()
                .stream()
                .map(list -> list.stream()
                        .max(Comparator.comparing(TelemetrySample::getRecordedAt))
                        .orElseThrow())
                .toList();
    }

    public Optional<String> lastSeen(String deviceId) {
        return Optional.ofNullable(lastSeenByDevice.get(deviceId));
    }
}
