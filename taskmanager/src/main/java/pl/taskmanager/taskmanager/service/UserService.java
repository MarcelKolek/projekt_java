package pl.taskmanager.taskmanager.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import pl.taskmanager.taskmanager.dto.RegisterRequest;
import pl.taskmanager.taskmanager.entity.User;
import pl.taskmanager.taskmanager.repository.UserRepository;

import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserService implements UserDetailsService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException {

        log.debug("Loading user by username: {}", username);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new UsernameNotFoundException("User not found: " + username)
                );

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                user.getRoles().stream()
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList())
        );
    }

    @Transactional
    public void register(RegisterRequest req) {
        log.info("Registering new user: {}", req.getUsername());

        if (userRepository.existsByUsername(req.getUsername())) {
            log.warn("Username already exists: {}", req.getUsername());
            throw new IllegalArgumentException("Username already exists");
        }

        User user = new User();
        user.setUsername(req.getUsername());
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        user.setEmail(req.getEmail());
        user.setRoles(Set.of("ROLE_USER"));

        userRepository.save(user);
        log.info("User registered successfully: {}", req.getUsername());
    }

    @Transactional(readOnly = true)
    public User findByUsername(String username) {
        log.debug("Finding user by username: {}", username);

        return userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new UsernameNotFoundException("User not found: " + username)
                );
    }

    @Transactional(readOnly = true)
    public Long findIdByUsername(String username) {
        log.debug("Finding user id by username: {}", username);
        return findByUsername(username).getId();
    }
}
