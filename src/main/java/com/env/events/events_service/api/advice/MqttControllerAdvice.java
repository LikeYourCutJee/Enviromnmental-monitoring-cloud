package com.env.events.events_service.api.advice;

import com.doora.common.api.dto.DooraApiErrorCode;
import com.doora.common.api.exception.ForbiddenException;
import com.doora.common.api.exception.UnauthorizedException;
import com.env.events.events_service.api.MqttController;
import com.env.events.events_service.domain.exception.*;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.io.IOException;
import java.util.Objects;

@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
@ControllerAdvice(assignableTypes = {MqttController.class})
public class MqttControllerAdvice {

    @ExceptionHandler(IOException.class)
    public ResponseEntity<Void> handleIOException(IOException ex) {
        LOG.warn("Client disconnected, suppressing IOException: {}", ex.getMessage());
        return ResponseEntity.noContent().build(); // Возвращаем 204 No Content
    }


    @ExceptionHandler
    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @Hidden
    EventApiError badRequest(EventServiceBadRequestException exception) {
        return processException(exception);
    }

    @ExceptionHandler
    @ResponseBody
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @Hidden
    EventApiError generalException(Exception exception) {
        return processGeneralException(exception);
    }

    @ExceptionHandler
    @ResponseBody
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @Hidden
    EventApiError generalException(UnauthorizedException exception) {
        return processGeneralException(exception, ApiErrorCode.E401001);
    }

    @ExceptionHandler
    @ResponseBody
    @ResponseStatus(HttpStatus.FORBIDDEN)
    @Hidden
    EventApiError generalException(ForbiddenException exception) {
        return processGeneralException(exception, ApiErrorCode.E403001);
    }

    @ExceptionHandler
    @ResponseBody
    @ResponseStatus(HttpStatus.REQUEST_TIMEOUT)
    @Hidden
    EventApiError generalException(EventServiceTimeOutException exception) {
        return processGeneralException(exception, ApiErrorCode.E403001); //todo Change error code to request timeout when added
    }

    @ExceptionHandler
    @ResponseBody
    @ResponseStatus(HttpStatus.FORBIDDEN)
    @Hidden
    EventApiError generalException(EventServiceForbiddenException exception) {
        return processGeneralException(exception, ApiErrorCode.E403001);
    }

    @ExceptionHandler
    @ResponseBody
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @Hidden
    EventApiError generalException(EventServiceNotFoundException exception) {
        return processGeneralException(exception, ApiErrorCode.E403001); //todo Change error code to not found when added
    }

    private EventApiError processException(EventServiceGeneralException exception) {
        EventApiError apiError = exception.getError();
        if (Objects.nonNull(apiError)) {
            LOG.error(apiError.getErrorMessage());
            return apiError;
        } else {
            LOG.error(exception.getMessage());
            return EventApiError.builder().errorMessage(exception.getMessage()).build();
        }
    }

    private EventApiError processGeneralException(Exception exception) {
        LOG.error(exception.getMessage());
        return EventApiError.builder().errorCode(DooraApiErrorCode.E500000.stringValue()).errorMessage(exception.getMessage()).build();
    }

    private EventApiError processGeneralException(Exception exception, ApiErrorCode apiErrorCode) {
        LOG.error(exception.getMessage());
        return EventApiError.builder().errorCode(apiErrorCode.stringValue()).errorMessage(exception.getMessage()).build();
    }
}
