package com.env.events.events_service.domain.exception;

public class EventServiceBadRequestException extends EventServiceGeneralException {

    public EventServiceBadRequestException(String message) {
        super(message);
    }

    public EventServiceBadRequestException(EventApiError apiError, String message) {
        super(apiError, message);
    }
}
