package pl.taskmanager.taskmanager.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CategoryRepository
        extends JpaRepository<
                pl.taskmanager.taskmanager.entity.Category,
                Long
        > {

    List<pl.taskmanager.taskmanager.entity.Category>
    findAllByUser(pl.taskmanager.taskmanager.entity.User user);
}
