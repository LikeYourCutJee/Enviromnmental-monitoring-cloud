package com.env.events.events_service.service.emqx.impl;

import com.env.events.events_service.config.properties.EmqxProperties;
import com.env.events.events_service.service.emqx.EmqxService;
import com.env.events.events_service.service.emqx.model.FunctionRequest;
import com.env.events.events_service.service.emqx.model.FunctionResponse;
import com.env.events.events_service.service.emqx.model.VarGetRequest;
import com.env.events.events_service.service.emqx.model.VarResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient;
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(
        value = "emqx.enable",
        havingValue = "true",
        matchIfMissing = false
)
public class EmqxServiceImpl implements EmqxService {

    private final EmqxProperties properties;
    private final ConcurrentHashMap<String, CompletableFuture<Object>> pending = new ConcurrentHashMap<>();
    private final ObjectMapper mapper = new ObjectMapper();

    private Mqtt5AsyncClient client;

    @PostConstruct
    public void init() {
        if (!properties.isEnable()) {
            LOG.warn("EMQX is disabled by configuration");
            return;
        }

        try {
            String uri = properties.getMqtt().getHost();
            String clientId = properties.getMqtt().getClient().getId();
            String stripped = uri.replace("tcp://", "");
            String[] parts = stripped.split(":");
            String host = parts[0];
            int port = parts.length > 1 ? Integer.parseInt(parts[1]) : 1883;

            client = Mqtt5Client.builder()
                    .identifier(clientId)
                    .serverHost(host)
                    .serverPort(port)
                    .buildAsync();

            String username = properties.getMqtt().getUsername();
            String password = properties.getMqtt().getPassword();

            var connect = client.connectWith()
                    .cleanStart(true);

            if (username != null && !username.isEmpty()) {
                connect.simpleAuth()
                        .username(username)
                        .password(password != null ? password.getBytes(StandardCharsets.UTF_8) : null)
                        .applySimpleAuth();
            }

            LOG.info("Connecting to EMQX MQTT host={} clientId={}",
                    properties.getMqtt().getHost(), clientId);

            connect.send().join();

            subscribe();
        } catch (Exception e) {
            LOG.error("Failed to initialize EMQX MQTT client", e);
        }
    }

    private void subscribe() {
        int qos = properties.getMqtt().getQos();
        MqttQos mqttQos = MqttQos.fromCode(qos);
        String replyPrefix = properties.getMqtt().getReply().getPrefix();

        client.subscribeWith()
                .topicFilter(replyPrefix + "/#")
                .qos(mqttQos)
                .callback(this::handleIncoming)
                .send();

        client.subscribeWith()
                .topicFilter("devices/+/result/+")
                .qos(mqttQos)
                .callback(this::handleIncoming)
                .send();

        client.subscribeWith()
                .topicFilter("devices/+/vars/response")
                .qos(mqttQos)
                .callback(this::handleIncoming)
                .send();

        LOG.info("Subscribed to MQTT topics: {}, {}, {}",
                replyPrefix + "/#", "devices/+/result/+", "devices/+/vars/response");
    }

    @PreDestroy
    public void shutdown() {
        try {
            if (client != null) {
                client.disconnect().join();
            }
        } catch (Exception e) {
            LOG.warn("Error shutting down MQTT client", e);
        }
    }

    private CompletableFuture<Object> publish(String topic, Object body, String requestId) {
        CompletableFuture<Object> future = new CompletableFuture<>();
        pending.put(requestId, future);

        try {
            byte[] payload = mapper.writeValueAsBytes(body);
            MqttQos mqttQos = MqttQos.fromCode(properties.getMqtt().getQos());

            Mqtt5Publish publish = Mqtt5Publish.builder()
                    .topic(topic)
                    .qos(mqttQos)
                    .payload(payload)
                    .build();

            client.publish(publish).whenComplete((ack, ex) -> {
                if (ex != null) {
                    pending.remove(requestId);
                    future.completeExceptionally(ex);
                    LOG.error("MQTT publish failed topic={} requestId={}", topic, requestId, ex);
                } else {
                    LOG.debug("MQTT publish succeeded topic={} requestId={}", topic, requestId);
                }
            });
        } catch (Exception e) {
            pending.remove(requestId);
            future.completeExceptionally(e);
        }

        return future;
    }

    private void handleIncoming(Mqtt5Publish publish) {
        try {
            String topic = publish.getTopic().toString();
            byte[] payload = publish.getPayloadAsBytes();
            if (payload == null || payload.length == 0) {
                return;
            }

            JsonNode node = mapper.readTree(payload);
            JsonNode idNode = node.get("requestId");
            if (idNode == null || idNode.isNull()) {
                LOG.debug("MQTT message without requestId, topic={}", topic);
                return;
            }

            String requestId = idNode.asText();
            CompletableFuture<Object> future = pending.remove(requestId);
            if (future != null) {
                future.complete(node);
                LOG.debug("Completed pending requestId={} from topic={}", requestId, topic);
            } else {
                LOG.debug("No pending future for requestId={}, topic={}", requestId, topic);
            }
        } catch (Exception e) {
            LOG.error("Failed to handle incoming MQTT message", e);
        }
    }

    @Override
    public FunctionResponse callDeviceFunction(String deviceId, String functionName, Map<String, Object> params) {
        String requestId = UUID.randomUUID().toString();
        String topic = String.format("devices/%s/cmd/%s", deviceId, functionName);

        FunctionRequest req = new FunctionRequest();
        req.requestId = requestId;
        req.params = params;

        CompletableFuture<Object> future = publish(topic, req, requestId);
        long timeoutSec = properties.getMqtt().getTimeout().getSeconds();

        try {
            Object result = future.get(timeoutSec, TimeUnit.SECONDS);
            JsonNode node = (JsonNode) result;
            return mapper.treeToValue(node, FunctionResponse.class);
        } catch (Exception e) {
            pending.remove(requestId);
            throw new RuntimeException(
                    "callDeviceFunction timeout/error: deviceId=" + deviceId + ", function=" + functionName, e);
        }
    }

    @Override
    public VarResponse getDeviceVariable(String deviceId, String variableName) {
        String requestId = UUID.randomUUID().toString();
        String topic = String.format("devices/%s/vars/get", deviceId);

        VarGetRequest req = new VarGetRequest();
        req.requestId = requestId;
        req.name = variableName;

        CompletableFuture<Object> future = publish(topic, req, requestId);
        long timeoutSec = properties.getMqtt().getTimeout().getSeconds();

        try {
            Object result = future.get(timeoutSec, TimeUnit.SECONDS);
            JsonNode node = (JsonNode) result;
            return mapper.treeToValue(node, VarResponse.class);
        } catch (Exception e) {
            pending.remove(requestId);
            throw new RuntimeException(
                    "getDeviceVariable timeout/error: deviceId=" + deviceId + ", var=" + variableName, e);
        }
    }
}