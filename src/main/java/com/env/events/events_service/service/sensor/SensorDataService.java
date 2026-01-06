package com.env.events.events_service.service.sensor;

import com.env.events.events_service.domain.entity.SensorData;
import com.env.events.events_service.repository.SensorDataRepository;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class SensorDataService {

    private final SensorDataRepository repo;

    public Mono<SensorData> saveEvent(String deviceId, JsonNode data) {
        if (data == null || data.isNull()) {
            return Mono.empty();
        }

        SensorData entity = SensorData.builder()
                .deviceId(deviceId)
                .temperature(decimalOrNull(data.get("temperature")))
                .humidity(decimalOrNull(data.get("humidity")))
                .pressure(decimalOrNull(data.get("pressure")))
                .aqi(shortOrNull(data.get("aqi")))
                .tvocPpb(intOrNull(data.get("tvoc_ppb")))
                .eco2Ppm(intOrNull(data.get("eco2_ppm")))
                // createdAt: если хочешь, чтобы выставляло приложение:
                .createdAt(OffsetDateTime.now())
                // если хочешь, чтобы выставляла БД DEFAULT now(), то ставь createdAt(null)
                .build();

        return repo.save(entity);
    }

    private static BigDecimal decimalOrNull(JsonNode n) {
        if (n == null || n.isNull()) return null;
        // numeric values in JSON -> BigDecimal safely
        return n.decimalValue();
    }

    private static Integer intOrNull(JsonNode n) {
        if (n == null || n.isNull()) return null;
        return n.asInt();
    }

    private static Short shortOrNull(JsonNode n) {
        if (n == null || n.isNull()) return null;
        return (short) n.asInt();
    }
}
