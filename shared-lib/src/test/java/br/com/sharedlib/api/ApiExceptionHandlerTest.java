package br.com.sharedlib.api;

import br.com.sharedlib.model.EntityNotFoundException;
import br.com.sharedlib.model.Problem;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.WebRequest;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApiExceptionHandlerTest {

    @Mock
    private MessageSource messageSource;

    @Mock
    private BindingResult bindingResult;

    @Mock
    private FieldError fieldError;

    @Mock
    private WebRequest request;

    @InjectMocks
    private ApiExceptionHandler underTest;

    @Test
    void handleMethodArgumentNotValid() {
        when(fieldError.getField()).thenReturn("email");

        List<FieldError> fieldErrors = List.of(fieldError);

        when(bindingResult.getAllErrors()).thenReturn(new ArrayList<>(fieldErrors));
        when(messageSource.getMessage(any(), any())).thenReturn("Required Field");

        MethodArgumentNotValidException exception =
                new MethodArgumentNotValidException(mock(MethodParameter.class), bindingResult);

        ResponseEntity<Object> response = underTest.handleMethodArgumentNotValid(
                exception, new HttpHeaders(), HttpStatus.BAD_REQUEST, request);

        assertThat(HttpStatus.BAD_REQUEST).isEqualTo(response.getStatusCode());

        Problem problem = (Problem) response.getBody();
        assertThat(problem).isNotNull();
        assertThat(problem.getUserMessage()).isEqualTo("One or more fields are invalid. Please check it and try again.");
        assertThat(problem.getObjects().size()).isEqualTo(1);
        assertThat(problem.getObjects().get(0).getName()).isEqualTo("email");
        assertThat(problem.getObjects().get(0).getUserMessage()).isEqualTo("Required Field");
    }

    @Test
    void handleHttpMessageNotReadable() {
        HttpMessageNotReadableException exception = new HttpMessageNotReadableException("Malformed JSON request",
                mock(HttpInputMessage.class));
        HttpHeaders headers = new HttpHeaders();
        HttpStatus status = HttpStatus.BAD_REQUEST;

        ResponseEntity<Object> response = underTest.handleHttpMessageNotReadable(
                exception, headers, status, request
        );

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        Problem problem = (Problem) response.getBody();
        assertThat(problem).isNotNull();
        assertThat(problem.getTitle()).isEqualTo("Incomprehensible message");
        assertThat(problem.getUserMessage()).isEqualTo("The request body is invalid. Check syntax, types and try again.");
    }

    @Test
    void handleEntityNotFoundException_shouldReturnNotFoundWithProblemDetails() {
        EntityNotFoundException exception = new EntityNotFoundException("Entity", "123");

        ResponseEntity<Object> response = underTest.handleEntityNotFoundException(exception, request);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

        Problem problem = (Problem) response.getBody();
        assertThat(problem).isNotNull();
        assertThat(problem.getDetail()).contains("Entity not found");
        assertThat(problem.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
        assertThat(problem.getTitle()).isEqualTo("Resource not found");
    }
}
