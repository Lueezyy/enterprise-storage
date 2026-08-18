package com.telemetry;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class Database {
    private static final String DB_URL = "jdbc:sqlite:telemetry.db";

    public static Connection connect() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    public static void initSchema() {
        String sql = """
                CREATE TABLE IF NOT EXISTS telemetry (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    device_id TEXT NOT NULL,
                    capacity_utilization REAL,
                    temperature REAL,
                    recorded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                );
                """;
            
        try (Connection conn = connect();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            System.out.println("telemetry table ready.");
        } catch (SQLException e) {
                System.err.println("Failed to initialize schema: " + e.getMessage());
        }
    }
}