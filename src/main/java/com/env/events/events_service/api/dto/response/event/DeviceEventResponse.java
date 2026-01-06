package com.env.events.events_service.api.dto.response.event;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DeviceEventResponse<T> {
    String topic;
    String iotId;

    T data;
}
