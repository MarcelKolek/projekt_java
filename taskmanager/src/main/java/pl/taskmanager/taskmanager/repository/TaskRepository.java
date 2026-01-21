package pl.taskmanager.taskmanager.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.taskmanager.taskmanager.entity.Task;

public interface TaskRepository extends JpaRepository<Task, Long> {
}
