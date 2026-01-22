package pl.taskmanager.taskmanager.controller.view;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AppViewController.class)
@org.springframework.context.annotation.Import(pl.taskmanager.taskmanager.config.SecurityConfig.class)
class AppViewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private pl.taskmanager.taskmanager.service.UserService userService;

    @Test
    void shouldShowIndexPage() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"));
    }
}
