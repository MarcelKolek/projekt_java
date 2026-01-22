package pl.taskmanager.taskmanager.controller.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import pl.taskmanager.taskmanager.dao.TaskStatsJdbcDao;
import pl.taskmanager.taskmanager.dto.TaskRequest;
import pl.taskmanager.taskmanager.entity.Task;
import pl.taskmanager.taskmanager.entity.TaskStatus;
import pl.taskmanager.taskmanager.entity.User;
import pl.taskmanager.taskmanager.repository.CategoryRepository;
import pl.taskmanager.taskmanager.repository.TaskRepository;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TaskApiController.class)
class TaskApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TaskRepository taskRepository;

    @MockitoBean
    private CategoryRepository categoryRepository;

    @MockitoBean
    private TaskStatsJdbcDao taskStatsJdbcDao;

    @MockitoBean
    private pl.taskmanager.taskmanager.service.UserService userService;

    @MockitoBean
    private pl.taskmanager.taskmanager.service.TaskService taskService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(username = "user")
    void shouldGetAllTasks() throws Exception {
        mockMvc.perform(get("/api/v1/tasks"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "user")
    void shouldCreateTask() throws Exception {
        TaskRequest req = new TaskRequest();
        req.title = "New Task";
        req.status = TaskStatus.TODO;

        pl.taskmanager.taskmanager.entity.User user = new pl.taskmanager.taskmanager.entity.User();
        user.setUsername("user");
        when(userService.findByUsername("user")).thenReturn(user);

        Task saved = new Task();
        saved.setTitle("New Task");
        saved.setUser(user);
        when(taskService.save(any(Task.class))).thenReturn(saved);

        mockMvc.perform(post("/api/v1/tasks")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("New Task"));
    }

    @Test
    @WithMockUser(username = "user")
    void shouldGetTaskById() throws Exception {
        pl.taskmanager.taskmanager.entity.User user = new pl.taskmanager.taskmanager.entity.User();
        user.setUsername("user");

        Task task = new Task();
        task.setTitle("Found");
        task.setUser(user);
        when(taskService.getById(1L, "user")).thenReturn(task);

        mockMvc.perform(get("/api/v1/tasks/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Found"));
    }

    @Test
    @WithMockUser(username = "user")
    void shouldDeleteTask() throws Exception {
        pl.taskmanager.taskmanager.entity.User user = new pl.taskmanager.taskmanager.entity.User();
        user.setUsername("user");

        Task task = new Task();
        task.setUser(user);
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

        mockMvc.perform(delete("/api/v1/tasks/1").with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldDenyAnonymous() throws Exception {
        mockMvc.perform(get("/api/v1/tasks"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "user")
    void shouldHandleNotFoundInUpdate() throws Exception {
        when(taskRepository.findById(999L)).thenReturn(Optional.empty());

        TaskRequest req = new TaskRequest();
        req.title = "Update";
        req.status = TaskStatus.TODO;

        mockMvc.perform(put("/api/v1/tasks/999")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "user")
    void shouldExportCsv() throws Exception {
        User user = new User();
        user.setUsername("user");
        when(userService.findByUsername("user")).thenReturn(user);
        when(taskRepository.findAllByUser(user)).thenReturn(java.util.List.of());

        mockMvc.perform(get("/api/v1/tasks/export/csv"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "text/csv; charset=utf-8"));
    }
}
