"""In-memory device registry."""

from threading import Lock
from typing import Dict, List, Optional

from models import Device


class DeviceRepository:
    def __init__(self) -> None:
        self._devices: Dict[int, Device] = {}
        self._lock = Lock()

    def upsert(self, device: Device) -> Device:
        with self._lock:
            self._devices[device.id] = device
            return device

    def get(self, device_id: int) -> Optional[Device]:
        with self._lock:
            return self._devices.get(device_id)

    def list_all(self) -> List[Device]:
        with self._lock:
            return list(self._devices.values())

    def delete(self, device_id: int) -> bool:
        with self._lock:
            return self._devices.pop(device_id, None) is not None
