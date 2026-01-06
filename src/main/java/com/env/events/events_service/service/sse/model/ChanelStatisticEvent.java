package com.env.events.events_service.service.sse.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ChanelStatisticEvent {
    String topic;
    int subscribers;
}
