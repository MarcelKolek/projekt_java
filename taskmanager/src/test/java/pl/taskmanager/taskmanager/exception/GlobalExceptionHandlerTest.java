package pl.taskmanager.taskmanager.exception;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @org.junit.jupiter.api.Test
    void shouldHandleNotFound() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Not found");
        org.springframework.http.ResponseEntity<java.util.Map<String, String>> response = handler.handleNotFound(ex);
        org.assertj.core.api.Assertions.assertThat(response.getStatusCode().value()).isEqualTo(404);
        org.assertj.core.api.Assertions.assertThat(response.getBody().get("error")).isEqualTo("Not found");
    }

    @org.junit.jupiter.api.Test
    void shouldHandleValidation() {
        org.springframework.web.bind.MethodArgumentNotValidException ex = org.mockito.Mockito.mock(org.springframework.web.bind.MethodArgumentNotValidException.class);
        org.springframework.validation.BindingResult bindingResult = org.mockito.Mockito.mock(org.springframework.validation.BindingResult.class);
        org.mockito.Mockito.when(ex.getBindingResult()).thenReturn(bindingResult);
        org.mockito.Mockito.when(bindingResult.getAllErrors()).thenReturn(java.util.List.of(
                new org.springframework.validation.FieldError("obj", "field", "message"),
                new org.springframework.validation.ObjectError("obj", "global message")
        ));

        org.springframework.http.ResponseEntity<java.util.Map<String, String>> response = handler.handleValidationExceptions(ex);
        org.assertj.core.api.Assertions.assertThat(response.getStatusCode().value()).isEqualTo(400);
        org.assertj.core.api.Assertions.assertThat(response.getBody().get("field")).isEqualTo("message");
        org.assertj.core.api.Assertions.assertThat(response.getBody().get("obj")).isEqualTo("global message");
    }

    @org.junit.jupiter.api.Test
    void shouldHandleNoResourceFound() {
        org.springframework.web.servlet.resource.NoResourceFoundException ex = org.mockito.Mockito.mock(org.springframework.web.servlet.resource.NoResourceFoundException.class);
        org.mockito.Mockito.when(ex.getResourcePath()).thenReturn("favicon.ico");
        org.springframework.http.ResponseEntity<java.util.Map<String, String>> response = handler.handleNoResourceFound(ex);
        org.assertj.core.api.Assertions.assertThat(response.getStatusCode().value()).isEqualTo(404);
        org.assertj.core.api.Assertions.assertThat(response.getBody().get("error")).isEqualTo("Not found");
    }

    @org.junit.jupiter.api.Test
    void shouldHandleIllegalArgument() {
        IllegalArgumentException ex = new IllegalArgumentException("Illegal");
        org.springframework.http.ResponseEntity<java.util.Map<String, String>> response = handler.handleIllegalArgument(ex);
        org.assertj.core.api.Assertions.assertThat(response.getStatusCode().value()).isEqualTo(400);
        org.assertj.core.api.Assertions.assertThat(response.getBody().get("error")).isEqualTo("Illegal");
    }

    @org.junit.jupiter.api.Test
    void shouldHandleIOException() {
        java.io.IOException ex = new java.io.IOException("IO error");
        org.springframework.http.ResponseEntity<java.util.Map<String, String>> response = handler.handleIOException(ex);
        org.assertj.core.api.Assertions.assertThat(response.getStatusCode().value()).isEqualTo(500);
        org.assertj.core.api.Assertions.assertThat(response.getBody().get("error")).isEqualTo("Unexpected file IO error occurred.");
    }

    @org.junit.jupiter.api.Test
    void shouldHandleGeneralException() {
        Exception ex = new Exception("General");
        org.springframework.http.ResponseEntity<java.util.Map<String, String>> response = handler.handleGeneralException(ex);
        org.assertj.core.api.Assertions.assertThat(response.getStatusCode().value()).isEqualTo(500);
        org.assertj.core.api.Assertions.assertThat(response.getBody().get("error")).isEqualTo("Unexpected server error occurred.");
    }
}
