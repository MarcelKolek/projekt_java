package pl.taskmanager.taskmanager.service;

@org.springframework.stereotype.Service
public class UserService implements org.springframework.security.core.userdetails.UserDetailsService {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(UserService.class);

    private final pl.taskmanager.taskmanager.repository.UserRepository userRepository;
    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    public UserService(pl.taskmanager.taskmanager.repository.UserRepository userRepository, org.springframework.security.crypto.password.PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public org.springframework.security.core.userdetails.UserDetails loadUserByUsername(String username) throws org.springframework.security.core.userdetails.UsernameNotFoundException {
        log.debug("Loading user by username: {}", username);
        pl.taskmanager.taskmanager.entity.User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new org.springframework.security.core.userdetails.UsernameNotFoundException("User not found: " + username));

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                user.getRoles().stream()
                        .map(org.springframework.security.core.authority.SimpleGrantedAuthority::new)
                        .collect(java.util.stream.Collectors.toList())
        );
    }

    @org.springframework.transaction.annotation.Transactional
    public void register(pl.taskmanager.taskmanager.dto.RegisterRequest req) {
        log.info("Registering new user: {}", req.getUsername());
        if (userRepository.existsByUsername(req.getUsername())) {
            log.warn("Username already exists: {}", req.getUsername());
            throw new IllegalArgumentException("Username already exists");
        }

        pl.taskmanager.taskmanager.entity.User user = new pl.taskmanager.taskmanager.entity.User();
        user.setUsername(req.getUsername());
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        user.setEmail(req.getEmail());
        user.setRoles(java.util.Set.of("ROLE_USER"));

        userRepository.save(user);
        log.info("User registered successfully: {}", req.getUsername());
    }

    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public pl.taskmanager.taskmanager.entity.User findByUsername(String username) {
        log.debug("Finding user by username: {}", username);
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new org.springframework.security.core.userdetails.UsernameNotFoundException("User not found: " + username));
    }

    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public Long findIdByUsername(String username) {
        log.debug("Finding user id by username: {}", username);
        return findByUsername(username).getId();
    }
}
