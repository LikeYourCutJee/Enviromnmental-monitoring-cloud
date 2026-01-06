package com.env.events.events_service.api;

import com.env.events.events_service.api.dto.request.event.MqttDeviceEventRequest;
import com.env.events.events_service.api.dto.response.event.DeviceEventResponse;
import com.env.events.events_service.service.emqx.EmqxService;
import com.env.events.events_service.service.emqx.model.FunctionResponse;
import com.env.events.events_service.service.emqx.model.VarResponse;
import com.env.events.events_service.service.sensor.SensorDataService;
import com.env.events.events_service.service.sse.SseService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.Map;

@Tag(name = "Public Particle API", description = "Particle API")
@RestController
@RequestMapping("/v1/")
@Slf4j
@RequiredArgsConstructor
public class MqttController {

    private final ObjectMapper objectMapper;
    private final SseService sse;
    private final EmqxService emqxService;
    private final SensorDataService sensorDataService;

    @GetMapping(path = "/events/stream/{iotId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<Object>> getEventStream(
            @PathVariable String iotId) {


        Flux<ServerSentEvent<Object>> keepAlive =
                Flux.interval(Duration.ofSeconds(15))
                        .map(t -> ServerSentEvent.<Object>builder().comment("keep-alive").build());


        Flux<ServerSentEvent<Object>> bus = Flux.defer(() ->
                Mono.fromRunnable(() -> sse.onHttpClientConnected(iotId))
                        .thenMany(sse.subscribe(iotId))
                        .doFinally(sig -> sse.onHttpClientDisconnected(iotId))
        );

        return Flux.merge(bus, keepAlive)
                .take(Duration.ofMinutes(30))
                .onErrorResume(ex ->
                        Flux.just(ServerSentEvent.<Object>builder()
                                .event("error")
                                .data(ex.getMessage())
                                .build()));
    }

    @PostMapping(value = "/mqtt", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<JsonNode> postEvent(@RequestBody MqttDeviceEventRequest event) {

        LOG.info("Received an event {}", event);
        
        Mono<JsonNode> responseMono = Mono.defer(() -> {
            if (event.getData() != null && !event.getData().isNull()) {
                return Mono.just(event.getData());
            }
            if (event.getRawData() != null) {
                return Mono.fromCallable(() -> objectMapper.readTree(event.getRawData()));
            }
            return Mono.empty();
        });

        return responseMono.flatMap(response ->
                emitJson(event.getIotId(), event.getTopic(), event.getEventName(), response)
                        .then(sensorDataService.saveEvent(event.getIotId(), response))
                        .thenReturn(response)
        );
    }

    @PostMapping(value = "/devices/{deviceId}/functions/{functionName}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<FunctionResponse> callDeviceFunction(
            @PathVariable String deviceId,
            @PathVariable String functionName,
            @RequestBody(required = false) Map<String, Object> params
    ) {
        return Mono.fromCallable(() -> {
            LOG.info("Calling device function: deviceId={}, functionName={}, params={}",
                    deviceId, functionName, params);
            return emqxService.callDeviceFunction(deviceId, functionName,
                    params != null ? params : Map.of());
        }).subscribeOn(Schedulers.boundedElastic());
    }

    @GetMapping(value = "/devices/{deviceId}/vars/{variableName}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<VarResponse> getDeviceVariable(
            @PathVariable String deviceId,
            @PathVariable String variableName
    ) {
        return Mono.fromCallable(() -> {
            LOG.info("Getting device variable: deviceId={}, variableName={}", deviceId, variableName);
            return emqxService.getDeviceVariable(deviceId, variableName);
        }).subscribeOn(Schedulers.boundedElastic());
    }

    private Mono<Void> emitJson(String iotId, String topic, String eventName, Object payload) {
        try {
            String json = objectMapper.writeValueAsString(
                    DeviceEventResponse.<Object>builder()
                            .data(payload)
                            .iotId(iotId)
                            .topic(topic)
                            .build()
            );

            ServerSentEvent<Object> evt = ServerSentEvent.<Object>builder()
                    .event(eventName)
                    .data(json)
                    .build();
            return sse.emit(iotId, evt);
        } catch (Exception e) {
            LOG.error("SSE JSON serialization failed for {}: {}", topic, e.getMessage(), e);
            return Mono.empty();
        }
    }
}
