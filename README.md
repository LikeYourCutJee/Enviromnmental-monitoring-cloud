# Environmental Monitoring Cloud - Events Service

A Spring Boot-based microservice for handling environmental monitoring IoT device events using MQTT protocol and real-time data streaming.

## 📋 Overview

This service is part of an environmental monitoring system that collects, processes, and streams sensor data from IoT devices. It provides real-time event streaming capabilities using Server-Sent Events (SSE) and integrates with EMQX MQTT broker for IoT device communication.

## ✨ Features

- **Real-time Event Streaming**: Server-Sent Events (SSE) support for live sensor data updates
- **MQTT Integration**: Seamless communication with IoT devices via EMQX broker
- **Environmental Sensor Data**: Collects temperature, humidity, pressure, air quality (AQI), TVOC, and CO2 data
- **Reactive Architecture**: Built with Spring WebFlux for non-blocking, reactive operations
- **Device Remote Control**: Call device functions and retrieve variables remotely
- **Persistent Storage**: Stores sensor data in PostgreSQL using R2DBC for reactive database access
- **API Documentation**: Integrated OpenAPI/Swagger documentation

## 🛠️ Technology Stack

- **Java 17**: Modern Java with latest features
- **Spring Boot 3.4.2**: Latest Spring Boot framework
- **Spring WebFlux**: Reactive web framework
- **Spring Data R2DBC**: Reactive database connectivity
- **PostgreSQL**: Persistent data storage
- **EMQX/HiveMQ**: MQTT broker and client
- **Lombok**: Reduces boilerplate code
- **MapStruct**: Object mapping framework
- **Jackson**: JSON processing
- **Maven**: Build and dependency management
- **JaCoCo**: Code coverage analysis

## 🏗️ Architecture

```
┌─────────────────┐
│   IoT Devices   │
└────────┬────────┘
         │ MQTT
         ▼
┌─────────────────┐
│   EMQX Broker   │
└────────┬────────┘
         │
         ▼
┌─────────────────────────────────────────┐
│         Events Service                   │
│  ┌───────────────────────────────────┐  │
│  │    MqttController (REST API)      │  │
│  └───────────┬───────────────────────┘  │
│              │                           │
│  ┌───────────▼─────┐  ┌──────────────┐  │
│  │   SSE Service   │  │ EMQX Service │  │
│  └────────┬────────┘  └──────┬───────┘  │
│           │                  │           │
│  ┌────────▼──────────────────▼────────┐ │
│  │      SensorDataService            │  │
│  └───────────────┬───────────────────┘  │
│                  │                       │
│  ┌───────────────▼───────────────────┐  │
│  │   SensorDataRepository (R2DBC)   │  │
│  └──────────────┬────────────────────┘  │
└─────────────────┼──────────────────────┘
                  │
         ┌────────▼────────┐
         │   PostgreSQL    │
         └─────────────────┘
```

## 📦 Prerequisites

- Java 17 or higher
- Maven 3.6+
- PostgreSQL database
- EMQX MQTT broker (optional, can be disabled)
- Docker (optional, for containerized deployment)

## 🚀 Installation

### 1. Clone the Repository

```bash
git clone https://github.com/LikeYourCutJee/Enviromnmental-monitoring-cloud.git
cd Enviromnmental-monitoring-cloud
```

### 2. Database Setup

Create a PostgreSQL database and execute the following schema:

```sql
CREATE TABLE sensor_data (
    id BIGSERIAL PRIMARY KEY,
    device_id VARCHAR(255) NOT NULL,
    temperature DECIMAL(5,2),
    humidity DECIMAL(5,2),
    pressure DECIMAL(7,2),
    aqi SMALLINT,
    tvoc_ppb INTEGER,
    eco2_ppm INTEGER,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE INDEX idx_sensor_data_device_id ON sensor_data(device_id);
CREATE INDEX idx_sensor_data_created_at ON sensor_data(created_at);
```

### 3. Configuration

Edit `src/main/resources/application.yml`:

```yaml
spring:
  application:
    name: event-service
  r2dbc:
    url: r2dbc:pool:postgresql://localhost:5432/your_database
    username: your_username
    password: your_password

server:
  port: 8085

emqx:
  enable: true  # Set to false to disable MQTT integration
  host: https://localhost:18084
  api:
    key: your_api_key
    secret: your_api_secret
  mqtt:
    host: tcp://localhost:1883
    username: ""
    password: ""
```

### 4. Build the Project

```bash
./mvnw clean install
```

### 5. Run the Application

```bash
./mvnw spring-boot:run
```

The service will start on port 8085 (or the port specified in your configuration).

## 📚 API Documentation

Once the application is running, access the Swagger UI documentation at:

```
http://localhost:8085/swagger-ui.html
```

### Key Endpoints

#### 1. Stream Device Events (SSE)

```
GET /v1/events/stream/{iotId}
```

Real-time streaming of device events using Server-Sent Events.

**Example:**
```bash
curl -N http://localhost:8085/v1/events/stream/device-123
```

#### 2. Receive MQTT Events

```
POST /v1/mqtt
Content-Type: application/json
```

Endpoint for receiving events from MQTT devices.

**Request Body:**
```json
{
  "iotId": "device-123",
  "topic": "devices/device-123/telemetry",
  "eventName": "telemetry",
  "data": {
    "temperature": 22.5,
    "humidity": 45.3,
    "pressure": 1013.25,
    "aqi": 35,
    "tvoc_ppb": 120,
    "eco2_ppm": 450
  }
}
```

#### 3. Call Device Function

```
POST /v1/devices/{deviceId}/functions/{functionName}
Content-Type: application/json
```

Remotely invoke a function on a device.

**Example:**
```bash
curl -X POST http://localhost:8085/v1/devices/device-123/functions/reset \
  -H "Content-Type: application/json" \
  -d '{"mode": "soft"}'
```

#### 4. Get Device Variable

```
GET /v1/devices/{deviceId}/vars/{variableName}
```

Retrieve a variable value from a device.

**Example:**
```bash
curl http://localhost:8085/v1/devices/device-123/vars/status
```

## 🔧 Configuration Options

### Database Configuration

- `spring.r2dbc.url`: R2DBC connection URL for PostgreSQL
- `spring.r2dbc.username`: Database username
- `spring.r2dbc.password`: Database password

### EMQX/MQTT Configuration

- `emqx.enable`: Enable/disable MQTT integration (true/false)
- `emqx.host`: EMQX REST API endpoint
- `emqx.api.key`: API key for EMQX authentication
- `emqx.api.secret`: API secret for EMQX authentication
- `emqx.mqtt.host`: MQTT broker address
- `emqx.mqtt.qos`: Quality of Service level (0, 1, or 2)
- `emqx.mqtt.timeout.seconds`: Request timeout in seconds

## 💻 Development

### Running Tests

```bash
./mvnw test
```

### Code Coverage

```bash
./mvnw jacoco:report
```

Coverage reports will be available in `target/site/jacoco/index.html`.

### Building for Production

```bash
./mvnw clean package -DskipTests
```

The executable JAR will be created in the `target/` directory.

## 📊 Sensor Data Model

The service supports the following environmental sensor measurements:

| Field | Type | Description |
|-------|------|-------------|
| `temperature` | DECIMAL | Temperature in Celsius |
| `humidity` | DECIMAL | Relative humidity percentage |
| `pressure` | DECIMAL | Atmospheric pressure in hPa |
| `aqi` | SMALLINT | Air Quality Index (0-500) |
| `tvoc_ppb` | INTEGER | Total Volatile Organic Compounds in parts per billion |
| `eco2_ppm` | INTEGER | Equivalent CO2 concentration in parts per million |

## 🔐 Security Considerations

- **Authentication**: Consider implementing JWT-based authentication for production use
- **MQTT Security**: Use TLS/SSL for MQTT connections in production
- **Database Security**: Use strong passwords and restrict database access
- **API Gateway**: Deploy behind an API gateway for additional security layers
- **Environment Variables**: Store sensitive configuration in environment variables, not in application.yml

## 🐛 Troubleshooting

### EMQX Connection Issues

If you're not using EMQX, set `emqx.enable=false` in your configuration.

### Database Connection Issues

Ensure PostgreSQL is running and the R2DBC URL format is correct:
```
r2dbc:pool:postgresql://host:port/database
```

### Port Already in Use

Change the server port in `application.yml`:
```yaml
server:
  port: 8086
```

## 📄 License

This project is provided as-is for educational and development purposes.

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## 📧 Contact

For questions and support, please open an issue in the GitHub repository.

---

**Built with ❤️ for IoT Environmental Monitoring**
