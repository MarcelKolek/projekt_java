package pl.taskmanager.taskmanager.dto;

public class CategoryResponse {
    public java.lang.Long id;
    public java.lang.String name;
    public java.lang.String color;

    public CategoryResponse() {}

    public CategoryResponse(pl.taskmanager.taskmanager.entity.Category category) {
        this.id = category.getId();
        this.name = category.getName();
        this.color = category.getColor();
    }

    public CategoryResponse(java.lang.Long id, java.lang.String name, java.lang.String color) {
        this.id = id;
        this.name = name;
        this.color = color;
    }
}
