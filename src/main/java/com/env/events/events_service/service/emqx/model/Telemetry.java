package com.env.events.events_service.service.emqx.model;
import java.util.Map;

public class Telemetry {
    public long timestamp;
    public long seq;
    public Map<String, Object> data;
}

