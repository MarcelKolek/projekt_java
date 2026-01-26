package pl.taskmanager.taskmanager.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import pl.taskmanager.taskmanager.entity.Category;
import pl.taskmanager.taskmanager.entity.User;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    List<Category> findAllByUser(User user);
}
