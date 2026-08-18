CREATE TABLE IF NOT EXISTS telemetry (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    device_id VARCHAR(50) NOT NULL,
    capacity_utilization DOUBLE,
    temperature DOUBLE,
    recorded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);