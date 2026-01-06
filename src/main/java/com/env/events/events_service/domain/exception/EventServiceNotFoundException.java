package com.env.events.events_service.domain.exception;

public class EventServiceNotFoundException extends EventServiceGeneralException {
    public EventServiceNotFoundException(String message) {
        super(message);
    }

    public EventServiceNotFoundException(EventApiError apiError, String message) {
        super(apiError, message);
    }

}
