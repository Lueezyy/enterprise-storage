package com.telemetry;

import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        System.out.println("Backend skeleton is alive.");
        Database.initSchema();

        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.setExecutor(null);
        server.start();
        System.out.println("Server listening on http://localhost:8080");
    }
}