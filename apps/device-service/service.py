"""Application service for device registry."""

from typing import List, Optional

from models import Device
from repository import DeviceRepository


class DeviceService:
    def __init__(self, repository: DeviceRepository) -> None:
        self._repository = repository

    def register(self, payload: dict) -> Device:
        device = Device.from_dict(payload)
        return self._repository.upsert(device)

    def get(self, device_id: int) -> Optional[Device]:
        return self._repository.get(device_id)

    def list_devices(self) -> List[Device]:
        return self._repository.list_all()

    def remove(self, device_id: int) -> bool:
        return self._repository.delete(device_id)
