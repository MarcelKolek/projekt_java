package pl.taskmanager.taskmanager.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import pl.taskmanager.taskmanager.entity.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);
}
