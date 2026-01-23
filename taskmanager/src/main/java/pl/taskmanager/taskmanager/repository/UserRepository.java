package pl.taskmanager.taskmanager.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository
        extends JpaRepository<
                pl.taskmanager.taskmanager.entity.User,
                Long
        > {

    Optional<pl.taskmanager.taskmanager.entity.User>
    findByUsername(String username);

    boolean existsByUsername(String username);
}
