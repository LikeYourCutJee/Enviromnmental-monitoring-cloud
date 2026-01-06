package com.env.events.events_service.repository;

import com.env.events.events_service.domain.entity.SensorData;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface SensorDataRepository
        extends ReactiveCrudRepository<SensorData, Long> {

    Flux<SensorData> findAllByDeviceId(String deviceId);

    Flux<SensorData> findTop10ByDeviceIdOrderByCreatedAtDesc(String deviceId);

    Mono<SensorData> findFirstByDeviceIdOrderByCreatedAtDesc(String deviceId);
}
