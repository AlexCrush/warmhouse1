package warmhouse.telemetry;

import com.google.gson.annotations.SerializedName;

public class IngestRequest {
    @SerializedName("device_id")
    public String deviceId;

    public String metric;
    public Double value;
    public String unit;

    @SerializedName("recorded_at")
    public String recordedAt;
}
