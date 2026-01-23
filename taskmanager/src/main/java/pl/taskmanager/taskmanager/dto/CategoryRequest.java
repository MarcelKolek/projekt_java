package pl.taskmanager.taskmanager.dto;

public class CategoryRequest {
    @io.swagger.v3.oas.annotations.media.Schema(description = "Nazwa kategorii", example = "Studia")
    @jakarta.validation.constraints.NotBlank(message = "name nie może być puste")
    @jakarta.validation.constraints.Size(max = 255, message = "name max 255 znaków")
    public java.lang.String name;

    @io.swagger.v3.oas.annotations.media.Schema(description = "Kolor kategorii (HEX #RRGGBB)", example = "#0d6efd")
    @jakarta.validation.constraints.NotBlank(message = "color nie może być puste")
    @jakarta.validation.constraints.Size(max = 7, message = "color musi mieć max 7 znaków (#RRGGBB)")
    @jakarta.validation.constraints.Pattern(
            regexp = "^#[0-9a-fA-F]{6}$",
            message = "color musi być w formacie HEX #RRGGBB, np. #0d6efd"
    )
    public java.lang.String color;
}
