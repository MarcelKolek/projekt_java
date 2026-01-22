package pl.taskmanager.taskmanager.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import pl.taskmanager.taskmanager.entity.TaskStatus;

import java.time.LocalDate;

public class TaskRequest {

    @NotBlank
    @Size(max = 255)
    public String title;

    @Size(max = 2000)
    public String description;

    @NotNull
    public TaskStatus status;

    public LocalDate dueDate;

    public Long categoryId;
}
