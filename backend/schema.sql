CREATE TABLE IF NOT EXISTS telemetry (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    device_id TEXT NOT NULL,
    capacity_utilization REAL,
    temperature REAL,
    recorded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);