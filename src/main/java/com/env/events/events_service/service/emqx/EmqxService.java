package com.env.events.events_service.service.emqx;


import com.env.events.events_service.service.emqx.model.FunctionResponse;
import com.env.events.events_service.service.emqx.model.VarResponse;

import java.util.Map;

public interface EmqxService {
    FunctionResponse callDeviceFunction(String deviceId, String functionName, Map<String, Object> params);
    VarResponse getDeviceVariable(String deviceId, String variableName);
}
