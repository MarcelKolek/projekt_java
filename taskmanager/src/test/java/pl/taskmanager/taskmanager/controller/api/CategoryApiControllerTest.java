package pl.taskmanager.taskmanager.controller.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import pl.taskmanager.taskmanager.dto.CategoryRequest;
import pl.taskmanager.taskmanager.dto.CategoryResponse;
import pl.taskmanager.taskmanager.service.CategoryService;
import pl.taskmanager.taskmanager.service.UserService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CategoryApiController.class)
@org.springframework.context.annotation.Import(pl.taskmanager.taskmanager.config.SecurityConfig.class)
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
        
        when(categoryService.getAll("user")).thenReturn(List.of(resp));

        mockMvc.perform(get("/api/v1/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Work"));
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
        
        when(categoryService.create(any(CategoryRequest.class), eq("user"))).thenReturn(saved);

        mockMvc.perform(post("/api/v1/categories")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("New Cat"));
    }

    @Test
    @WithMockUser(username = "user")
    void shouldDeleteCategory() throws Exception {
        mockMvc.perform(delete("/api/v1/categories/1").with(csrf()))
                .andExpect(status().isNoContent());
    }
}
