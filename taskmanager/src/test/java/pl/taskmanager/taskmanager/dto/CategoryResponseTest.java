package pl.taskmanager.taskmanager.dto;

import org.junit.jupiter.api.Test;
import pl.taskmanager.taskmanager.entity.Category;

import static org.assertj.core.api.Assertions.assertThat;

class CategoryResponseTest {

    @Test
    void shouldCreateEmptyCategoryResponse() {
        CategoryResponse response = new CategoryResponse();
        assertThat(response.id).isNull();
        assertThat(response.name).isNull();
        assertThat(response.color).isNull();
    }

    @Test
    void shouldCreateCategoryResponseFromEntity() {
        Category category = new Category();
        category.setId(1L);
        category.setName("Work");
        category.setColor("blue");

        CategoryResponse response = new CategoryResponse(category);

        assertThat(response.id).isEqualTo(1L);
        assertThat(response.name).isEqualTo("Work");
        assertThat(response.color).isEqualTo("blue");
    }

    @Test
    void shouldCreateCategoryResponseFromFields() {
        CategoryResponse response = new CategoryResponse(2L, "Home", "red");

        assertThat(response.id).isEqualTo(2L);
        assertThat(response.name).isEqualTo("Home");
        assertThat(response.color).isEqualTo("red");
    }
}
