package services

import (
	"bytes"
	"encoding/json"
	"fmt"
	"net/http"
	"time"
)

// DeviceClient registers devices in device-service (strangler MVP).
type DeviceClient struct {
	BaseURL    string
	HTTPClient *http.Client
}

func NewDeviceClient(baseURL string) *DeviceClient {
	return &DeviceClient{
		BaseURL: baseURL,
		HTTPClient: &http.Client{
			Timeout: 5 * time.Second,
		},
	}
}

type DevicePayload struct {
	ID       int    `json:"id"`
	Name     string `json:"name"`
	Type     string `json:"type"`
	Location string `json:"location"`
	HouseID  string `json:"house_id,omitempty"`
}

func (c *DeviceClient) Register(payload DevicePayload) error {
	if c.BaseURL == "" {
		return nil
	}
	body, err := json.Marshal(payload)
	if err != nil {
		return err
	}
	resp, err := c.HTTPClient.Post(c.BaseURL+"/api/v1/devices", "application/json", bytes.NewReader(body))
	if err != nil {
		return err
	}
	defer resp.Body.Close()
	if resp.StatusCode >= 300 {
		return fmt.Errorf("device-service status %d", resp.StatusCode)
	}
	return nil
}
