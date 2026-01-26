package pl.taskmanager.taskmanager.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class CategoryRequest {

    @Schema(description = "Nazwa kategorii", example = "Studia")
    @NotBlank(message = "name nie może być puste")
    @Size(max = 255, message = "name max 255 znaków")
    public String name;

    @Schema(description = "Kolor kategorii (HEX #RRGGBB)", example = "#0d6efd")
    @NotBlank(message = "color nie może być puste")
    @Size(max = 7, message = "color musi mieć max 7 znaków (#RRGGBB)")
    @Pattern(
            regexp = "^#[0-9a-fA-F]{6}$",
            message = "color musi być w formacie HEX #RRGGBB, np. #0d6efd"
    )
    public String color;
}
