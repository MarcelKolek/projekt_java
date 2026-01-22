package pl.taskmanager.taskmanager.controller.view;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AppViewController {

    @GetMapping("/")
    public String home() {
        return "index";
    }
}
