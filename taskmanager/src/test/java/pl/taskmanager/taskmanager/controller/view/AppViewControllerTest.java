package pl.taskmanager.taskmanager.controller.view;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AppViewController.class)
@org.springframework.context.annotation.Import(pl.taskmanager.taskmanager.config.SecurityConfig.class)
class AppViewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private pl.taskmanager.taskmanager.service.UserService userService;

    @MockitoBean
    private pl.taskmanager.taskmanager.service.TaskService taskService;

    @MockitoBean
    private pl.taskmanager.taskmanager.service.CategoryService categoryService;

    @Test
    @org.springframework.security.test.context.support.WithMockUser(username = "testuser")
    void shouldShowIndexPage() throws Exception {
        org.mockito.Mockito.when(categoryService.getAll("testuser")).thenReturn(java.util.List.of());
        org.mockito.Mockito.when(taskService.list(org.mockito.ArgumentMatchers.eq("testuser"), any(), any(), any(), any(), any(), any()))
                .thenReturn(org.springframework.data.domain.Page.empty());

        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attributeExists("newTask"))
                .andExpect(model().attributeExists("categories"))
                .andExpect(model().attributeExists("tasks"));
    }
}
