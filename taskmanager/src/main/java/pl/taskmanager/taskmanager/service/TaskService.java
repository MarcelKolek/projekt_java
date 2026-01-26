package pl.taskmanager.taskmanager.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import pl.taskmanager.taskmanager.dao.TaskStatsJdbcDao;

import pl.taskmanager.taskmanager.dto.TaskRequest;
import pl.taskmanager.taskmanager.dto.TaskResponse;
import pl.taskmanager.taskmanager.dto.TaskStatsResponse;

import pl.taskmanager.taskmanager.entity.Category;
import pl.taskmanager.taskmanager.entity.Task;
import pl.taskmanager.taskmanager.entity.TaskStatus;
import pl.taskmanager.taskmanager.entity.User;

import pl.taskmanager.taskmanager.exception.ResourceNotFoundException;

import pl.taskmanager.taskmanager.repository.CategoryRepository;
import pl.taskmanager.taskmanager.repository.TaskRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class TaskService {

    private static final Logger log = LoggerFactory.getLogger(TaskService.class);

    private final TaskRepository taskRepository;
    private final UserService userService;
    private final CategoryRepository categoryRepository;
    private final TaskStatsJdbcDao taskStatsJdbcDao;

    public TaskService(
            TaskRepository taskRepository,
            UserService userService,
            CategoryRepository categoryRepository,
            TaskStatsJdbcDao taskStatsJdbcDao
    ) {
        this.taskRepository = taskRepository;
        this.userService = userService;
        this.categoryRepository = categoryRepository;
        this.taskStatsJdbcDao = taskStatsJdbcDao;
    }

    public Page<TaskResponse> list(
            String username,
            TaskStatus status,
            Long categoryId,
            String q,
            LocalDate dueBefore,
            LocalDate dueAfter,
            Pageable pageable
    ) {
        log.debug("Listing tasks for user: {}", username);

        return taskRepository.search(username, status, categoryId, q, dueBefore, dueAfter, pageable)
                .map(TaskResponse::new);
    }

    @Transactional
    public TaskResponse create(TaskRequest req, String username) {
        log.info("Creating task: {} for user: {}", req.title, username);

        User user = userService.findByUsername(username);

        Task task = new Task();
        task.setUser(user);

        applyRequest(task, req, user);
        return save(task);
    }

    @Transactional
    public TaskResponse update(Long id, TaskRequest req, String username) {
        log.info("Updating task id: {} for user: {}", id, username);

        Task task = getTaskEntity(id, username);
        applyRequest(task, req, task.getUser());

        return save(task);
    }

    @Transactional
    public TaskResponse save(Task task) {
        log.info("Saving task: {}", task.getTitle());
        return new TaskResponse(taskRepository.save(task));
    }

    @Transactional
    public TaskResponse updateWithFile(Long id, String filename, String username) {
        log.info("Updating task id: {} with file: {} for user: {}", id, filename, username);

        Task task = getTaskEntity(id, username);
        task.setAttachmentFilename(filename);

        return save(task);
    }

    private void applyRequest(Task task, TaskRequest req, User user) {
        task.setTitle(req.title);
        task.setDescription(req.description);

        if (req.status != null) {
            task.setStatus(req.status);
        }

        task.setDueDate(req.dueDate);

        if (req.categoryId == null) {
            task.setCategory(null);
            return;
        }

        Category cat = categoryRepository.findById(req.categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        if (!cat.getUser().getId().equals(user.getId())) {
            throw new ResourceNotFoundException("Category not found");
        }

        task.setCategory(cat);
    }

    @Transactional
    public void delete(Long id, String username) {
        log.info("Deleting task id: {} for user: {}", id, username);

        Task task = getTaskEntity(id, username);
        taskRepository.delete(task);
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> findAllByUser(String username) {
        log.debug("Finding all tasks for user: {}", username);

        User user = userService.findByUsername(username);

        return taskRepository.findAllByUser(user).stream()
                .map(TaskResponse::new)
                .collect(Collectors.toList());
    }

    public TaskResponse getById(Long id, String username) {
        return new TaskResponse(getTaskEntity(id, username));
    }

    public Task getTaskEntity(Long id, String username) {
        log.debug("Getting task entity id: {} for user: {}", id, username);

        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));

        if (!task.getUser().getUsername().equals(username)) {
            throw new ResourceNotFoundException("Task not found");
        }

        return task;
    }

    public TaskStatsResponse getStats(String username) {
        Long userId = userService.findIdByUsername(username);
        return taskStatsJdbcDao.getStats(userId);
    }
}
