package pl.taskmanager.taskmanager.dto;

import org.junit.jupiter.api.Test;
import pl.taskmanager.taskmanager.entity.Category;
import pl.taskmanager.taskmanager.entity.Task;
import pl.taskmanager.taskmanager.entity.TaskStatus;

import static org.assertj.core.api.Assertions.assertThat;

class TaskResponseTest {

    @Test
    void shouldCreateEmptyTaskResponse() {
        TaskResponse response = new TaskResponse();
        assertThat(response.id).isNull();
    }

    @Test
    void shouldHandleNullEntity() {
        TaskResponse response = new TaskResponse(null);
        assertThat(response.id).isNull();
    }

    @Test
    void shouldCreateTaskResponseFromEntityWithoutCategory() {
        Task task = new Task();
        task.setId(1L);
        task.setTitle("Test Task");
        task.setStatus(TaskStatus.TODO);

        TaskResponse response = new TaskResponse(task);

        assertThat(response.id).isEqualTo(1L);
        assertThat(response.title).isEqualTo("Test Task");
        assertThat(response.category).isNull();
    }

    @Test
    void shouldCreateTaskResponseFromEntityWithCategory() {
        Category category = new Category();
        category.setId(10L);
        category.setName("Work");

        Task task = new Task();
        task.setId(1L);
        task.setCategory(category);

        TaskResponse response = new TaskResponse(task);

        assertThat(response.id).isEqualTo(1L);
        assertThat(response.category).isNotNull();
        assertThat(response.category.id).isEqualTo(10L);
    }
}
