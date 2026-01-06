package com.env.events.events_service.service.sse.impl;

import com.env.events.events_service.service.sse.SseService;
import com.env.events.events_service.service.sse.model.ChanelStatisticEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Slf4j
public class SseServiceImpl implements SseService {

    private final ConcurrentHashMap<String, Channel> channels = new ConcurrentHashMap<>();
    private static final Scheduler OFFLOAD = Schedulers.boundedElastic();

    private static final class Channel {
        // Храним последний ивент — новый подписчик гарантированно его увидит
        final Sinks.Many<ServerSentEvent<Object>> sink = Sinks.many().replay().latest();
        final AtomicInteger httpClients = new AtomicInteger(0);
    }

    @Override
    public Flux<ServerSentEvent<Object>> subscribe(String topic) {
        Channel ch = channels.computeIfAbsent(topic, t -> new Channel());
        return ch.sink.asFlux();
    }

    @Override
    public Flux<ServerSentEvent<Object>> subscribe(Collection<String> topics) {
        if (topics == null || topics.isEmpty()) return Flux.empty();
        return Flux.merge(topics.stream().distinct().map(this::subscribe).toList());
    }

    @Override
    public Mono<Void> emit(String topic, ServerSentEvent<Object> event) {
        Channel ch = channels.get(topic);
        if (ch == null) return Mono.empty();
        Sinks.EmitResult res = ch.sink.tryEmitNext(event);
        if (!res.isSuccess() && res == Sinks.EmitResult.FAIL_TERMINATED) {
            channels.remove(topic);
        }
        return Mono.empty();
    }

    @Override
    public void closeTopic(String topic) {
        Channel ch = channels.remove(topic);
        if (ch != null) ch.sink.tryEmitComplete();
    }

    @Override public boolean hasTopic(String topic) { return channels.containsKey(topic); }
    @Override public int getSubscriberCount(String topic) {
        Channel ch = channels.get(topic);
        return ch == null ? 0 : ch.httpClients.get();
    }

    @Override
    public int onHttpClientConnected(String topic) {
        Channel ch = channels.computeIfAbsent(topic, t -> new Channel());
        int now = ch.httpClients.incrementAndGet();
        emitCountAsync(topic, now);
        return now;
    }

    @Override
    public int onHttpClientDisconnected(String topic) {
        Channel ch = channels.get(topic);
        if (ch == null) return 0;
        int left = ch.httpClients.decrementAndGet();
        if (left > 0) {
            emitCountAsync(topic, left);
        } else {
            channels.remove(topic);
            ch.sink.tryEmitComplete();
        }
        return Math.max(left, 0);
    }

    private void emitCountAsync(String topic, int count) {
        Channel ch = channels.get(topic);
        if (ch == null) return;

        // сериализации JSON нет — отдаём объект; Spring сам сериализует
        ServerSentEvent<Object> evt = ServerSentEvent.builder()
                .event("channelStats")
                .data(ChanelStatisticEvent.builder().topic(topic).subscribers(count).build())
                .build();

        // можно эмитить сразу; replay().latest() гарантирует доставку новому подписчику
        ch.sink.tryEmitNext(evt);
        // если хотите «увести» с event-loop — оберните одной строкой:
        // OFFLOAD.schedule(() -> ch.sink.tryEmitNext(evt));
    }
}

