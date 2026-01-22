package pl.taskmanager.taskmanager.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import pl.taskmanager.taskmanager.entity.Task;
import pl.taskmanager.taskmanager.entity.User;
import pl.taskmanager.taskmanager.exception.ResourceNotFoundException;
import pl.taskmanager.taskmanager.repository.TaskRepository;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private TaskService taskService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldListTasks() {
        when(taskRepository.search(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(Page.empty());

        Page<Task> result = taskService.list("user", null, null, null, null, null, Pageable.unpaged());

        assertThat(result).isEmpty();
        verify(taskRepository).search(eq("user"), any(), any(), any(), any(), any(), any());
    }

    @Test
    void shouldSaveTask() {
        Task task = new Task();
        task.setTitle("Test");
        when(taskRepository.save(task)).thenReturn(task);

        Task saved = taskService.save(task);

        assertThat(saved.getTitle()).isEqualTo("Test");
        verify(taskRepository).save(task);
    }

    @Test
    void shouldGetTaskById() {
        User user = new User();
        user.setUsername("owner");

        Task task = new Task();
        task.setUser(user);
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

        Task result = taskService.getById(1L, "owner");

        assertThat(result).isEqualTo(task);
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
    void shouldUpdateTaskStatusInRepo() {
        // Ten test jest bardziej dla pokrycia Repository/Entity jeśli byśmy tam mieli logikę
        Task task = new Task();
        task.setStatus(pl.taskmanager.taskmanager.entity.TaskStatus.TODO);
        task.setStatus(pl.taskmanager.taskmanager.entity.TaskStatus.DONE);
        assertThat(task.getStatus()).isEqualTo(pl.taskmanager.taskmanager.entity.TaskStatus.DONE);
    }
}
