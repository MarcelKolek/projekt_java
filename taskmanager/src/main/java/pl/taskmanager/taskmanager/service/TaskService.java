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

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TaskService.class);
    private final TaskRepository taskRepository;

    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    public Page<Task> list(
            String username,
            TaskStatus status,
            Long categoryId,
            String q,
            LocalDate dueBefore,
            LocalDate dueAfter,
            Pageable pageable
    ) {
        log.debug("Listing tasks for user: {}", username);
        return taskRepository.search(
                username,
                status,
                categoryId,
                q,
                dueBefore,
                dueAfter,
                pageable
        );
    }

    @Transactional
    public Task save(Task task) {
        log.info("Saving task: {}", task.getTitle());
        return taskRepository.save(task);
    }

    @Transactional
    public void delete(Long id, String username) {
        log.info("Deleting task id: {} for user: {}", id, username);
        Task task = getById(id, username);
        taskRepository.delete(task);
    }

    public Task getById(Long id, String username) {
        log.debug("Getting task id: {} for user: {}", id, username);
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new pl.taskmanager.taskmanager.exception.ResourceNotFoundException("Task not found"));
        if (!task.getUser().getUsername().equals(username)) {
            throw new pl.taskmanager.taskmanager.exception.ResourceNotFoundException("Task not found");
        }
        return task;
    }
}
