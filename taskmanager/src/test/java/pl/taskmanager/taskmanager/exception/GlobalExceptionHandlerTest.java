package pl.taskmanager.taskmanager.exception;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void shouldHandleNotFound() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Not found");

        ResponseEntity<Map<String, String>> response = handler.handleNotFound(ex);

        Assertions.assertThat(response.getStatusCode().value()).isEqualTo(404);
        Assertions.assertThat(response.getBody().get("error")).isEqualTo("Not found");
    }

    @Test
    void shouldHandleValidation() {
        MethodArgumentNotValidException ex = Mockito.mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = Mockito.mock(BindingResult.class);

        Mockito.when(ex.getBindingResult()).thenReturn(bindingResult);
        Mockito.when(bindingResult.getAllErrors()).thenReturn(List.of(
                new FieldError("obj", "field", "message"),
                new ObjectError("obj", "global message")
        ));

        ResponseEntity<Map<String, String>> response = handler.handleValidationExceptions(ex);

        Assertions.assertThat(response.getStatusCode().value()).isEqualTo(400);
        Assertions.assertThat(response.getBody().get("field")).isEqualTo("message");
        Assertions.assertThat(response.getBody().get("obj")).isEqualTo("global message");
    }

    @Test
    void shouldHandleNoResourceFound() {
        NoResourceFoundException ex = Mockito.mock(NoResourceFoundException.class);
        Mockito.when(ex.getResourcePath()).thenReturn("favicon.ico");

        ResponseEntity<Map<String, String>> response = handler.handleNoResourceFound(ex);

        Assertions.assertThat(response.getStatusCode().value()).isEqualTo(404);
        Assertions.assertThat(response.getBody().get("error")).isEqualTo("Not found");
    }

    @Test
    void shouldHandleIllegalArgument() {
        IllegalArgumentException ex = new IllegalArgumentException("Illegal");

        ResponseEntity<Map<String, String>> response = handler.handleIllegalArgument(ex);

        Assertions.assertThat(response.getStatusCode().value()).isEqualTo(400);
        Assertions.assertThat(response.getBody().get("error")).isEqualTo("Illegal");
    }

    @Test
    void shouldHandleIOException() {
        IOException ex = new IOException("IO error");

        ResponseEntity<Map<String, String>> response = handler.handleIOException(ex);

        Assertions.assertThat(response.getStatusCode().value()).isEqualTo(500);
        Assertions.assertThat(response.getBody().get("error"))
                .isEqualTo("Unexpected file IO error occurred.");
    }

    @Test
    void shouldHandleGeneralException() {
        Exception ex = new Exception("General");

        ResponseEntity<Map<String, String>> response = handler.handleGeneralException(ex);

        Assertions.assertThat(response.getStatusCode().value()).isEqualTo(500);
        Assertions.assertThat(response.getBody().get("error"))
                .isEqualTo("Unexpected server error occurred.");
    }
}
