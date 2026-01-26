package pl.taskmanager.taskmanager.controller.view;

import jakarta.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import pl.taskmanager.taskmanager.dto.RegisterRequest;
import pl.taskmanager.taskmanager.service.UserService;

@Controller
public class RegistrationController {

    private static final Logger log = LoggerFactory.getLogger(RegistrationController.class);

    private final UserService userService;

    public RegistrationController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        log.debug("Showing registration form");
        model.addAttribute("registerRequest", new RegisterRequest());
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(
            @Valid @ModelAttribute("registerRequest") RegisterRequest registerRequest,
            BindingResult bindingResult,
            Model model
    ) {
        log.info("Attempting to register user: {}", registerRequest.getUsername());

        if (bindingResult.hasErrors()) {
            log.warn("Registration validation failed for user: {}", registerRequest.getUsername());
            return "register";
        }

        try {
            userService.register(registerRequest);
            log.info("User {} registered successfully", registerRequest.getUsername());
        } catch (IllegalArgumentException e) {
            log.error("Registration failed for user {}: {}", registerRequest.getUsername(), e.getMessage());
            model.addAttribute("registrationError", e.getMessage());
            return "register";
        }

        return "redirect:/login?registered";
    }
}
