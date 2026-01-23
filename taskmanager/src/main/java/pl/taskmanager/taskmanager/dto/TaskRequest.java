package pl.taskmanager.taskmanager.dto;

public class TaskRequest {

    @io.swagger.v3.oas.annotations.media.Schema(description = "Tytuł zadania", example = "Zrobić projekt")
    @jakarta.validation.constraints.NotBlank
    @jakarta.validation.constraints.Size(max = 255)
    public java.lang.String title;

    @io.swagger.v3.oas.annotations.media.Schema(description = "Opis zadania", example = "Dokończyć backend i UI")
    @jakarta.validation.constraints.Size(max = 2000)
    public java.lang.String description;

    @io.swagger.v3.oas.annotations.media.Schema(description = "Status zadania", example = "TODO")
    @jakarta.validation.constraints.NotNull
    public pl.taskmanager.taskmanager.entity.TaskStatus status;

    @io.swagger.v3.oas.annotations.media.Schema(description = "Deadline (yyyy-MM-dd)", example = "2026-02-01")
    public java.time.LocalDate dueDate;

    @io.swagger.v3.oas.annotations.media.Schema(description = "ID kategorii (może być null)", example = "1")
    public java.lang.Long categoryId;
}
