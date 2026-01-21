package pl.taskmanager.taskmanager.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.taskmanager.taskmanager.entity.Category;

public interface CategoryRepository extends JpaRepository<Category, Long> {
}
