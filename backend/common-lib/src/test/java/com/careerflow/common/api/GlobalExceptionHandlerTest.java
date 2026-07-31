package com.careerflow.common.api;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
        request = new MockHttpServletRequest("GET", "/api/v1/test");
    }

    @Test
    void handleNotFoundReturns404Payload() {
        ApiErrorResponse response = handler.handleNotFound(
                new ResourceNotFoundException("Entity missing"),
                request
        );

        assertThat(response.status()).isEqualTo(HttpStatus.NOT_FOUND.value());
        assertThat(response.message()).isEqualTo("Entity missing");
        assertThat(response.path()).isEqualTo("/api/v1/test");
    }

    @Test
    void handleForbiddenReturns403Payload() {
        ApiErrorResponse response = handler.handleForbidden(
                new ForbiddenException("Access denied"),
                request
        );

        assertThat(response.status()).isEqualTo(HttpStatus.FORBIDDEN.value());
        assertThat(response.message()).isEqualTo("Access denied");
    }

    @Test
    void handleBadRequestReturns400Payload() {
        ApiErrorResponse response = handler.handleBadRequest(
                new IllegalArgumentException("Bad input"),
                request
        );

        assertThat(response.status()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response.message()).isEqualTo("Bad input");
    }

    @Test
    void handleValidationReturnsFieldErrors() {
        Object target = new Object();
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(target, "request");
        bindingResult.addError(new FieldError("request", "username", "must not be blank"));
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        when(exception.getBindingResult()).thenReturn(bindingResult);

        ApiErrorResponse response = handler.handleValidation(exception, request);

        assertThat(response.status()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response.message()).isEqualTo("Validation failed");
        assertThat(response.validationErrors()).containsEntry("username", "must not be blank");
    }

    @Test
    void handleGenericReturns500Payload() {
        ApiErrorResponse response = handler.handleGeneric(new RuntimeException("boom"), request);

        assertThat(response.status()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
        assertThat(response.message()).isEqualTo("Unexpected server error");
    }
}
