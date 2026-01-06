package com.env.events.events_service.service.emqx.impl;

import com.env.events.events_service.service.emqx.EmqxService;
import com.env.events.events_service.service.emqx.model.FunctionResponse;
import com.env.events.events_service.service.emqx.model.VarResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@ConditionalOnProperty(value = "emqx.enable", havingValue = "false", matchIfMissing = true  )
public class EmqxServiceStub implements EmqxService {

    @Override
    public FunctionResponse callDeviceFunction(String deviceId, String functionName, Map<String, Object> params) {
        LOG.warn(
                "[EMQX STUB] callDeviceFunction: EMQX disabled in configuration. " +
                        "deviceId={}, functionName={}, params={}",
                deviceId, functionName, params
        );


        FunctionResponse resp = new FunctionResponse();
        resp.requestId = UUID.randomUUID().toString();
        resp.success = false;
        resp.error = "EMQX is disabled (stub)";
        return resp;
    }

    @Override
    public VarResponse getDeviceVariable(String deviceId, String variableName) {
        LOG.warn(
                "[EMQX STUB] getDeviceVariable: EMQX disabled in configuration. " +
                        "deviceId={}, variableName={}",
                deviceId, variableName
        );

        VarResponse resp = new VarResponse();
        resp.requestId = UUID.randomUUID().toString();
        resp.name = variableName;
        resp.value = null;
        resp.status = "error";
        resp.error = "EMQX is disabled (stub)";
        return resp;
    }
}
