package pl.taskmanager.taskmanager.controller.view;

import org.junit.jupiter.api.Test;

import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

import org.springframework.context.annotation.Import;

import org.springframework.data.domain.Page;

import org.springframework.security.test.context.support.WithMockUser;

import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import pl.taskmanager.taskmanager.config.SecurityConfig;

import pl.taskmanager.taskmanager.service.CategoryService;
import pl.taskmanager.taskmanager.service.TaskService;
import pl.taskmanager.taskmanager.service.UserService;

import java.util.List;

@WebMvcTest(AppViewController.class)
@Import(SecurityConfig.class)
class AppViewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private TaskService taskService;

    @MockitoBean
    private CategoryService categoryService;

    @Test
    @WithMockUser(username = "testuser")
    void shouldShowIndexPage() throws Exception {
        Mockito.when(categoryService.getAll("testuser")).thenReturn(List.of());

        Mockito.when(taskService.list(
                ArgumentMatchers.eq("testuser"),
                ArgumentMatchers.any(),
                ArgumentMatchers.any(),
                ArgumentMatchers.any(),
                ArgumentMatchers.any(),
                ArgumentMatchers.any(),
                ArgumentMatchers.any()
        )).thenReturn(Page.empty());

        mockMvc.perform(MockMvcRequestBuilders.get("/"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("index"))
                .andExpect(MockMvcResultMatchers.model().attributeExists("newTask"))
                .andExpect(MockMvcResultMatchers.model().attributeExists("categories"))
                .andExpect(MockMvcResultMatchers.model().attributeExists("tasks"));
    }

    @Test
    void shouldRedirectAnonymousUserToLogin() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/"))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.redirectedUrlPattern("**/login"));
    }
}
