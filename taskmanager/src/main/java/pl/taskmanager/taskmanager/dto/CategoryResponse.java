package pl.taskmanager.taskmanager.dto;

public class CategoryResponse {
    public Long id;
    public String name;
    public String color;

    public CategoryResponse() {}

    public CategoryResponse(pl.taskmanager.taskmanager.entity.Category category) {
        this.id = category.getId();
        this.name = category.getName();
        this.color = category.getColor();
    }

    public CategoryResponse(Long id, String name, String color) {
        this.id = id;
        this.name = name;
        this.color = color;
    }
}
