package warmhouse.telemetry;

import com.google.gson.annotations.SerializedName;

public class TelemetrySample {
    private final String id;

    @SerializedName("device_id")
    private final String deviceId;

    private final String metric;
    private final double value;
    private final String unit;

    @SerializedName("recorded_at")
    private final String recordedAt;

    public TelemetrySample(
            String id,
            String deviceId,
            String metric,
            double value,
            String unit,
            String recordedAt
    ) {
        this.id = id;
        this.deviceId = deviceId;
        this.metric = metric;
        this.value = value;
        this.unit = unit;
        this.recordedAt = recordedAt;
    }

    public String getId() {
        return id;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public String getMetric() {
        return metric;
    }

    public double getValue() {
        return value;
    }

    public String getUnit() {
        return unit;
    }

    public String getRecordedAt() {
        return recordedAt;
    }
}
