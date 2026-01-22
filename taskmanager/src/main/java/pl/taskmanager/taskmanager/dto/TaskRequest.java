package pl.taskmanager.taskmanager.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import pl.taskmanager.taskmanager.entity.TaskStatus;

import java.time.LocalDate;

public class TaskRequest {

    @Schema(description = "Tytuł zadania", example = "Zrobić projekt")
    @NotBlank
    @Size(max = 255)
    public String title;

    @Schema(description = "Opis zadania", example = "Dokończyć backend i UI")
    @Size(max = 2000)
    public String description;

    @Schema(description = "Status zadania", example = "TODO")
    @NotNull
    public TaskStatus status;

    @Schema(description = "Deadline (yyyy-MM-dd)", example = "2026-02-01")
    public LocalDate dueDate;

    @Schema(description = "ID kategorii (może być null)", example = "1")
    public Long categoryId;
}
