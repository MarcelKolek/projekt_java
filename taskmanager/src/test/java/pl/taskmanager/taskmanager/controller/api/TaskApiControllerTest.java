package pl.taskmanager.taskmanager.controller.api;

@org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest(pl.taskmanager.taskmanager.controller.api.TaskApiController.class)
@org.springframework.context.annotation.Import(pl.taskmanager.taskmanager.config.SecurityConfig.class)
class TaskApiControllerTest {

    @org.springframework.beans.factory.annotation.Autowired
    private org.springframework.test.web.servlet.MockMvc mockMvc;

    @org.springframework.test.context.bean.override.mockito.MockitoBean
    private pl.taskmanager.taskmanager.service.UserService userService;

    @org.springframework.test.context.bean.override.mockito.MockitoBean
    private pl.taskmanager.taskmanager.service.TaskService taskService;

    @org.springframework.test.context.bean.override.mockito.MockitoBean
    private pl.taskmanager.taskmanager.service.CategoryService categoryService;

    @org.springframework.test.context.bean.override.mockito.MockitoBean
    private pl.taskmanager.taskmanager.service.CsvService csvService;

    @org.springframework.test.context.bean.override.mockito.MockitoBean
    private pl.taskmanager.taskmanager.service.PdfService pdfService;

    @org.springframework.test.context.bean.override.mockito.MockitoBean
    private pl.taskmanager.taskmanager.service.FileService fileService;

    @org.springframework.beans.factory.annotation.Autowired
    private com.fasterxml.jackson.databind.ObjectMapper objectMapper;

    @org.junit.jupiter.api.Test
    @org.springframework.security.test.context.support.WithMockUser(username = "user")
    void shouldGetAllTasks() throws java.lang.Exception {
        org.mockito.Mockito.when(taskService.list(
                        org.mockito.Mockito.eq("user"),
                        org.mockito.ArgumentMatchers.any(),
                        org.mockito.ArgumentMatchers.any(),
                        org.mockito.ArgumentMatchers.any(),
                        org.mockito.ArgumentMatchers.any(),
                        org.mockito.ArgumentMatchers.any(),
                        org.mockito.ArgumentMatchers.any()
                ))
                .thenReturn(org.springframework.data.domain.Page.empty());

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/v1/tasks"))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk());
    }

    @org.junit.jupiter.api.Test
    @org.springframework.security.test.context.support.WithMockUser(username = "user")
    void shouldCreateTask() throws java.lang.Exception {
        pl.taskmanager.taskmanager.dto.TaskRequest req = new pl.taskmanager.taskmanager.dto.TaskRequest();
        req.title = "New Task";
        req.status = pl.taskmanager.taskmanager.entity.TaskStatus.TODO;

        pl.taskmanager.taskmanager.dto.TaskResponse saved = new pl.taskmanager.taskmanager.dto.TaskResponse();
        saved.id = 1L;
        saved.title = "New Task";

        org.mockito.Mockito.when(taskService.create(
                        org.mockito.ArgumentMatchers.any(pl.taskmanager.taskmanager.dto.TaskRequest.class),
                        org.mockito.Mockito.eq("user")
                ))
                .thenReturn(saved);

        org.springframework.mock.web.MockMultipartFile taskPart =
                new org.springframework.mock.web.MockMultipartFile(
                        "task",
                        "",
                        "application/json",
                        objectMapper.writeValueAsString(req).getBytes()
                );

        mockMvc.perform(
                        org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart("/api/v1/tasks")
                                .file(taskPart)
                                .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf())
                )
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isCreated())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.title").value("New Task"));
    }

    @org.junit.jupiter.api.Test
    @org.springframework.security.test.context.support.WithMockUser(username = "user")
    void shouldCreateTaskWithFile() throws java.lang.Exception {
        pl.taskmanager.taskmanager.dto.TaskRequest req = new pl.taskmanager.taskmanager.dto.TaskRequest();
        req.title = "With File";
        req.status = pl.taskmanager.taskmanager.entity.TaskStatus.TODO;

        pl.taskmanager.taskmanager.dto.TaskResponse saved = new pl.taskmanager.taskmanager.dto.TaskResponse();
        saved.id = 10L;
        saved.title = "With File";

        org.mockito.Mockito.when(taskService.create(
                        org.mockito.ArgumentMatchers.any(pl.taskmanager.taskmanager.dto.TaskRequest.class),
                        org.mockito.Mockito.eq("user")
                ))
                .thenReturn(saved);

        org.mockito.Mockito.when(taskService.updateWithFile(
                        org.mockito.Mockito.eq(10L),
                        org.mockito.ArgumentMatchers.any(),
                        org.mockito.Mockito.eq("user")
                ))
                .thenReturn(saved);

        org.springframework.mock.web.MockMultipartFile taskPart =
                new org.springframework.mock.web.MockMultipartFile(
                        "task",
                        "",
                        "application/json",
                        objectMapper.writeValueAsString(req).getBytes()
                );

        org.springframework.mock.web.MockMultipartFile filePart =
                new org.springframework.mock.web.MockMultipartFile(
                        "file",
                        "test.txt",
                        "text/plain",
                        "hello".getBytes()
                );

        mockMvc.perform(
                        org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart("/api/v1/tasks")
                                .file(taskPart)
                                .file(filePart)
                                .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf())
                )
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isCreated())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.title").value("With File"));
    }

    @org.junit.jupiter.api.Test
    @org.springframework.security.test.context.support.WithMockUser(username = "user")
    void shouldGetTaskById() throws java.lang.Exception {
        pl.taskmanager.taskmanager.dto.TaskResponse task = new pl.taskmanager.taskmanager.dto.TaskResponse();
        task.id = 1L;
        task.title = "Found";

        org.mockito.Mockito.when(taskService.getById(1L, "user")).thenReturn(task);

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/v1/tasks/1"))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.title").value("Found"));
    }

    @org.junit.jupiter.api.Test
    @org.springframework.security.test.context.support.WithMockUser(username = "user")
    void shouldDeleteTask() throws java.lang.Exception {
        mockMvc.perform(
                        org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete("/api/v1/tasks/1")
                                .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf())
                )
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isNoContent());
    }

    @org.junit.jupiter.api.Test
    void shouldDenyAnonymous() throws java.lang.Exception {
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/v1/tasks"))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isFound());
    }

    @org.junit.jupiter.api.Test
    @org.springframework.security.test.context.support.WithMockUser(username = "user")
    void shouldHandleNotFoundInUpdate() throws java.lang.Exception {
        org.mockito.Mockito.when(taskService.update(
                        org.mockito.Mockito.eq(999L),
                        org.mockito.ArgumentMatchers.any(),
                        org.mockito.Mockito.eq("user")
                ))
                .thenThrow(new pl.taskmanager.taskmanager.exception.ResourceNotFoundException("Task not found"));

        pl.taskmanager.taskmanager.dto.TaskRequest req = new pl.taskmanager.taskmanager.dto.TaskRequest();
        req.title = "Update";
        req.status = pl.taskmanager.taskmanager.entity.TaskStatus.TODO;

        org.springframework.mock.web.MockMultipartFile taskPart =
                new org.springframework.mock.web.MockMultipartFile(
                        "task",
                        "",
                        "application/json",
                        objectMapper.writeValueAsString(req).getBytes()
                );

        mockMvc.perform(
                        org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart("/api/v1/tasks/999")
                                .file(taskPart)
                                .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf())
                                .with(request -> { request.setMethod("PUT"); return request; })
                )
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isNotFound());
    }

    @org.junit.jupiter.api.Test
    @org.springframework.security.test.context.support.WithMockUser(username = "user")
    void shouldExportCsv() throws java.lang.Exception {
        org.mockito.Mockito.when(taskService.findAllByUser("user")).thenReturn(java.util.List.of());

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/v1/tasks/export/csv"))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.header().string("Content-Type", "text/csv; charset=utf-8"));
    }

    @org.junit.jupiter.api.Test
    @org.springframework.security.test.context.support.WithMockUser(username = "user")
    void shouldExportPdf() throws java.lang.Exception {
        org.mockito.Mockito.when(taskService.findAllByUser("user")).thenReturn(java.util.List.of());

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/v1/tasks/export/pdf"))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.header().string("Content-Type", "application/pdf"));
    }

    @org.junit.jupiter.api.Test
    @org.springframework.security.test.context.support.WithMockUser(username = "user")
    void shouldStats() throws java.lang.Exception {
        org.mockito.Mockito.when(taskService.getStats("user"))
                .thenReturn(new pl.taskmanager.taskmanager.dto.TaskStatsResponse(0, 0, 0, 0, 0.0));

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/v1/tasks/stats"))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.total").value(0));
    }

    @org.junit.jupiter.api.Test
    @org.springframework.security.test.context.support.WithMockUser(username = "user")
    void shouldUploadFile() throws java.lang.Exception {
        org.springframework.mock.web.MockMultipartFile file =
                new org.springframework.mock.web.MockMultipartFile("file", "test.txt", "text/plain", "content".getBytes());

        pl.taskmanager.taskmanager.dto.TaskResponse task = new pl.taskmanager.taskmanager.dto.TaskResponse();
        task.id = 1L;
        task.attachmentFilename = "1_test.txt";

        org.mockito.Mockito.when(taskService.updateWithFile(
                        org.mockito.Mockito.eq(1L),
                        org.mockito.ArgumentMatchers.any(),
                        org.mockito.Mockito.eq("user")
                ))
                .thenReturn(task);

        mockMvc.perform(
                        org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart("/api/v1/tasks/upload")
                                .file(file)
                                .param("taskId", "1")
                                .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf())
                )
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.content().string(
                        org.hamcrest.Matchers.containsString("Plik zapisany")
                ));
    }
}
