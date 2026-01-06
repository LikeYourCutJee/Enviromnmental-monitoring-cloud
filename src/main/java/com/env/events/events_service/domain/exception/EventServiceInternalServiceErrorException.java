package com.env.events.events_service.domain.exception;

public class EventServiceInternalServiceErrorException extends EventServiceGeneralException {

    public EventServiceInternalServiceErrorException(String message) {
        super(message);
    }

    public EventServiceInternalServiceErrorException(EventApiError apiError, String message) {
        super(apiError, message);
    }
}
