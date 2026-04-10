# AGMS - Automated Greenhouse Management System (Microservices)

This repository contains a Spring Boot microservice system for greenhouse operations.

## Services

- **config-server** (port `8888`)
- **eureka-server** (port `8761`)
- **gateway** (port `8080`)
- **zone-service** (port `8081`)
- **sensor-service** (port `8082`)
- **automation-service** (port `8083`)
- **crop-service** (port `8084`)

---

## Prerequisites

- Java 17
- Maven 3.9+ (or Maven Wrapper `mvnw` if available)
- Postman

---

## Startup Order (Required)

> Start **infrastructure first**, then domain services.

1. Config Server
2. Eureka Server
3. API Gateway
4. Zone Service
5. Sensor Service
6. Automation Service
7. Crop Service

---

## Run Instructions (VS Code Terminal)

Open separate terminals and run each service:

### 1) Config Server
```bash
cd config-server
mvn spring-boot:run
```

### 2) Eureka Server
```bash
cd eureka-server
mvn spring-boot:run
```

### 3) Gateway
```bash
cd gateway
mvn spring-boot:run
```

### 4) Zone Service
```bash
cd zone-service
mvn spring-boot:run
```

### 5) Sensor Service
```bash
cd sensor-service
mvn spring-boot:run
```

### 6) Automation Service
```bash
cd automation-service
mvn spring-boot:run
```

### 7) Crop Service
```bash
cd crop-service
mvn spring-boot:run
```

---

## Build All Modules

From the repository root:

```bash
mvn clean install -DskipTests
```

If Maven is not installed globally and wrapper exists:

```bash
./mvnw clean install -DskipTests
```

---

## Eureka Dashboard

Open:

- `http://localhost:8761`

Take a screenshot when all services are **UP** and save it as:

- `docs/eureka-dashboard-up.png`

---

## API Testing

Use the provided Postman collection in the repository root:

- `AGMS_Postman_Collection.json`

Recommended Postman execution order:

1. Get JWT Token
2. Create Zone
3. Get Zone By ID
4. Trigger Automation
5. Register Crop Batch
6. Update Crop Status
7. Security checks

---

## Notes for Submission Validation

- Keep a clean service-based folder structure.
- Use meaningful commits (example: `Initial Setup`, `Zone Service Impl`, `Gateway JWT Security`).
- Avoid bulk upload commits.
- Keep this README and Postman collection updated with implemented endpoints only.
