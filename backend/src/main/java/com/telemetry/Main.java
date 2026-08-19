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

public class Main {
    static class TelemetryPayload {
        String deviceId;
        double capacityUtilization;
        double temperature;
    }

    public static void main(String[] args) throws IOException {
        System.out.println("Backend skeleton is alive.");
        Database.initSchema();

        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.setExecutor(null);

        server.createContext("/api/telemetry", new TelemetryHandler());

        server.start();
        System.out.println("Server listening on http://localhost:8080");
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
                String json = gson.toJson(rows);
                byte[] responseBytes = json.getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, responseBytes.length);
                OutputStream os = exchange.getResponseBody();
                os.write(responseBytes);
                os.close();
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

        private void sendResponse(HttpExchange exchange, int statusCode, String message) throws IOException {
            byte[] responseBytes = message.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(statusCode, responseBytes.length);
            OutputStream os = exchange.getResponseBody();
            os.write(responseBytes);
            os.close();
        }
    }
}