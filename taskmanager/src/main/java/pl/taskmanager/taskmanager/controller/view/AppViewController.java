package pl.taskmanager.taskmanager.controller.view;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import pl.taskmanager.taskmanager.service.CategoryService;
import pl.taskmanager.taskmanager.service.TaskService;
import java.util.Collections;

@Controller
public class AppViewController {

    private static final Logger log = LoggerFactory.getLogger(AppViewController.class);

    private final TaskService taskService;
    private final CategoryService categoryService;

    public AppViewController(TaskService taskService, CategoryService categoryService) {
        this.taskService = taskService;
        this.categoryService = categoryService;
    }

    @GetMapping("/")
    public String home(
            @AuthenticationPrincipal UserDetails userDetails,
            Model model
    ) {
        if (userDetails != null) {
            log.debug("Accessing home page for user={}", userDetails.getUsername());
            model.addAttribute("categories", categoryService.getAll(userDetails.getUsername()));

            org.springframework.data.domain.Page<pl.taskmanager.taskmanager.dto.TaskResponse> tasksPage =
                taskService.list(userDetails.getUsername(), null, null, null, null, null,
                org.springframework.data.domain.PageRequest.of(0, 10, org.springframework.data.domain.Sort.by("dueDate").ascending()));
            model.addAttribute("tasks", tasksPage.getContent());
        } else {
            model.addAttribute("categories", Collections.emptyList());
            model.addAttribute("tasks", Collections.emptyList());
        }
        model.addAttribute("newTask", new pl.taskmanager.taskmanager.dto.TaskRequest());
        return "index";
    }
}
