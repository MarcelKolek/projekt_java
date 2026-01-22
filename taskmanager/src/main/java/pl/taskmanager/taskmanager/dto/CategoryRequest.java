package pl.taskmanager.taskmanager.dto;

import jakarta.validation.constraints.NotBlank;

public class CategoryRequest {

    @NotBlank
    public String name;

    @NotBlank
    public String color;
}
