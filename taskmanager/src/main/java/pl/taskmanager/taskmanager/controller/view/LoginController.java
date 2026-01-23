package pl.taskmanager.taskmanager.controller.view;

@org.springframework.stereotype.Controller
public class LoginController {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LoginController.class);

    @org.springframework.web.bind.annotation.GetMapping("/login")
    public String login() {
        log.debug("Accessing login page");
        return "login";
    }
}
