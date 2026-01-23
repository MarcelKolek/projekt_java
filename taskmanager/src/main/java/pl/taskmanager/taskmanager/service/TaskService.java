package pl.taskmanager.taskmanager.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.taskmanager.taskmanager.dto.TaskResponse;
import pl.taskmanager.taskmanager.entity.Task;
import pl.taskmanager.taskmanager.entity.TaskStatus;
import pl.taskmanager.taskmanager.repository.TaskRepository;
import pl.taskmanager.taskmanager.repository.UserRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class TaskService {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TaskService.class);
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final pl.taskmanager.taskmanager.repository.CategoryRepository categoryRepository;
    private final pl.taskmanager.taskmanager.dao.TaskJdbcDao taskJdbcDao;

    public TaskService(TaskRepository taskRepository, UserRepository userRepository, pl.taskmanager.taskmanager.repository.CategoryRepository categoryRepository, pl.taskmanager.taskmanager.dao.TaskJdbcDao taskJdbcDao) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.taskJdbcDao = taskJdbcDao;
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
        return taskRepository.search(
                username,
                status,
                categoryId,
                q,
                dueBefore,
                dueAfter,
                pageable
        ).map(TaskResponse::new);
    }

    @Transactional
    public TaskResponse create(pl.taskmanager.taskmanager.dto.TaskRequest req, String username) {
        log.info("Creating task: {} for user: {}", req.title, username);
        pl.taskmanager.taskmanager.entity.User user = findUser(username);
        Task task = new Task();
        task.setUser(user);
        applyRequest(task, req, user);
        return save(task);
    }

    @Transactional
    public TaskResponse update(Long id, pl.taskmanager.taskmanager.dto.TaskRequest req, String username) {
        log.info("Updating task id: {} for user: {}", id, username);
        Task task = getTaskEntity(id, username);
        applyRequest(task, req, task.getUser());
        return save(task);
    }

    @Transactional
    public TaskResponse save(Task task) {
        log.info("Saving task: {}", task.getTitle());
        if (task.getId() == null && task.getUser() != null) {
            taskJdbcDao.insertLog("Tworzenie zadania: " + task.getTitle(), task.getUser().getId());
        }
        return new TaskResponse(taskRepository.save(task));
    }
    
    @Transactional
    public TaskResponse updateWithFile(Long id, String filename, String username) {
        Task task = getTaskEntity(id, username);
        task.setAttachmentFilename(filename);
        return save(task);
    }

    private void applyRequest(Task task, pl.taskmanager.taskmanager.dto.TaskRequest req, pl.taskmanager.taskmanager.entity.User user) {
        task.setTitle(req.title);
        task.setDescription(req.description);
        if (req.status != null) task.setStatus(req.status);
        task.setDueDate(req.dueDate);

        if (req.categoryId == null) {
            task.setCategory(null);
        } else {
            pl.taskmanager.taskmanager.entity.Category cat = categoryRepository.findById(req.categoryId)
                    .orElseThrow(() -> new pl.taskmanager.taskmanager.exception.ResourceNotFoundException("Category not found"));
            if (!cat.getUser().getId().equals(user.getId())) {
                throw new pl.taskmanager.taskmanager.exception.ResourceNotFoundException("Category not found");
            }
            task.setCategory(cat);
        }
    }

    private pl.taskmanager.taskmanager.entity.User findUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new org.springframework.security.core.userdetails.UsernameNotFoundException("User not found"));
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
        pl.taskmanager.taskmanager.entity.User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new org.springframework.security.core.userdetails.UsernameNotFoundException("User not found"));
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
                .orElseThrow(() -> new pl.taskmanager.taskmanager.exception.ResourceNotFoundException("Task not found"));
        if (!task.getUser().getUsername().equals(username)) {
            throw new pl.taskmanager.taskmanager.exception.ResourceNotFoundException("Task not found");
        }
        return task;
    }
}
