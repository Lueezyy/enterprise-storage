package com.telemetry;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class Database {

    public static class TelemetryRow {
        public long id;
        public String deviceId;
        public double capacityUtilization;
        public double temperature;
        public String recordedAt;
    }

    private static final String DB_URL = "jdbc:sqlite:telemetry.db";

    public static Connection connect() throws SQLException {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new SQLException("SQLite JDBC driver not found in classpath", e);
        }
        Connection conn = DriverManager.getConnection(DB_URL);
        try (Statement pragma = conn.createStatement()) {
            pragma.execute("PRAGMA foreign_keys = ON");
        }
        return conn;
    }

    public static void insertTelemetry(String deviceId, double capacityUtilization, double temperature) throws SQLException {
        String sql = "INSERT INTO telemetry (device_id, capacity_utilization, temperature) VALUES (?, ?, ?)";

        try (Connection conn = connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, deviceId);
            stmt.setDouble(2, capacityUtilization);
            stmt.setDouble(3, temperature);
            stmt.executeUpdate();
        }
    }

    public static List<TelemetryRow> getAllTelemetry() throws SQLException {
        String sql = "SELECT id, device_id, capacity_utilization, temperature, recorded_at " + 
                     "FROM telemetry ORDER BY recorded_at DESC";
        
        List<TelemetryRow> rows = new ArrayList<>();

        try (Connection conn = connect();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                TelemetryRow row = new TelemetryRow();
                row.id = rs.getLong("id");
                row.deviceId = rs.getString("device_id");
                row.capacityUtilization = rs.getDouble("capacity_utilization");
                row.temperature = rs.getDouble("temperature");
                row.recordedAt = rs.getString("recorded_at");
                rows.add(row);
            }
        }

        return rows;
    }

    public static void initSchema() {
        String createServers = """
            CREATE TABLE IF NOT EXISTS servers (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                hostname TEXT NOT NULL,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            );
            """;
            
        String createStorageDevices = """
            CREATE TABLE IF NOT EXISTS storage_devices (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                server_id INTEGER NOT NULL,
                device_name TEXT NOT NULL,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (server_id) REFERENCES servers(id)
            );
            """;

        String createTelemetry = """
            CREATE TABLE IF NOT EXISTS telemetry (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                device_id INTEGER NOT NULL,
                capacity_utilization REAL,
                temperature REAL,
                recorded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (device_id) REFERENCES storage_devices(id)
            );
            """;
        
        try (Connection conn = connect();
             Statement stmt = conn.createStatement()) {
            stmt.execute(createServers);
            stmt.execute(createStorageDevices);
            stmt.execute(createTelemetry);
            System.out.println("Schema ready: servers, storage_devices, telemetry.");
        } catch (SQLException e) {
            System.err.println("Failed to initialize schema: " + e.getMessage());
        }
    }
}