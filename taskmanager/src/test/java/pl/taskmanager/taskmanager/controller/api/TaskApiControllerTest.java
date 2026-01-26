package pl.taskmanager.taskmanager.controller.api;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.hamcrest.Matchers;

import org.junit.jupiter.api.Test;

import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

import org.springframework.context.annotation.Import;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;

import org.springframework.data.domain.Page;

import org.springframework.mock.web.MockMultipartFile;

import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;

import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import pl.taskmanager.taskmanager.config.SecurityConfig;

import pl.taskmanager.taskmanager.dto.TaskRequest;
import pl.taskmanager.taskmanager.dto.TaskResponse;
import pl.taskmanager.taskmanager.dto.TaskStatsResponse;

import pl.taskmanager.taskmanager.entity.TaskStatus;

import pl.taskmanager.taskmanager.exception.ResourceNotFoundException;

import pl.taskmanager.taskmanager.service.CategoryService;
import pl.taskmanager.taskmanager.service.CsvService;
import pl.taskmanager.taskmanager.service.FileService;
import pl.taskmanager.taskmanager.service.PdfService;
import pl.taskmanager.taskmanager.service.TaskService;
import pl.taskmanager.taskmanager.service.UserService;

import java.util.List;

@WebMvcTest(TaskApiController.class)
@Import(SecurityConfig.class)
class TaskApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private TaskService taskService;

    @MockitoBean
    private CategoryService categoryService;

    @MockitoBean
    private CsvService csvService;

    @MockitoBean
    private PdfService pdfService;

    @MockitoBean
    private FileService fileService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(username = "user")
    void shouldGetAllTasks() throws Exception {
        Mockito.when(taskService.list(
                        Mockito.eq("user"),
                        ArgumentMatchers.any(),
                        ArgumentMatchers.any(),
                        ArgumentMatchers.any(),
                        ArgumentMatchers.any(),
                        ArgumentMatchers.any(),
                        ArgumentMatchers.any()
                ))
                .thenReturn(Page.empty());

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/tasks"))
                .andExpect(MockMvcResultMatchers.status().isOk());
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

        Mockito.when(taskService.create(ArgumentMatchers.any(TaskRequest.class), Mockito.eq("user")))
                .thenReturn(saved);

        MockMultipartFile taskPart = new MockMultipartFile(
                "task",
                "",
                "application/json",
                objectMapper.writeValueAsBytes(req)
        );

        mockMvc.perform(
                        MockMvcRequestBuilders.multipart("/api/v1/tasks")
                                .file(taskPart)
                                .with(SecurityMockMvcRequestPostProcessors.csrf())
                )
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.title").value("New Task"));
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

        Mockito.when(taskService.create(ArgumentMatchers.any(TaskRequest.class), Mockito.eq("user")))
                .thenReturn(saved);

        Mockito.when(taskService.updateWithFile(Mockito.eq(10L), ArgumentMatchers.any(), Mockito.eq("user")))
                .thenReturn(saved);

        MockMultipartFile taskPart = new MockMultipartFile(
                "task",
                "",
                "application/json",
                objectMapper.writeValueAsBytes(req)
        );

        MockMultipartFile filePart = new MockMultipartFile(
                "file",
                "test.txt",
                "text/plain",
                "hello".getBytes()
        );

        mockMvc.perform(
                        MockMvcRequestBuilders.multipart("/api/v1/tasks")
                                .file(taskPart)
                                .file(filePart)
                                .with(SecurityMockMvcRequestPostProcessors.csrf())
                )
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.title").value("With File"));
    }

    @Test
    @WithMockUser(username = "user")
    void shouldGetTaskById() throws Exception {
        TaskResponse task = new TaskResponse();
        task.id = 1L;
        task.title = "Found";

        Mockito.when(taskService.getById(1L, "user")).thenReturn(task);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/tasks/1"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.title").value("Found"));
    }

    @Test
    @WithMockUser(username = "user")
    void shouldDeleteTask() throws Exception {
        mockMvc.perform(
                        MockMvcRequestBuilders.delete("/api/v1/tasks/1")
                                .with(SecurityMockMvcRequestPostProcessors.csrf())
                )
                .andExpect(MockMvcResultMatchers.status().isNoContent());
    }

    @Test
    void shouldDenyAnonymous() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/tasks"))
                .andExpect(MockMvcResultMatchers.status().isFound());
    }

    @Test
    @WithMockUser(username = "user")
    void shouldHandleNotFoundInUpdate() throws Exception {
        Mockito.when(taskService.update(
                        Mockito.eq(999L),
                        ArgumentMatchers.any(),
                        Mockito.eq("user")
                ))
                .thenThrow(new ResourceNotFoundException("Task not found"));

        TaskRequest req = new TaskRequest();
        req.title = "Update";
        req.status = TaskStatus.TODO;

        MockMultipartFile taskPart = new MockMultipartFile(
                "task",
                "",
                "application/json",
                objectMapper.writeValueAsBytes(req)
        );

        mockMvc.perform(
                        MockMvcRequestBuilders.multipart("/api/v1/tasks/999")
                                .file(taskPart)
                                .with(SecurityMockMvcRequestPostProcessors.csrf())
                                .with(request -> {
                                    request.setMethod("PUT");
                                    return request;
                                })
                )
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    @WithMockUser(username = "user")
    void shouldExportCsv() throws Exception {
        Mockito.when(taskService.findAllByUser("user")).thenReturn(List.of());

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/tasks/export/csv"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.header().string("Content-Type", "text/csv; charset=utf-8"));
    }

    @Test
    @WithMockUser(username = "user")
    void shouldExportPdf() throws Exception {
        Mockito.when(taskService.findAllByUser("user")).thenReturn(List.of());

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/tasks/export/pdf"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.header().string("Content-Type", "application/pdf"));
    }

    @Test
    @WithMockUser(username = "user")
    void shouldStats() throws Exception {
        Mockito.when(taskService.getStats("user"))
                .thenReturn(new TaskStatsResponse(0, 0, 0, 0, 0.0));

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/tasks/stats"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.total").value(0));
    }

    @Test
    @WithMockUser(username = "user")
    void shouldUploadFile() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.txt",
                "text/plain",
                "content".getBytes()
        );

        TaskResponse task = new TaskResponse();
        task.id = 1L;
        task.attachmentFilename = "1_test.txt";

        Mockito.when(taskService.updateWithFile(Mockito.eq(1L), ArgumentMatchers.any(), Mockito.eq("user")))
                .thenReturn(task);

        mockMvc.perform(
                        MockMvcRequestBuilders.multipart("/api/v1/tasks/upload")
                                .file(file)
                                .param("taskId", "1")
                                .with(SecurityMockMvcRequestPostProcessors.csrf())
                )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(
                        Matchers.containsString("Plik zapisany")
                ));
    }

    @Test
    @WithMockUser(username = "user")
    void shouldUpdateTaskWithoutFile() throws Exception {
        TaskRequest req = new TaskRequest();
        req.title = "Updated Task";
        req.status = TaskStatus.DONE;

        TaskResponse saved = new TaskResponse();
        saved.id = 1L;
        saved.title = "Updated Task";

        Mockito.when(taskService.update(Mockito.eq(1L), ArgumentMatchers.any(TaskRequest.class), Mockito.eq("user")))
                .thenReturn(saved);

        MockMultipartFile taskPart = new MockMultipartFile(
                "task",
                "",
                "application/json",
                objectMapper.writeValueAsBytes(req)
        );

        mockMvc.perform(
                        MockMvcRequestBuilders.multipart("/api/v1/tasks/1")
                                .file(taskPart)
                                .with(SecurityMockMvcRequestPostProcessors.csrf())
                                .with(request -> {
                                    request.setMethod("PUT");
                                    return request;
                                })
                )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.title").value("Updated Task"));
    }

    @Test
    @WithMockUser(username = "user")
    void shouldUpdateTaskWithFile() throws Exception {
        TaskRequest req = new TaskRequest();
        req.title = "Updated With File";
        req.status = TaskStatus.DONE;

        TaskResponse saved = new TaskResponse();
        saved.id = 1L;
        saved.title = "Updated With File";

        Mockito.when(taskService.update(Mockito.eq(1L), ArgumentMatchers.any(TaskRequest.class), Mockito.eq("user")))
                .thenReturn(saved);

        Mockito.when(taskService.updateWithFile(Mockito.eq(1L), ArgumentMatchers.any(), Mockito.eq("user")))
                .thenReturn(saved);

        MockMultipartFile taskPart = new MockMultipartFile(
                "task",
                "",
                "application/json",
                objectMapper.writeValueAsBytes(req)
        );

        MockMultipartFile filePart = new MockMultipartFile(
                "file",
                "test.txt",
                "text/plain",
                "content".getBytes()
        );

        mockMvc.perform(
                        MockMvcRequestBuilders.multipart("/api/v1/tasks/1")
                                .file(taskPart)
                                .file(filePart)
                                .with(SecurityMockMvcRequestPostProcessors.csrf())
                                .with(request -> {
                                    request.setMethod("PUT");
                                    return request;
                                })
                )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.title").value("Updated With File"));
    }

    @Test
    @WithMockUser(username = "user")
    void shouldFailUploadEmptyFile() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.txt",
                "text/plain",
                new byte[0]
        );

        mockMvc.perform(
                        MockMvcRequestBuilders.multipart("/api/v1/tasks/upload")
                                .file(file)
                                .param("taskId", "1")
                                .with(SecurityMockMvcRequestPostProcessors.csrf())
                )
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.content().string("Plik jest pusty"));
    }

    @Test
    @WithMockUser(username = "user")
    void shouldDownloadFile() throws Exception {
        Resource resource = new ByteArrayResource("content".getBytes()) {
            @Override
            public String getFilename() {
                return "test.txt";
            }
        };

        Mockito.when(fileService.loadFileAsResource("test.txt")).thenReturn(resource);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/tasks/download/test.txt"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.header().string(
                        "Content-Disposition",
                        "attachment; filename=\"test.txt\""
                ))
                .andExpect(MockMvcResultMatchers.content().bytes("content".getBytes()));
    }

    @Test
    @WithMockUser(username = "user")
    void shouldDeleteAttachment() throws Exception {
        TaskResponse task = new TaskResponse();
        task.id = 1L;
        task.attachmentFilename = "test.txt";

        Mockito.when(taskService.getById(1L, "user")).thenReturn(task);

        mockMvc.perform(
                        MockMvcRequestBuilders.delete("/api/v1/tasks/1/attachment")
                                .with(SecurityMockMvcRequestPostProcessors.csrf())
                )
                .andExpect(MockMvcResultMatchers.status().isNoContent());

        Mockito.verify(fileService).deleteFile("test.txt");
        Mockito.verify(taskService).updateWithFile(1L, null, "user");
    }

    @Test
    @WithMockUser(username = "user")
    void shouldDeleteAttachmentWhenNoneExists() throws Exception {
        TaskResponse task = new TaskResponse();
        task.id = 1L;
        task.attachmentFilename = null;

        Mockito.when(taskService.getById(1L, "user")).thenReturn(task);

        mockMvc.perform(
                        MockMvcRequestBuilders.delete("/api/v1/tasks/1/attachment")
                                .with(SecurityMockMvcRequestPostProcessors.csrf())
                )
                .andExpect(MockMvcResultMatchers.status().isNoContent());

        Mockito.verify(fileService, Mockito.never()).deleteFile(Mockito.anyString());
    }
}
