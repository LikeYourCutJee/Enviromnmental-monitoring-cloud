package com.env.events.events_service.domain.exception;

public class EventServiceTimeOutException extends EventServiceGeneralException {
    public EventServiceTimeOutException(String message) {
        super(message);
    }

    public EventServiceTimeOutException(EventApiError apiError, String message) {
        super(apiError, message);
    }
}
