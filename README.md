# Storage Infrastructure Monitoring System

## Overview

This in-progress project is a small enterprise-style storage monitoring system. A Python agent simulates storage
devices and sends telemetry over HTTP to a Java backend, which validates it, stores it
in a relational SQLite database, and exposes it through a REST API.

## Architecture
- **Python agent** (`agent/`) — simulates storage devices, generates fake telemetry
  readings on a loop, and POSTs them to the Java backend.
- **Java backend** (`backend/`) — a plain Java app using
  `com.sun.net.httpserver.HttpServer` for REST endpoints and JDBC (`sqlite-jdbc`) for
  persistence. JSON parsing via Gson.
- **Database** — SQLite, stored as a single file (`backend/telemetry.db`, gitignored).