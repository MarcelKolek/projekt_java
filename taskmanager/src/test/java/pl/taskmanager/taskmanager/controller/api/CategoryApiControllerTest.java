package pl.taskmanager.taskmanager.controller.api;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;

import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

import org.springframework.context.annotation.Import;

import org.springframework.http.MediaType;

import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;

import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import pl.taskmanager.taskmanager.config.SecurityConfig;

import pl.taskmanager.taskmanager.dto.CategoryRequest;
import pl.taskmanager.taskmanager.dto.CategoryResponse;

import pl.taskmanager.taskmanager.service.CategoryService;
import pl.taskmanager.taskmanager.service.UserService;

import java.util.List;

@WebMvcTest(CategoryApiController.class)
@Import(SecurityConfig.class)
class CategoryApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CategoryService categoryService;

    @MockitoBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(username = "user")
    void shouldGetAllCategories() throws Exception {
        CategoryResponse resp = new CategoryResponse();
        resp.id = 1L;
        resp.name = "Work";
        resp.color = "#ff0000";

        Mockito.when(categoryService.getAll("user")).thenReturn(List.of(resp));

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/categories"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].name").value("Work"));
    }

    @Test
    @WithMockUser(username = "user")
    void shouldCreateCategory() throws Exception {
        CategoryRequest req = new CategoryRequest();
        req.name = "New Cat";
        req.color = "#00ff00";

        CategoryResponse saved = new CategoryResponse();
        saved.id = 2L;
        saved.name = "New Cat";
        saved.color = "#00ff00";

        Mockito.when(categoryService.create(ArgumentMatchers.any(CategoryRequest.class), Mockito.eq("user")))
                .thenReturn(saved);

        mockMvc.perform(
                        MockMvcRequestBuilders.post("/api/v1/categories")
                                .with(SecurityMockMvcRequestPostProcessors.csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(req))
                )
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value("New Cat"));
    }

    @Test
    @WithMockUser(username = "user")
    void shouldDeleteCategory() throws Exception {
        mockMvc.perform(
                        MockMvcRequestBuilders.delete("/api/v1/categories/1")
                                .with(SecurityMockMvcRequestPostProcessors.csrf())
                )
                .andExpect(MockMvcResultMatchers.status().isNoContent());
    }

    @Test
    @WithMockUser(username = "user")
    void shouldGetCategoryById() throws Exception {
        CategoryResponse resp = new CategoryResponse();
        resp.id = 1L;
        resp.name = "Work";
        resp.color = "#ff0000";

        Mockito.when(categoryService.getById(1L, "user")).thenReturn(resp);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/categories/1"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value("Work"));
    }

    @Test
    @WithMockUser(username = "user")
    void shouldUpdateCategory() throws Exception {
        CategoryRequest req = new CategoryRequest();
        req.name = "Updated Cat";
        req.color = "#0000ff";

        CategoryResponse saved = new CategoryResponse();
        saved.id = 1L;
        saved.name = "Updated Cat";
        saved.color = "#0000ff";

        Mockito.when(categoryService.update(Mockito.eq(1L), ArgumentMatchers.any(CategoryRequest.class), Mockito.eq("user")))
                .thenReturn(saved);

        mockMvc.perform(
                        MockMvcRequestBuilders.put("/api/v1/categories/1")
                                .with(SecurityMockMvcRequestPostProcessors.csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(req))
                )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value("Updated Cat"));
    }
}
