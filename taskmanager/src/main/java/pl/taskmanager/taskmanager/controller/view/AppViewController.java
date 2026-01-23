package pl.taskmanager.taskmanager.controller.view;

@org.springframework.stereotype.Controller
public class AppViewController {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AppViewController.class);

    private final pl.taskmanager.taskmanager.service.TaskService taskService;
    private final pl.taskmanager.taskmanager.service.CategoryService categoryService;

    public AppViewController(pl.taskmanager.taskmanager.service.TaskService taskService, pl.taskmanager.taskmanager.service.CategoryService categoryService) {
        this.taskService = taskService;
        this.categoryService = categoryService;
    }

    @org.springframework.web.bind.annotation.GetMapping("/")
    public String home(
            @org.springframework.security.core.annotation.AuthenticationPrincipal org.springframework.security.core.userdetails.UserDetails userDetails,
            org.springframework.ui.Model model
    ) {
        if (userDetails != null) {
            log.debug("Accessing home page for user={}", userDetails.getUsername());
            model.addAttribute("categories", categoryService.getAll(userDetails.getUsername()));

            org.springframework.data.domain.Page<pl.taskmanager.taskmanager.dto.TaskResponse> tasksPage =
                taskService.list(userDetails.getUsername(), null, null, null, null, null,
                org.springframework.data.domain.PageRequest.of(0, 10, org.springframework.data.domain.Sort.by("dueDate").ascending()));
            model.addAttribute("tasks", tasksPage.getContent());
        } else {
            model.addAttribute("categories", java.util.Collections.emptyList());
            model.addAttribute("tasks", java.util.Collections.emptyList());
        }
        model.addAttribute("newTask", new pl.taskmanager.taskmanager.dto.TaskRequest());
        return "index";
    }
}
