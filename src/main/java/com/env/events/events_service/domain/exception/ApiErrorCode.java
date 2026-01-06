package com.env.events.events_service.domain.exception;

import lombok.Getter;

import java.util.Objects;

@Getter
public enum ApiErrorCode
{
    E500000(500000, "Internal Server Error."),
    E401001(401001, "Unauthorized."),
    E403001(403001, "Forbidden."),
    E400101(400101, "Wrong input data."),
    E400102(400102, "Input data limitation.");

    private final int value;
    private final String reason;

    private ApiErrorCode(int value, String reason) {
        this.value = value;
        this.reason = reason;
    }

    public int getValue() {
        return this.value;
    }

    public String stringValue() {
        return String.valueOf(this.value);
    }

    public String getReason() {
        return this.reason;
    }

    public static ApiErrorCode valueOf(int value) {
        ApiErrorCode clientErrorCode = resolve(value);
        if (Objects.isNull(clientErrorCode)) {
            throw new IllegalArgumentException("No matching constant for [" + value + "]");
        } else {
            return clientErrorCode;
        }
    }

    private static ApiErrorCode resolve(int value) {
        ApiErrorCode[] clientErrorCodes = values();

        for(ApiErrorCode errorCode : clientErrorCodes) {
            if (errorCode.value == value) {
                return errorCode;
            }
        }

        return null;
    }
}
