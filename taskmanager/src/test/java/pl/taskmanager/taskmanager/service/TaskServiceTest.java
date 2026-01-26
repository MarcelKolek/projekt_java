package pl.taskmanager.taskmanager.service;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import pl.taskmanager.taskmanager.dao.TaskJdbcDao;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private UserService userService;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private TaskJdbcDao taskJdbcDao;

    @Mock
    private TaskStatsJdbcDao taskStatsJdbcDao;

    @InjectMocks
    private TaskService taskService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldListTasks() {
        when(taskRepository.search(
                any(), any(), any(), any(), any(), any(), any()
        )).thenReturn(Page.empty());

        Page<TaskResponse> result = taskService.list(
                "user",
                null,
                null,
                null,
                null,
                null,
                Pageable.unpaged()
        );

        assertThat(result).isEmpty();

        verify(taskRepository).search(
                eq("user"),
                any(),
                any(),
                any(),
                any(),
                any(),
                any()
        );
    }

    @Test
    void shouldSaveTask() {
        Task task = new Task();
        task.setTitle("Test");

        when(taskRepository.save(task)).thenReturn(task);

        TaskResponse saved = taskService.save(task);

        assertThat(saved.title).isEqualTo("Test");
        verify(taskRepository).save(task);
    }

    @Test
    void shouldGetTaskById() {
        User user = new User();
        user.setUsername("owner");

        Task task = new Task();
        task.setUser(user);

        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

        TaskResponse result = taskService.getById(1L, "owner");

        assertThat(result.id).isEqualTo(task.getId());
    }

    @Test
    void shouldThrowWhenTaskNotFound() {
        when(taskRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.getById(1L, "any"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void shouldThrowWhenTaskNotOwnedByUser() {
        User owner = new User();
        owner.setUsername("owner");

        Task task = new Task();
        task.setUser(owner);

        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

        assertThatThrownBy(() -> taskService.getById(1L, "stranger"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void shouldDeleteTask() {
        User user = new User();
        user.setUsername("owner");

        Task task = new Task();
        task.setUser(user);

        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

        taskService.delete(1L, "owner");

        verify(taskRepository).delete(task);
    }

    @Test
    void shouldCreateTask() {
        String username = "testuser";

        User user = new User();
        user.setId(1L);
        user.setUsername(username);

        TaskRequest req = new TaskRequest();
        req.title = "New Task";
        req.status = TaskStatus.TODO;

        when(userService.findByUsername(username)).thenReturn(user);
        when(taskRepository.save(any(Task.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        TaskResponse result = taskService.create(req, username);

        assertThat(result.title).isEqualTo("New Task");
        verify(taskRepository).save(any(Task.class));
    }

    @Test
    void shouldUpdateTask() {
        String username = "testuser";

        User user = new User();
        user.setId(1L);
        user.setUsername(username);

        Task task = new Task();
        task.setId(1L);
        task.setTitle("Old Title");
        task.setUser(user);

        TaskRequest req = new TaskRequest();
        req.title = "Updated Title";
        req.status = TaskStatus.IN_PROGRESS;

        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(taskRepository.save(any(Task.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        TaskResponse result = taskService.update(1L, req, username);

        assertThat(result.title).isEqualTo("Updated Title");
        verify(taskRepository).save(task);
    }

    @Test
    void shouldFindAllByUser() {
        String username = "testuser";

        User user = new User();
        user.setUsername(username);

        when(userService.findByUsername(username)).thenReturn(user);
        when(taskRepository.findAllByUser(user))
                .thenReturn(List.of(new Task()));

        List<TaskResponse> result = taskService.findAllByUser(username);

        assertThat(result).hasSize(1);
    }

    @Test
    void shouldUpdateWithFile() {
        String username = "testuser";

        User user = new User();
        user.setUsername(username);

        Task task = new Task();
        task.setUser(user);

        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(taskRepository.save(any(Task.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        TaskResponse result = taskService.updateWithFile(1L, "file.txt", username);

        assertThat(result.attachmentFilename).isEqualTo("file.txt");
    }

    @Test
    void shouldUpdateTaskStatusInRepo() {
        Task task = new Task();
        task.setStatus(TaskStatus.TODO);
        task.setStatus(TaskStatus.DONE);

        assertThat(task.getStatus()).isEqualTo(TaskStatus.DONE);
    }

    @Test
    void shouldCreateTaskWithCategory() {
        String username = "testuser";

        User user = new User();
        user.setId(1L);
        user.setUsername(username);

        Category category = new Category();
        category.setId(10L);
        category.setUser(user);

        TaskRequest req = new TaskRequest();
        req.title = "Task with category";
        req.categoryId = 10L;

        when(userService.findByUsername(username)).thenReturn(user);
        when(categoryRepository.findById(10L)).thenReturn(Optional.of(category));
        when(taskRepository.save(any(Task.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        TaskResponse result = taskService.create(req, username);

        assertThat(result.category.id).isEqualTo(10L);
    }

    @Test
    void shouldThrowWhenCategoryNotFound() {
        String username = "testuser";

        User user = new User();
        user.setId(1L);
        user.setUsername(username);

        TaskRequest req = new TaskRequest();
        req.categoryId = 99L;

        when(userService.findByUsername(username)).thenReturn(user);
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.create(req, username))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Category not found");
    }

    @Test
    void shouldThrowWhenCategoryNotOwnedByUser() {
        String username = "testuser";

        User user = new User();
        user.setId(1L);
        user.setUsername(username);

        User otherUser = new User();
        otherUser.setId(2L);

        Category category = new Category();
        category.setId(10L);
        category.setUser(otherUser);

        TaskRequest req = new TaskRequest();
        req.categoryId = 10L;

        when(userService.findByUsername(username)).thenReturn(user);
        when(categoryRepository.findById(10L)).thenReturn(Optional.of(category));

        assertThatThrownBy(() -> taskService.create(req, username))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Category not found");
    }

    @Test
    void shouldGetStats() {
        String username = "testuser";

        when(userService.findIdByUsername(username)).thenReturn(1L);

        TaskStatsResponse stats =
                new TaskStatsResponse(10, 5, 3, 2, 20.0);

        when(taskStatsJdbcDao.getStats(1L)).thenReturn(stats);

        TaskStatsResponse result = taskService.getStats(username);

        assertThat(result).isSameAs(stats);
    }
}
