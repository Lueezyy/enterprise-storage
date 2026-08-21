package com.telemetry;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import com.google.gson.Gson;
import java.net.InetSocketAddress;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;

public class Main {

    static class TelemetryPayload {
        long deviceId;
        double capacityUtilization;
        double temperature;
    }

    public static void main(String[] args) throws IOException {
        System.out.println("Backend skeleton is alive.");
        Database.initSchema();
        seedData();

        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.setExecutor(null);

        server.createContext("/api/telemetry", new TelemetryHandler());
        server.createContext("/api/servers", new ServersHandler());
        server.createContext("/api/devices", new DevicesHandler());

        server.start();
        System.out.println("Server listening on http://localhost:8080");
    }

    private static void seedData() {
        try {
            if (Database.countServers() > 0) {
                System.out.println("Seed data already present, skipping.");
                return;
            }

            long server1 = Database.insertServer("server-01");
            long server2 = Database.insertServer("server-02");

            Database.insertStorageDevice(server1, "disk-01");
            Database.insertStorageDevice(server1, "disk-02");
            Database.insertStorageDevice(server2, "disk-01");

            System.out.println("Seed data created: 2 servers, 3 storage devices.");
        } catch (SQLException e) {
            System.err.println("Failed to seed data: " + e.getMessage());
        }
    }

    private static void sendResponse(HttpExchange exchange, int statusCode, String message) throws IOException {
        byte[] responseBytes = message.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(statusCode, responseBytes.length);
        OutputStream os = exchange.getResponseBody();
        os.write(responseBytes);
        os.close();
    }

    private static void writeJson(HttpExchange exchange, Object data, Gson gson) throws IOException {
        String json = gson.toJson(data);
        byte[] responseBytes = json.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, responseBytes.length);
        OutputStream os = exchange.getResponseBody();
        os.write(responseBytes);
        os.close();
    }

    static class ServersHandler implements HttpHandler {
        private static final Gson gson = new Gson();

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendResponse(exchange, 405, "Only GET is supported");
                return;
            }
            try {
                var servers = Database.getAllServers();
                writeJson(exchange, servers, gson);
            } catch (Exception e) {
                e.printStackTrace();
                sendResponse(exchange, 500, "Failed to fetch servers: " + e.getMessage());
            }
        }
    }

    static class DevicesHandler implements HttpHandler {
        private static final Gson gson = new Gson();

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendResponse(exchange, 405, "Only GET is supported");
                return;
            }

            String path = exchange.getRequestURI().getPath(); // e.g. /api/devices or /api/devices/2
            String[] parts = path.split("/");

            try {
                if (parts.length > 3) {
                    long id;
                    try {
                        id = Long.parseLong(parts[3]);
                    } catch (NumberFormatException e) {
                        sendResponse(exchange, 400, "Device id must be a number");
                        return;
                    }

                    var device = Database.getStorageDeviceById(id);
                    if (device == null) {
                        sendResponse(exchange, 404, "No device with id " + id);
                        return;
                    }
                    writeJson(exchange, device, gson);
                } else {
                    var devices = Database.getAllStorageDevices();
                    writeJson(exchange, devices, gson);
                }
            } catch (Exception e) {
                e.printStackTrace();
                sendResponse(exchange, 500, "Failed to fetch devices: " + e.getMessage());
            }
        }
    }

    static class TelemetryHandler implements HttpHandler {
        private static final Gson gson = new Gson();

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String method = exchange.getRequestMethod();

            if ("GET".equalsIgnoreCase(method)) {
                handleGet(exchange);
            } else if ("POST".equalsIgnoreCase(method)) {
                handlePost(exchange);
            } else {
                sendResponse(exchange, 405, "Only GET and POST are supported");
            }
        }

        private void handleGet(HttpExchange exchange) throws IOException {
            try {
                var rows = Database.getAllTelemetry();
                writeJson(exchange, rows, gson);
            } catch (Exception e) {
                e.printStackTrace();
                sendResponse(exchange, 500, "Failed to fetch telemetry: " + e.getMessage());
            }
        }

        private void handlePost(HttpExchange exchange) throws IOException {
            InputStream is = exchange.getRequestBody();
            String body = new String(is.readAllBytes(), StandardCharsets.UTF_8);

            try {
                TelemetryPayload payload = gson.fromJson(body, TelemetryPayload.class);
                Database.insertTelemetry(payload.deviceId, payload.capacityUtilization, payload.temperature);
                sendResponse(exchange, 201, "Telemetry recorded");
            } catch (Exception e) {
                e.printStackTrace();
                sendResponse(exchange, 500, "Failed to record telemetry: " + e.getMessage());
            }
        }
    }
}