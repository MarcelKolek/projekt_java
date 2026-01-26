package pl.taskmanager.taskmanager.service;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

import pl.taskmanager.taskmanager.dto.CategoryResponse;
import pl.taskmanager.taskmanager.dto.TaskResponse;
import pl.taskmanager.taskmanager.entity.TaskStatus;

import static org.junit.jupiter.api.Assertions.assertTrue;

class CsvServiceTest {

    private final CsvService csvService = new CsvService();

    @Test
    void exportTasksToCsv_EmptyList() {
        byte[] result = csvService.exportTasksToCsv(Collections.emptyList());
        String csv = new String(result, StandardCharsets.UTF_8);

        assertTrue(csv.contains(
                "id,title,description,status,dueDate,categoryId,categoryName,createdAt,updatedAt"
        ));
    }

    @Test
    void exportTasksToCsv_WithData() {
        TaskResponse task = new TaskResponse();
        task.id = 1L;
        task.title = "Test Task";
        task.description = "Description \"with quotes\"";
        task.status = TaskStatus.TODO;
        task.dueDate = LocalDate.of(2023, 10, 27);
        task.createdAt = LocalDateTime.of(2023, 10, 27, 10, 0);
        task.updatedAt = LocalDateTime.of(2023, 10, 27, 11, 0);

        CategoryResponse category = new CategoryResponse();
        category.id = 2L;
        category.name = "Work";
        task.category = category;

        byte[] result = csvService.exportTasksToCsv(List.of(task));
        String csv = new String(result, StandardCharsets.UTF_8);

        assertTrue(csv.contains("\"1\""));
        assertTrue(csv.contains("\"Test Task\""));
        assertTrue(csv.contains("\"Description \"\"with quotes\"\"\""));
        assertTrue(csv.contains("\"TODO\""));
        assertTrue(csv.contains("\"2023-10-27\""));
        assertTrue(csv.contains("\"2\""));
        assertTrue(csv.contains("\"Work\""));
    }

    @Test
    void exportTasksToCsv_WithNulls() {
        TaskResponse task = new TaskResponse();
        task.id = 1L;
        task.title = "Test Task";
        task.status = null;
        task.dueDate = null;
        task.category = null;
        task.createdAt = null;
        task.updatedAt = null;

        byte[] result = csvService.exportTasksToCsv(List.of(task));
        String csv = new String(result, StandardCharsets.UTF_8);

        assertTrue(csv.contains("\"1\""));
        assertTrue(csv.contains("\"Test Task\""));
    }
}
