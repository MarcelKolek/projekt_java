package pl.taskmanager.taskmanager.controller.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import pl.taskmanager.taskmanager.dao.TaskStatsJdbcDao;
import pl.taskmanager.taskmanager.dto.TaskRequest;
import pl.taskmanager.taskmanager.dto.TaskResponse;
import pl.taskmanager.taskmanager.entity.TaskStatus;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.springframework.mock.web.MockMultipartFile;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;

@WebMvcTest(TaskApiController.class)
@org.springframework.context.annotation.Import(pl.taskmanager.taskmanager.config.SecurityConfig.class)
class TaskApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TaskStatsJdbcDao taskStatsJdbcDao;

    @MockitoBean
    private pl.taskmanager.taskmanager.service.UserService userService;

    @MockitoBean
    private pl.taskmanager.taskmanager.service.TaskService taskService;

    @MockitoBean
    private pl.taskmanager.taskmanager.service.CategoryService categoryService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(username = "user")
    void shouldGetAllTasks() throws Exception {
        when(taskService.list(eq("user"), any(), any(), any(), any(), any(), any()))
                .thenReturn(org.springframework.data.domain.Page.empty());
        mockMvc.perform(get("/api/v1/tasks"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "user")
    void shouldCreateTask() throws Exception {
        TaskRequest req = new TaskRequest();
        req.title = "New Task";
        req.status = TaskStatus.TODO;

        TaskResponse saved = new TaskResponse();
        saved.id = 1L;
        saved.title = "New Task";
        when(taskService.create(any(TaskRequest.class), eq("user"))).thenReturn(saved);

        MockMultipartFile taskPart = new MockMultipartFile("task", "", "application/json", objectMapper.writeValueAsString(req).getBytes());

        mockMvc.perform(multipart("/api/v1/tasks")
                        .file(taskPart)
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("New Task"));
    }

    @Test
    @WithMockUser(username = "user")
    void shouldCreateTaskWithFile() throws Exception {
        TaskRequest req = new TaskRequest();
        req.title = "With File";
        req.status = TaskStatus.TODO;

        TaskResponse saved = new TaskResponse();
        saved.id = 10L;
        saved.title = "With File";
        when(taskService.create(any(TaskRequest.class), eq("user"))).thenReturn(saved);
        when(taskService.updateWithFile(eq(10L), any(), eq("user"))).thenReturn(saved);

        MockMultipartFile taskPart = new MockMultipartFile("task", "", "application/json", objectMapper.writeValueAsString(req).getBytes());
        MockMultipartFile filePart = new MockMultipartFile("file", "test.txt", "text/plain", "hello".getBytes());

        mockMvc.perform(multipart("/api/v1/tasks")
                        .file(taskPart)
                        .file(filePart)
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("With File"));
    }

    @Test
    @WithMockUser(username = "user")
    void shouldGetTaskById() throws Exception {
        TaskResponse task = new TaskResponse();
        task.id = 1L;
        task.title = "Found";
        when(taskService.getById(1L, "user")).thenReturn(task);

        mockMvc.perform(get("/api/v1/tasks/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Found"));
    }

    @Test
    @WithMockUser(username = "user")
    void shouldDeleteTask() throws Exception {
        mockMvc.perform(delete("/api/v1/tasks/1").with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldDenyAnonymous() throws Exception {
        mockMvc.perform(get("/api/v1/tasks"))
                .andExpect(status().isFound());
    }

    @Test
    @WithMockUser(username = "user")
    void shouldHandleNotFoundInUpdate() throws Exception {
        when(taskService.update(eq(999L), any(), eq("user"))).thenThrow(new pl.taskmanager.taskmanager.exception.ResourceNotFoundException("Task not found"));

        TaskRequest req = new TaskRequest();
        req.title = "Update";
        req.status = TaskStatus.TODO;

        MockMultipartFile taskPart = new MockMultipartFile("task", "", "application/json", objectMapper.writeValueAsString(req).getBytes());

        mockMvc.perform(multipart("/api/v1/tasks/999")
                        .file(taskPart)
                        .with(csrf())
                        .with(request -> { request.setMethod("PUT"); return request; }))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "user")
    void shouldExportCsv() throws Exception {
        when(taskService.findAllByUser("user")).thenReturn(java.util.List.of());

        mockMvc.perform(get("/api/v1/tasks/export/csv"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "text/csv; charset=utf-8"));
    }

    @Test
    @WithMockUser(username = "user")
    void shouldExportPdf() throws Exception {
        when(taskService.findAllByUser("user")).thenReturn(java.util.List.of());

        mockMvc.perform(get("/api/v1/tasks/export/pdf"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/pdf"));
    }

    @Test
    @WithMockUser(username = "user")
    void shouldStats() throws Exception {
        when(taskService.findAllByUser("user")).thenReturn(java.util.List.of());

        mockMvc.perform(get("/api/v1/tasks/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(0));
    }

    @Test
    @WithMockUser(username = "user")
    void shouldStatsJdbc() throws Exception {
        when(userService.findIdByUsername("user")).thenReturn(1L);
        when(taskStatsJdbcDao.getStats(1L)).thenReturn(new pl.taskmanager.taskmanager.dto.TaskStatsResponse(5, 2, 1, 2, 40.0));

        mockMvc.perform(get("/api/v1/tasks/stats/jdbc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(5))
                .andExpect(jsonPath("$.percentDone").value(40.0));
    }

    @Test
    @WithMockUser(username = "user")
    void shouldUploadFile() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "test.txt", "text/plain", "content".getBytes());
        
        TaskResponse task = new TaskResponse();
        task.id = 1L;
        task.attachmentFilename = "1_test.txt";
        
        when(taskService.updateWithFile(eq(1L), any(), eq("user"))).thenReturn(task);

        mockMvc.perform(multipart("/api/v1/tasks/upload")
                        .file(file)
                        .param("taskId", "1")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Plik zapisany")));
    }
}
