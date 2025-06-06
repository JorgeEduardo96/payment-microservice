package br.com.clientservice.api.exceptionhandler;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.WebRequest;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
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
                new MethodArgumentNotValidException(null, bindingResult);

        ResponseEntity<Object> response = underTest.handleMethodArgumentNotValid(
                exception, new HttpHeaders(), HttpStatus.BAD_REQUEST, request);

        assertThat(HttpStatus.BAD_REQUEST).isEqualTo(response.getStatusCode());

        Problem problem = (Problem) response.getBody();
        assertThat(problem).isNotNull();
        assertThat(problem.getUserMessage()).isEqualTo("One or more fields are invalid. Please check it and try again.");
        assertThat(problem.getObjects().size()).isEqualTo(1);
        assertThat(problem.getObjects().get(0).getName()).isEqualTo("Email");
        assertThat(problem.getObjects().get(0).getUserMessage()).isEqualTo("Required Field");
    }
}
