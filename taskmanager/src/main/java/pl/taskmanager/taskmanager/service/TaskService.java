package pl.taskmanager.taskmanager.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.taskmanager.taskmanager.entity.Task;
import pl.taskmanager.taskmanager.entity.TaskStatus;
import pl.taskmanager.taskmanager.repository.TaskRepository;

import java.time.LocalDate;

@Service
@Transactional(readOnly = true)
public class TaskService {

    private final TaskRepository taskRepository;

    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    public Page<Task> list(
            TaskStatus status,
            Long categoryId,
            String q,
            LocalDate dueBefore,
            LocalDate dueAfter,
            Pageable pageable
    ) {
        return taskRepository.search(
                status,
                categoryId,
                q,
                dueBefore,
                dueAfter,
                pageable
        );
    }
}
