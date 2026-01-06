package com.env.events.events_service.api.dto.request.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MqttDeviceEventRequest {

    @JsonProperty("iotId")
    String iotId;

    @JsonProperty("topic")
    String topic;

    @JsonProperty("eventName")
    String eventName;

    @JsonProperty("data")
    JsonNode data;

    @JsonProperty("rawData")
    String rawData;

    @JsonProperty("rawDataType")
    String rawDataType;

}
