package services

import (
	"bytes"
	"encoding/json"
	"fmt"
	"net/http"
	"time"
)

// TelemetryClient sends samples to telemetry-service (strangler MVP).
type TelemetryClient struct {
	BaseURL    string
	HTTPClient *http.Client
}

func NewTelemetryClient(baseURL string) *TelemetryClient {
	return &TelemetryClient{
		BaseURL: baseURL,
		HTTPClient: &http.Client{
			Timeout: 5 * time.Second,
		},
	}
}

type TelemetryPayload struct {
	DeviceID   string    `json:"device_id"`
	Metric     string    `json:"metric"`
	Value      float64   `json:"value"`
	Unit       string    `json:"unit"`
	RecordedAt time.Time `json:"recorded_at"`
}

func (c *TelemetryClient) Ingest(payload TelemetryPayload) error {
	if c.BaseURL == "" {
		return nil
	}
	body, err := json.Marshal(payload)
	if err != nil {
		return err
	}
	resp, err := c.HTTPClient.Post(c.BaseURL+"/api/v1/telemetry/samples", "application/json", bytes.NewReader(body))
	if err != nil {
		return err
	}
	defer resp.Body.Close()
	if resp.StatusCode >= 300 {
		return fmt.Errorf("telemetry-service status %d", resp.StatusCode)
	}
	return nil
}
