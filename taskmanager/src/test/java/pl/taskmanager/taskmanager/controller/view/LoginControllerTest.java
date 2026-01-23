package pl.taskmanager.taskmanager.controller.view;

@org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest(LoginController.class)
@org.springframework.context.annotation.Import(pl.taskmanager.taskmanager.config.SecurityConfig.class)
class LoginControllerTest {

    @org.springframework.beans.factory.annotation.Autowired
    private org.springframework.test.web.servlet.MockMvc mockMvc;

    @org.springframework.test.context.bean.override.mockito.MockitoBean
    private pl.taskmanager.taskmanager.service.UserService userService;

    @org.junit.jupiter.api.Test
    void shouldShowLoginForm() throws Exception {
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/login"))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.view().name("login"));
    }
}
