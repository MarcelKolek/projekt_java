package pl.taskmanager.taskmanager.service;

class UserServiceTest {

    @org.mockito.Mock
    private pl.taskmanager.taskmanager.repository.UserRepository userRepository;

    @org.mockito.Mock
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @org.mockito.InjectMocks
    private pl.taskmanager.taskmanager.service.UserService userService;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        org.mockito.MockitoAnnotations.openMocks(this);
    }

    @org.junit.jupiter.api.Test
    void shouldLoadUserByUsername() {
        pl.taskmanager.taskmanager.entity.User user = new pl.taskmanager.taskmanager.entity.User();
        user.setUsername("testuser");
        user.setPassword("encodedPass");
        user.setRoles(java.util.Set.of("ROLE_USER"));

        org.mockito.Mockito.when(userRepository.findByUsername("testuser"))
                .thenReturn(java.util.Optional.of(user));

        org.springframework.security.core.userdetails.UserDetails userDetails =
                userService.loadUserByUsername("testuser");

        org.assertj.core.api.Assertions.assertThat(userDetails)
                .returns("testuser", org.springframework.security.core.userdetails.UserDetails::getUsername)
                .returns("encodedPass", org.springframework.security.core.userdetails.UserDetails::getPassword);
    }

    @org.junit.jupiter.api.Test
    void shouldThrowExceptionWhenUserNotFound() {
        org.mockito.Mockito.when(userRepository.findByUsername("none"))
                .thenReturn(java.util.Optional.empty());

        org.assertj.core.api.Assertions.assertThatThrownBy(
                        () -> userService.loadUserByUsername("none"))
                .isInstanceOf(org.springframework.security.core.userdetails.UsernameNotFoundException.class);
    }

    @org.junit.jupiter.api.Test
    void shouldRegisterUser() {
        pl.taskmanager.taskmanager.dto.RegisterRequest req =
                new pl.taskmanager.taskmanager.dto.RegisterRequest();
        req.setUsername("newuser");
        req.setPassword("password");
        req.setEmail("test@example.com");

        org.mockito.Mockito.when(userRepository.existsByUsername("newuser"))
                .thenReturn(false);
        org.mockito.Mockito.when(passwordEncoder.encode("password"))
                .thenReturn("encoded");

        userService.register(req);

        org.mockito.Mockito.verify(userRepository, org.mockito.Mockito.times(1))
                .save(org.mockito.ArgumentMatchers.any(pl.taskmanager.taskmanager.entity.User.class));
    }

    @org.junit.jupiter.api.Test
    void shouldThrowExceptionWhenUsernameExists() {
        pl.taskmanager.taskmanager.dto.RegisterRequest req =
                new pl.taskmanager.taskmanager.dto.RegisterRequest();
        req.setUsername("exists");

        org.mockito.Mockito.when(userRepository.existsByUsername("exists"))
                .thenReturn(true);

        org.assertj.core.api.Assertions.assertThatThrownBy(() -> userService.register(req))
                .isInstanceOf(java.lang.IllegalArgumentException.class)
                .hasMessage("Username already exists");
    }

    @org.junit.jupiter.api.Test
    void shouldFindByUsername() {
        pl.taskmanager.taskmanager.entity.User user =
                new pl.taskmanager.taskmanager.entity.User();
        user.setUsername("user");

        org.mockito.Mockito.when(userRepository.findByUsername("user"))
                .thenReturn(java.util.Optional.of(user));

        pl.taskmanager.taskmanager.entity.User result =
                userService.findByUsername("user");

        org.assertj.core.api.Assertions.assertThat(result.getUsername())
                .isEqualTo("user");
    }
}
