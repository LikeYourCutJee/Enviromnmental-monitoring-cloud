package com.env.events.events_service.service.emqx.model;
import java.util.Map;

public class FunctionResponse {
    public String requestId;
    public boolean success;
    public long timestamp;
    public Map<String, Object> result;
    public String error;
}

