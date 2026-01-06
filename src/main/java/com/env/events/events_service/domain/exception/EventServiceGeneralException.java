package com.env.events.events_service.domain.exception;

public class EventServiceGeneralException extends RuntimeException {

    private EventApiError apiError;

    public EventServiceGeneralException(String message) {
        super(message);
    }

    public EventServiceGeneralException(EventApiError apiError, String message) {
        this(message);
        this.apiError = apiError;
    }

    public EventApiError getError() {
        return apiError;
    }
}

