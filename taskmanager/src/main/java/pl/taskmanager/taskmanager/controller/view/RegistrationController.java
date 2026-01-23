package pl.taskmanager.taskmanager.controller.view;

@org.springframework.stereotype.Controller
public class RegistrationController {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(RegistrationController.class);

    private final pl.taskmanager.taskmanager.service.UserService userService;

    public RegistrationController(pl.taskmanager.taskmanager.service.UserService userService) {
        this.userService = userService;
    }

    @org.springframework.web.bind.annotation.GetMapping("/register")
    public String showRegistrationForm(org.springframework.ui.Model model) {
        log.debug("Showing registration form");
        model.addAttribute("registerRequest", new pl.taskmanager.taskmanager.dto.RegisterRequest());
        return "register";
    }

    @org.springframework.web.bind.annotation.PostMapping("/register")
    public String registerUser(@jakarta.validation.Valid @org.springframework.web.bind.annotation.ModelAttribute("registerRequest") pl.taskmanager.taskmanager.dto.RegisterRequest registerRequest,
                               org.springframework.validation.BindingResult bindingResult,
                               org.springframework.ui.Model model) {
        log.info("Attempting to register user: {}", registerRequest.getUsername());
        if (bindingResult.hasErrors()) {
            log.warn("Registration validation failed for user: {}", registerRequest.getUsername());
            return "register";
        }

        try {
            userService.register(registerRequest);
            log.info("User {} registered successfully", registerRequest.getUsername());
        } catch (java.lang.IllegalArgumentException e) {
            log.error("Registration failed for user {}: {}", registerRequest.getUsername(), e.getMessage());
            model.addAttribute("registrationError", e.getMessage());
            return "register";
        }

        return "redirect:/login?registered";
    }
}
