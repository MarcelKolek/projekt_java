package pl.taskmanager.taskmanager.controller.api;

@org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest(CategoryApiController.class)
@org.springframework.context.annotation.Import(pl.taskmanager.taskmanager.config.SecurityConfig.class)
class CategoryApiControllerTest {

    @org.springframework.beans.factory.annotation.Autowired
    private org.springframework.test.web.servlet.MockMvc mockMvc;

    @org.springframework.test.context.bean.override.mockito.MockitoBean
    private pl.taskmanager.taskmanager.service.CategoryService categoryService;

    @org.springframework.test.context.bean.override.mockito.MockitoBean
    private pl.taskmanager.taskmanager.service.UserService userService;

    @org.springframework.beans.factory.annotation.Autowired
    private com.fasterxml.jackson.databind.ObjectMapper objectMapper;

    @org.junit.jupiter.api.Test
    @org.springframework.security.test.context.support.WithMockUser(username = "user")
    void shouldGetAllCategories() throws Exception {
        pl.taskmanager.taskmanager.dto.CategoryResponse resp = new pl.taskmanager.taskmanager.dto.CategoryResponse();
        resp.id = 1L;
        resp.name = "Work";
        resp.color = "#ff0000";
        
        org.mockito.Mockito.when(categoryService.getAll("user")).thenReturn(java.util.List.of(resp));

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/v1/categories"))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$[0].name").value("Work"));
    }

    @org.junit.jupiter.api.Test
    @org.springframework.security.test.context.support.WithMockUser(username = "user")
    void shouldCreateCategory() throws Exception {
        pl.taskmanager.taskmanager.dto.CategoryRequest req = new pl.taskmanager.taskmanager.dto.CategoryRequest();
        req.name = "New Cat";
        req.color = "#00ff00";

        pl.taskmanager.taskmanager.dto.CategoryResponse saved = new pl.taskmanager.taskmanager.dto.CategoryResponse();
        saved.id = 2L;
        saved.name = "New Cat";
        saved.color = "#00ff00";
        
        org.mockito.Mockito.when(categoryService.create(org.mockito.ArgumentMatchers.any(pl.taskmanager.taskmanager.dto.CategoryRequest.class), org.mockito.Mockito.eq("user"))).thenReturn(saved);

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/v1/categories")
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isCreated())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.name").value("New Cat"));
    }

    @org.junit.jupiter.api.Test
    @org.springframework.security.test.context.support.WithMockUser(username = "user")
    void shouldDeleteCategory() throws Exception {
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete("/api/v1/categories/1").with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isNoContent());
    }
    @org.junit.jupiter.api.Test
    @org.springframework.security.test.context.support.WithMockUser(username = "user")
    void shouldGetCategoryById() throws Exception {
        pl.taskmanager.taskmanager.dto.CategoryResponse resp = new pl.taskmanager.taskmanager.dto.CategoryResponse();
        resp.id = 1L;
        resp.name = "Work";
        resp.color = "#ff0000";

        org.mockito.Mockito.when(categoryService.getById(1L, "user")).thenReturn(resp);

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/v1/categories/1"))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.name").value("Work"));
    }

    @org.junit.jupiter.api.Test
    @org.springframework.security.test.context.support.WithMockUser(username = "user")
    void shouldUpdateCategory() throws Exception {
        pl.taskmanager.taskmanager.dto.CategoryRequest req = new pl.taskmanager.taskmanager.dto.CategoryRequest();
        req.name = "Updated Cat";
        req.color = "#0000ff";

        pl.taskmanager.taskmanager.dto.CategoryResponse saved = new pl.taskmanager.taskmanager.dto.CategoryResponse();
        saved.id = 1L;
        saved.name = "Updated Cat";
        saved.color = "#0000ff";

        org.mockito.Mockito.when(categoryService.update(org.mockito.Mockito.eq(1L), org.mockito.ArgumentMatchers.any(pl.taskmanager.taskmanager.dto.CategoryRequest.class), org.mockito.Mockito.eq("user"))).thenReturn(saved);

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/api/v1/categories/1")
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.name").value("Updated Cat"));
    }
}
