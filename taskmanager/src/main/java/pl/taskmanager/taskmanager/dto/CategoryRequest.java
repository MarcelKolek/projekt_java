package pl.taskmanager.taskmanager.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CategoryRequest {

    @Schema(description = "Nazwa kategorii", example = "Studia")
    @NotBlank
    @Size(max = 255)
    public String name;

    @Schema(description = "Kolor kategorii (HEX)", example = "#0d6efd")
    @NotBlank
    @Size(max = 20)
    public String color;
}
