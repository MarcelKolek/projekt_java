package pl.taskmanager.taskmanager.service;

class TaskServiceTest {

    @org.mockito.Mock
    private pl.taskmanager.taskmanager.repository.TaskRepository taskRepository;

    @org.mockito.Mock
    private pl.taskmanager.taskmanager.service.UserService userService;

    @org.mockito.Mock
    private pl.taskmanager.taskmanager.dao.TaskJdbcDao taskJdbcDao;

    @org.mockito.InjectMocks
    private pl.taskmanager.taskmanager.service.TaskService taskService;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        org.mockito.MockitoAnnotations.openMocks(this);
    }

    @org.junit.jupiter.api.Test
    void shouldListTasks() {
        org.mockito.Mockito.when(taskRepository.search(
                        org.mockito.ArgumentMatchers.any(),
                        org.mockito.ArgumentMatchers.any(),
                        org.mockito.ArgumentMatchers.any(),
                        org.mockito.ArgumentMatchers.any(),
                        org.mockito.ArgumentMatchers.any(),
                        org.mockito.ArgumentMatchers.any(),
                        org.mockito.ArgumentMatchers.any()
                ))
                .thenReturn(org.springframework.data.domain.Page.empty());

        org.springframework.data.domain.Page<pl.taskmanager.taskmanager.dto.TaskResponse> result =
                taskService.list("user", null, null, null, null, null, org.springframework.data.domain.Pageable.unpaged());

        org.assertj.core.api.Assertions.assertThat(result).isEmpty();

        org.mockito.Mockito.verify(taskRepository).search(
                org.mockito.ArgumentMatchers.eq("user"),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any()
        );
    }

    @org.junit.jupiter.api.Test
    void shouldSaveTask() {
        pl.taskmanager.taskmanager.entity.Task task = new pl.taskmanager.taskmanager.entity.Task();
        task.setTitle("Test");

        org.mockito.Mockito.when(taskRepository.save(task)).thenReturn(task);

        pl.taskmanager.taskmanager.dto.TaskResponse saved = taskService.save(task);

        org.assertj.core.api.Assertions.assertThat(saved.title).isEqualTo("Test");
        org.mockito.Mockito.verify(taskRepository).save(task);
    }

    @org.junit.jupiter.api.Test
    void shouldGetTaskById() {
        pl.taskmanager.taskmanager.entity.User user = new pl.taskmanager.taskmanager.entity.User();
        user.setUsername("owner");

        pl.taskmanager.taskmanager.entity.Task task = new pl.taskmanager.taskmanager.entity.Task();
        task.setUser(user);

        org.mockito.Mockito.when(taskRepository.findById(1L)).thenReturn(java.util.Optional.of(task));

        pl.taskmanager.taskmanager.dto.TaskResponse result = taskService.getById(1L, "owner");

        org.assertj.core.api.Assertions.assertThat(result.id).isEqualTo(task.getId());
    }

    @org.junit.jupiter.api.Test
    void shouldThrowWhenTaskNotFound() {
        org.mockito.Mockito.when(taskRepository.findById(1L)).thenReturn(java.util.Optional.empty());

        org.assertj.core.api.Assertions.assertThatThrownBy(() -> taskService.getById(1L, "any"))
                .isInstanceOf(pl.taskmanager.taskmanager.exception.ResourceNotFoundException.class);
    }

    @org.junit.jupiter.api.Test
    void shouldThrowWhenTaskNotOwnedByUser() {
        pl.taskmanager.taskmanager.entity.User owner = new pl.taskmanager.taskmanager.entity.User();
        owner.setUsername("owner");

        pl.taskmanager.taskmanager.entity.Task task = new pl.taskmanager.taskmanager.entity.Task();
        task.setUser(owner);

        org.mockito.Mockito.when(taskRepository.findById(1L)).thenReturn(java.util.Optional.of(task));

        org.assertj.core.api.Assertions.assertThatThrownBy(() -> taskService.getById(1L, "stranger"))
                .isInstanceOf(pl.taskmanager.taskmanager.exception.ResourceNotFoundException.class);
    }

    @org.junit.jupiter.api.Test
    void shouldDeleteTask() {
        pl.taskmanager.taskmanager.entity.User user = new pl.taskmanager.taskmanager.entity.User();
        user.setUsername("owner");

        pl.taskmanager.taskmanager.entity.Task task = new pl.taskmanager.taskmanager.entity.Task();
        task.setUser(user);

        org.mockito.Mockito.when(taskRepository.findById(1L)).thenReturn(java.util.Optional.of(task));

        taskService.delete(1L, "owner");

        org.mockito.Mockito.verify(taskRepository).delete(task);
    }

    @org.junit.jupiter.api.Test
    void shouldCreateTask() {
        java.lang.String username = "testuser";

        pl.taskmanager.taskmanager.entity.User user = new pl.taskmanager.taskmanager.entity.User();
        user.setId(1L);
        user.setUsername(username);

        pl.taskmanager.taskmanager.dto.TaskRequest req = new pl.taskmanager.taskmanager.dto.TaskRequest();
        req.title = "New Task";
        req.status = pl.taskmanager.taskmanager.entity.TaskStatus.TODO;

        org.mockito.Mockito.when(userService.findByUsername(username)).thenReturn(user);
        org.mockito.Mockito.when(taskRepository.save(org.mockito.ArgumentMatchers.any(pl.taskmanager.taskmanager.entity.Task.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        pl.taskmanager.taskmanager.dto.TaskResponse result = taskService.create(req, username);

        org.assertj.core.api.Assertions.assertThat(result.title).isEqualTo("New Task");
        org.mockito.Mockito.verify(taskRepository).save(org.mockito.ArgumentMatchers.any(pl.taskmanager.taskmanager.entity.Task.class));
    }

    @org.junit.jupiter.api.Test
    void shouldUpdateTask() {
        java.lang.String username = "testuser";

        pl.taskmanager.taskmanager.entity.User user = new pl.taskmanager.taskmanager.entity.User();
        user.setId(1L);
        user.setUsername(username);

        pl.taskmanager.taskmanager.entity.Task task = new pl.taskmanager.taskmanager.entity.Task();
        task.setId(1L);
        task.setTitle("Old Title");
        task.setUser(user);

        pl.taskmanager.taskmanager.dto.TaskRequest req = new pl.taskmanager.taskmanager.dto.TaskRequest();
        req.title = "Updated Title";
        req.status = pl.taskmanager.taskmanager.entity.TaskStatus.IN_PROGRESS;

        org.mockito.Mockito.when(taskRepository.findById(1L)).thenReturn(java.util.Optional.of(task));
        org.mockito.Mockito.when(taskRepository.save(org.mockito.ArgumentMatchers.any(pl.taskmanager.taskmanager.entity.Task.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        pl.taskmanager.taskmanager.dto.TaskResponse result = taskService.update(1L, req, username);

        org.assertj.core.api.Assertions.assertThat(result.title).isEqualTo("Updated Title");
        org.mockito.Mockito.verify(taskRepository).save(task);
    }

    @org.junit.jupiter.api.Test
    void shouldFindAllByUser() {
        java.lang.String username = "testuser";

        pl.taskmanager.taskmanager.entity.User user = new pl.taskmanager.taskmanager.entity.User();
        user.setUsername(username);

        org.mockito.Mockito.when(userService.findByUsername(username)).thenReturn(user);
        org.mockito.Mockito.when(taskRepository.findAllByUser(user)).thenReturn(java.util.List.of(new pl.taskmanager.taskmanager.entity.Task()));

        java.util.List<pl.taskmanager.taskmanager.dto.TaskResponse> result = taskService.findAllByUser(username);

        org.assertj.core.api.Assertions.assertThat(result).hasSize(1);
    }

    @org.junit.jupiter.api.Test
    void shouldUpdateWithFile() {
        java.lang.String username = "testuser";

        pl.taskmanager.taskmanager.entity.User user = new pl.taskmanager.taskmanager.entity.User();
        user.setUsername(username);

        pl.taskmanager.taskmanager.entity.Task task = new pl.taskmanager.taskmanager.entity.Task();
        task.setUser(user);

        org.mockito.Mockito.when(taskRepository.findById(1L)).thenReturn(java.util.Optional.of(task));
        org.mockito.Mockito.when(taskRepository.save(org.mockito.ArgumentMatchers.any(pl.taskmanager.taskmanager.entity.Task.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        pl.taskmanager.taskmanager.dto.TaskResponse result = taskService.updateWithFile(1L, "file.txt", username);

        org.assertj.core.api.Assertions.assertThat(result.attachmentFilename).isEqualTo("file.txt");
    }

    @org.junit.jupiter.api.Test
    void shouldUpdateTaskStatusInRepo() {
        pl.taskmanager.taskmanager.entity.Task task = new pl.taskmanager.taskmanager.entity.Task();
        task.setStatus(pl.taskmanager.taskmanager.entity.TaskStatus.TODO);
        task.setStatus(pl.taskmanager.taskmanager.entity.TaskStatus.DONE);

        org.assertj.core.api.Assertions.assertThat(task.getStatus()).isEqualTo(pl.taskmanager.taskmanager.entity.TaskStatus.DONE);
    }
}
