package com.env.events.events_service.domain.exception;

public class EventServiceForbiddenException extends EventServiceGeneralException {

    public EventServiceForbiddenException(String message) {
        super(message);
    }

    public EventServiceForbiddenException(EventApiError apiError, String message) {
        super(apiError, message);
    }
}
