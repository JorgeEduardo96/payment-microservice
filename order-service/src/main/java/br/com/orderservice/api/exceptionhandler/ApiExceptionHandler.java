package br.com.orderservice.api.exceptionhandler;

import br.com.orderservice.domain.exception.EntityNotFoundException;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@RequiredArgsConstructor
public class ApiExceptionHandler {

    @ExceptionHandler(EntityNotFoundException.class)
    public ApiErrorResponse handleClientNotFoundException(EntityNotFoundException ex) {
        return ApiErrorResponse.builder()
                .status(404)
                .message(ex.getMessage())
                .build();
    }

    @Builder
    public static class ApiErrorResponse {
        private int status;
        private String message;

        @Override
        public String toString() {
            return "ApiErrorResponse{" +
                    "status=" + status +
                    ", message='" + message + '\'' +
                    '}';
        }
    }
}
