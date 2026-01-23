package pl.taskmanager.taskmanager.dto;

public class RegisterRequest {

    @jakarta.validation.constraints.NotBlank(message = "Username cannot be empty")
    @jakarta.validation.constraints.Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private java.lang.String username;

    @jakarta.validation.constraints.NotBlank(message = "Password must be at least 6 characters")
    @jakarta.validation.constraints.Size(min = 6, message = "Password must be at least 6 characters")
    private java.lang.String password;

    @jakarta.validation.constraints.Email(message = "Email must be valid")
    private java.lang.String email;

    public RegisterRequest() {}

    public java.lang.String getUsername() { return username; }
    public void setUsername(java.lang.String username) { this.username = username; }
    public java.lang.String getPassword() { return password; }
    public void setPassword(java.lang.String password) { this.password = password; }
    public java.lang.String getEmail() { return email; }
    public void setEmail(java.lang.String email) { this.email = email; }
}
