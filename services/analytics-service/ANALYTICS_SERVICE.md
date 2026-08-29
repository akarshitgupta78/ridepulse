# RidePulse: Intelligent Distributed Ride-Booking Platform

## 1. Overview
RidePulse is a high-concurrency, fault-tolerant distributed ride-hailing backend platform. It features real-time geospatial driver indexing, heuristic-based dispatch matching, event-driven state machines, optimistic locking, idempotent payment settlements, and ML-powered dynamic surge pricing.

---

## 2. Platform Architecture

### Microservices Ecosystem
* **API Gateway (`:8088`)**: Single entry point built with Spring Cloud Gateway; handles reverse proxying, global CORS configuration, and unified routing for HTTP and WebSockets.
* **User Service (`:8081`)**: Manages rider profile lifecycle, registrations, contact information, and rider ratings in PostgreSQL.
* **Booking Service (`:8082`)**: Core transactional ride state machine (`REQUESTED`, `MATCHED`, `STARTED`, `COMPLETED`, `CANCELLED`). Uses optimistic locking (`@Version`) to eliminate race conditions and publishes lifecycle events to Apache Kafka.
* **Matching Engine (`:8083`)**: Event-driven dispatch service consuming `RIDE_REQUESTED` events from Kafka. Uses Redis Geospatial queries, a multi-factor candidate scoring heuristic, and Redisson distributed locks to prevent double driver dispatch.
* **Location Service (`:8084`)**: High-throughput driver telemetry service using Redis Geospatial commands (`GEOADD`, `GEORADIUS`) and Spring WebSocket/STOMP message brokers for live tracking.
* **Kafka UI (`:8085`)**: Web UI dashboard for real-time Kafka cluster, topic, and message inspection.
* **Notification Service (`:8086`)**: Real-time push notification dispatcher via WebSockets (`/ws-notifications`) for match confirmations and ride status changes.
* **Driver Service (`:8087`)**: Manages driver profiles, vehicle details, availability toggles (online/offline), and acceptance performance.
* **Payment Service (`:8089`)**: Fault-tolerant payment settlement engine consuming Kafka events. Enforces strict idempotency via atomic Redis `SETNX` locks and unique transaction references to prevent duplicate billing.
* **ML Demand & Dynamic Pricing Engine (`:8000`)**: Python/FastAPI microservice utilizing Uber H3 hexagonal spatial indexing (Resolution 7) and dynamic demand-to-supply ratio calculations for surge pricing.
* **Analytics Service**: Asynchronous stream aggregator consuming platform events for real-time reporting, system KPIs, and ML feature preparation.

---

## 3. Analytics Service (`services/analytics-service`)

### Role & Purpose
The Analytics Service operates as an asynchronous, non-blocking telemetry engine. It monitors platform health, ride volume metrics, driver utilization rates, and spatial supply-demand imbalances without adding load to transactional write paths.

### Directory Structure
```text
services/analytics-service/
├── pom.xml
└── src/
    └── main/
        ├── java/com/ridepulse/analytics/
        │   ├── config/          # Kafka listener & database persistence configs
        │   ├── consumer/        # Topic listeners consuming 'ride.events'
        │   ├── dto/             # Outgoing telemetry & dashboard payloads
        │   ├── entity/          # Time-series analytics & metrics entities
        │   ├── repository/      # Analytical query repositories (OLAP/read-heavy)
        │   ├── service/         # KPI rollups, aggregation, & feature pipelines
        │   ├── controller/      # Admin endpoints for health & metrics queries
        │   └── AnalyticsServiceApplication.java
        └── resources/
            └── application.yml
```
