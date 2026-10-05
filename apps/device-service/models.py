"""Domain models for device-service."""


class Device:
    """Registered smart-home device (registry entry)."""

    def __init__(
        self,
        device_id: int,
        name: str,
        device_type: str,
        location: str,
        house_id: str = "default",
    ) -> None:
        self.id = device_id
        self.name = name
        self.type = device_type
        self.location = location
        self.house_id = house_id

    def to_dict(self) -> dict:
        return {
            "id": self.id,
            "name": self.name,
            "type": self.type,
            "location": self.location,
            "house_id": self.house_id,
        }

    @classmethod
    def from_dict(cls, data: dict) -> "Device":
        return cls(
            device_id=int(data["id"]),
            name=data["name"],
            device_type=data["type"],
            location=data["location"],
            house_id=data.get("house_id", "default"),
        )
