package pl.taskmanager.taskmanager.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void shouldHandleNotFound() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Not found");
        ResponseEntity<Map<String, String>> response = handler.handleNotFound(ex);
        assertThat(response.getStatusCode().value()).isEqualTo(404);
        assertThat(response.getBody().get("error")).isEqualTo("Not found");
    }

    @Test
    void shouldHandleValidation() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getAllErrors()).thenReturn(List.of(
                new FieldError("obj", "field", "message")
        ));

        ResponseEntity<Map<String, String>> response = handler.handleValidationExceptions(ex);
        assertThat(response.getStatusCode().value()).isEqualTo(400);
        assertThat(response.getBody().get("field")).isEqualTo("message");
    }

    @Test
    void shouldHandleIllegalArgument() {
        IllegalArgumentException ex = new IllegalArgumentException("Illegal");
        ResponseEntity<Map<String, String>> response = handler.handleIllegalArgument(ex);
        assertThat(response.getStatusCode().value()).isEqualTo(400);
        assertThat(response.getBody().get("error")).isEqualTo("Illegal");
    }

    @Test
    void shouldHandleGeneralException() {
        Exception ex = new Exception("General");
        ResponseEntity<Map<String, String>> response = handler.handleGeneralException(ex);
        assertThat(response.getStatusCode().value()).isEqualTo(500);
        assertThat(response.getBody().get("error")).isEqualTo("Unexpected server error occurred.");
    }
}
