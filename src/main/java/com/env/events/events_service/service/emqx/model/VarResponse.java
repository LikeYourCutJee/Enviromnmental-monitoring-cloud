package com.env.events.events_service.service.emqx.model;

public class VarResponse {
    public String requestId;
    public String name;
    public Object value;
    public String status; // ok / updated / error
    public long timestamp;
    public String error;
}
