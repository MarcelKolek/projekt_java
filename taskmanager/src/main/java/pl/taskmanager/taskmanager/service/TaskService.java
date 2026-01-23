package pl.taskmanager.taskmanager.service;

@org.springframework.stereotype.Service
@org.springframework.transaction.annotation.Transactional(readOnly = true)
public class TaskService {

    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(TaskService.class);

    private final pl.taskmanager.taskmanager.repository.TaskRepository taskRepository;
    private final pl.taskmanager.taskmanager.service.UserService userService;
    private final pl.taskmanager.taskmanager.repository.CategoryRepository categoryRepository;

    private final pl.taskmanager.taskmanager.dao.TaskStatsJdbcDao taskStatsJdbcDao;

    public TaskService(
            pl.taskmanager.taskmanager.repository.TaskRepository taskRepository,
            pl.taskmanager.taskmanager.service.UserService userService,
            pl.taskmanager.taskmanager.repository.CategoryRepository categoryRepository,
            pl.taskmanager.taskmanager.dao.TaskStatsJdbcDao taskStatsJdbcDao
    ) {
        this.taskRepository = taskRepository;
        this.userService = userService;
        this.categoryRepository = categoryRepository;
        this.taskStatsJdbcDao = taskStatsJdbcDao;
    }

    public org.springframework.data.domain.Page<pl.taskmanager.taskmanager.dto.TaskResponse> list(
            java.lang.String username,
            pl.taskmanager.taskmanager.entity.TaskStatus status,
            java.lang.Long categoryId,
            java.lang.String q,
            java.time.LocalDate dueBefore,
            java.time.LocalDate dueAfter,
            org.springframework.data.domain.Pageable pageable
    ) {
        log.debug("Listing tasks for user: {}", username);
        return taskRepository.search(
                username, status, categoryId, q, dueBefore, dueAfter, pageable
        ).map(pl.taskmanager.taskmanager.dto.TaskResponse::new);
    }

    @org.springframework.transaction.annotation.Transactional
    public pl.taskmanager.taskmanager.dto.TaskResponse create(
            pl.taskmanager.taskmanager.dto.TaskRequest req,
            java.lang.String username
    ) {
        log.info("Creating task: {} for user: {}", req.title, username);
        pl.taskmanager.taskmanager.entity.User user = userService.findByUsername(username);
        pl.taskmanager.taskmanager.entity.Task task = new pl.taskmanager.taskmanager.entity.Task();
        task.setUser(user);
        applyRequest(task, req, user);
        return save(task);
    }

    @org.springframework.transaction.annotation.Transactional
    public pl.taskmanager.taskmanager.dto.TaskResponse update(
            java.lang.Long id,
            pl.taskmanager.taskmanager.dto.TaskRequest req,
            java.lang.String username
    ) {
        log.info("Updating task id: {} for user: {}", id, username);
        pl.taskmanager.taskmanager.entity.Task task = getTaskEntity(id, username);
        applyRequest(task, req, task.getUser());
        return save(task);
    }

    @org.springframework.transaction.annotation.Transactional
    public pl.taskmanager.taskmanager.dto.TaskResponse save(pl.taskmanager.taskmanager.entity.Task task) {
        log.info("Saving task: {}", task.getTitle());
        return new pl.taskmanager.taskmanager.dto.TaskResponse(taskRepository.save(task));
    }

    @org.springframework.transaction.annotation.Transactional
    public pl.taskmanager.taskmanager.dto.TaskResponse updateWithFile(
            java.lang.Long id,
            java.lang.String filename,
            java.lang.String username
    ) {
        log.info("Updating task id: {} with file: {} for user: {}", id, filename, username);
        pl.taskmanager.taskmanager.entity.Task task = getTaskEntity(id, username);
        task.setAttachmentFilename(filename);
        return save(task);
    }

    private void applyRequest(
            pl.taskmanager.taskmanager.entity.Task task,
            pl.taskmanager.taskmanager.dto.TaskRequest req,
            pl.taskmanager.taskmanager.entity.User user
    ) {
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

    @org.springframework.transaction.annotation.Transactional
    public void delete(java.lang.Long id, java.lang.String username) {
        log.info("Deleting task id: {} for user: {}", id, username);
        pl.taskmanager.taskmanager.entity.Task task = getTaskEntity(id, username);
        taskRepository.delete(task);
    }

    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public java.util.List<pl.taskmanager.taskmanager.dto.TaskResponse> findAllByUser(java.lang.String username) {
        log.debug("Finding all tasks for user: {}", username);
        pl.taskmanager.taskmanager.entity.User user = userService.findByUsername(username);
        return taskRepository.findAllByUser(user).stream()
                .map(pl.taskmanager.taskmanager.dto.TaskResponse::new)
                .collect(java.util.stream.Collectors.toList());
    }

    public pl.taskmanager.taskmanager.dto.TaskResponse getById(java.lang.Long id, java.lang.String username) {
        return new pl.taskmanager.taskmanager.dto.TaskResponse(getTaskEntity(id, username));
    }

    public pl.taskmanager.taskmanager.entity.Task getTaskEntity(java.lang.Long id, java.lang.String username) {
        log.debug("Getting task entity id: {} for user: {}", id, username);
        pl.taskmanager.taskmanager.entity.Task task = taskRepository.findById(id)
                .orElseThrow(() -> new pl.taskmanager.taskmanager.exception.ResourceNotFoundException("Task not found"));
        if (!task.getUser().getUsername().equals(username)) {
            throw new pl.taskmanager.taskmanager.exception.ResourceNotFoundException("Task not found");
        }
        return task;
    }

    public pl.taskmanager.taskmanager.dto.TaskStatsResponse getStats(java.lang.String username) {
        java.lang.Long userId = userService.findIdByUsername(username);
        return taskStatsJdbcDao.getStats(userId);
    }
}
