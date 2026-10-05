"""device-service: реестр устройств (MVP strangler рядом с монолитом)."""

from flask import Flask, jsonify, request

from repository import DeviceRepository
from service import DeviceService

app = Flask(__name__)
device_service = DeviceService(DeviceRepository())


@app.get("/health")
def health():
    return jsonify({"status": "ok", "service": "device-service"})


@app.get("/api/v1/devices")
def list_devices():
    devices = [d.to_dict() for d in device_service.list_devices()]
    return jsonify(devices)


@app.get("/api/v1/devices/<int:device_id>")
def get_device(device_id: int):
    device = device_service.get(device_id)
    if device is None:
        return jsonify({"error": "device not found"}), 404
    return jsonify(device.to_dict())


@app.post("/api/v1/devices")
def register_device():
    payload = request.get_json(silent=True) or {}
    required = ("id", "name", "type", "location")
    missing = [k for k in required if k not in payload]
    if missing:
        return jsonify({"error": f"missing fields: {', '.join(missing)}"}), 400
    device = device_service.register(payload)
    return jsonify(device.to_dict()), 201


@app.delete("/api/v1/devices/<int:device_id>")
def delete_device(device_id: int):
    if not device_service.remove(device_id):
        return jsonify({"error": "device not found"}), 404
    return jsonify({"message": "deleted"}), 200


if __name__ == "__main__":
    app.run(host="0.0.0.0", port=8082)
