package com.env.events.events_service.service.sse;

import org.springframework.http.codec.ServerSentEvent;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;

public interface SseService {


    Flux<ServerSentEvent<Object>> subscribe(String topic);
    Flux<ServerSentEvent<Object>> subscribe(Collection<String> topics);

    Mono<Void> emit(String topic, ServerSentEvent<Object> event);

    void closeTopic(String topic);
    boolean hasTopic(String topic);

    int getSubscriberCount(String topic);

    int onHttpClientDisconnected(String topic);
    int onHttpClientConnected(String topic);
}
