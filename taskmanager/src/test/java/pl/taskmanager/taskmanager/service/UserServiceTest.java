package pl.taskmanager.taskmanager.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import pl.taskmanager.taskmanager.dto.RegisterRequest;
import pl.taskmanager.taskmanager.entity.User;
import pl.taskmanager.taskmanager.repository.UserRepository;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldLoadUserByUsername() {
        User user = new User();
        user.setUsername("testuser");
        user.setPassword("encodedPass");
        user.setRoles(Set.of("ROLE_USER"));

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        UserDetails userDetails = userService.loadUserByUsername("testuser");

        assertThat(userDetails.getUsername()).isEqualTo("testuser");
        assertThat(userDetails.getPassword()).isEqualTo("encodedPass");
    }

    @Test
    void shouldThrowExceptionWhenUserNotFound() {
        when(userRepository.findByUsername("none")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.loadUserByUsername("none"))
                .isInstanceOf(UsernameNotFoundException.class);
    }

    @Test
    void shouldRegisterUser() {
        RegisterRequest req = new RegisterRequest();
        req.setUsername("newuser");
        req.setPassword("password");
        req.setEmail("test@example.com");

        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(passwordEncoder.encode("password")).thenReturn("encoded");

        userService.register(req);

        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void shouldThrowExceptionWhenUsernameExists() {
        RegisterRequest req = new RegisterRequest();
        req.setUsername("exists");

        when(userRepository.existsByUsername("exists")).thenReturn(true);

        assertThatThrownBy(() -> userService.register(req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Username already exists");
    }

    @Test
    void shouldFindByUsername() {
        User user = new User();
        user.setUsername("user");
        when(userRepository.findByUsername("user")).thenReturn(Optional.of(user));

        User result = userService.findByUsername("user");
        assertThat(result.getUsername()).isEqualTo("user");
    }
}
