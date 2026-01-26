package pl.taskmanager.taskmanager.controller.view;

@org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest(AppViewController.class)
@org.springframework.context.annotation.Import(pl.taskmanager.taskmanager.config.SecurityConfig.class)
class AppViewControllerTest {

    @org.springframework.beans.factory.annotation.Autowired
    private org.springframework.test.web.servlet.MockMvc mockMvc;

    @org.springframework.test.context.bean.override.mockito.MockitoBean
    private pl.taskmanager.taskmanager.service.UserService userService;

    @org.springframework.test.context.bean.override.mockito.MockitoBean
    private pl.taskmanager.taskmanager.service.TaskService taskService;

    @org.springframework.test.context.bean.override.mockito.MockitoBean
    private pl.taskmanager.taskmanager.service.CategoryService categoryService;

    @org.junit.jupiter.api.Test
    @org.springframework.security.test.context.support.WithMockUser(username = "testuser")
    void shouldShowIndexPage() throws Exception {
        org.mockito.Mockito.when(categoryService.getAll("testuser")).thenReturn(java.util.List.of());
        org.mockito.Mockito.when(
                taskService.list(
                        org.mockito.ArgumentMatchers.eq("testuser"),
                        org.mockito.ArgumentMatchers.any(),
                        org.mockito.ArgumentMatchers.any(),
                        org.mockito.ArgumentMatchers.any(),
                        org.mockito.ArgumentMatchers.any(),
                        org.mockito.ArgumentMatchers.any(),
                        org.mockito.ArgumentMatchers.any()
                )
        ).thenReturn(org.springframework.data.domain.Page.empty());

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/"))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.view().name("index"))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.model().attributeExists("newTask"))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.model().attributeExists("categories"))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.model().attributeExists("tasks"));
    }

    @org.junit.jupiter.api.Test
    void shouldRedirectAnonymousUserToLogin() throws Exception {
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/"))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().is3xxRedirection())
                // safer than redirectedUrl("/login") because of context path / params
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern("**/login"));
    }
}
