package pl.taskmanager.taskmanager.controller.view;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import pl.taskmanager.taskmanager.config.SecurityConfig;
import pl.taskmanager.taskmanager.service.UserService;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RegistrationController.class)
@Import(SecurityConfig.class)
class RegistrationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @Test
    void shouldShowRegistrationForm() throws Exception {
        mockMvc.perform(get("/register"))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().attributeExists("registerRequest"));
    }

    @Test
    void shouldRegisterUserSuccessfully() throws Exception {
        mockMvc.perform(post("/register")
                        .with(csrf())
                        .param("username", "newuser")
                        .param("password", "password")
                        .param("email", "test@test.com"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?registered"));
    }

    @Test
    void shouldShowErrorOnInvalidInput() throws Exception {
        mockMvc.perform(post("/register")
                        .with(csrf())
                        .param("username", "us")
                        .param("password", "123"))
                .andExpect(status().isOk())
                .andExpect(view().name("register"));
    }
}
