package pl.taskmanager.taskmanager.service;

class CsvServiceTest {

    private final CsvService csvService = new CsvService();

    @org.junit.jupiter.api.Test
    void exportTasksToCsv_EmptyList() {
        byte[] result = csvService.exportTasksToCsv(java.util.Collections.emptyList());
        String csv = new String(result, java.nio.charset.StandardCharsets.UTF_8);
        org.junit.jupiter.api.Assertions.assertTrue(csv.contains("id,title,description,status,dueDate,categoryId,categoryName,createdAt,updatedAt"));
    }

    @org.junit.jupiter.api.Test
    void exportTasksToCsv_WithData() {
        pl.taskmanager.taskmanager.dto.TaskResponse task = new pl.taskmanager.taskmanager.dto.TaskResponse();
        task.id = 1L;
        task.title = "Test Task";
        task.description = "Description \"with quotes\"";
        task.status = pl.taskmanager.taskmanager.entity.TaskStatus.TODO;
        task.dueDate = java.time.LocalDate.of(2023, 10, 27);
        task.createdAt = java.time.LocalDateTime.of(2023, 10, 27, 10, 0);
        task.updatedAt = java.time.LocalDateTime.of(2023, 10, 27, 11, 0);

        pl.taskmanager.taskmanager.dto.CategoryResponse category = new pl.taskmanager.taskmanager.dto.CategoryResponse();
        category.id = 2L;
        category.name = "Work";
        task.category = category;

        byte[] result = csvService.exportTasksToCsv(java.util.List.of(task));
        String csv = new String(result, java.nio.charset.StandardCharsets.UTF_8);

        org.junit.jupiter.api.Assertions.assertTrue(csv.contains("\"1\""));
        org.junit.jupiter.api.Assertions.assertTrue(csv.contains("\"Test Task\""));
        org.junit.jupiter.api.Assertions.assertTrue(csv.contains("\"Description \"\"with quotes\"\"\""));
        org.junit.jupiter.api.Assertions.assertTrue(csv.contains("\"TODO\""));
        org.junit.jupiter.api.Assertions.assertTrue(csv.contains("\"2023-10-27\""));
        org.junit.jupiter.api.Assertions.assertTrue(csv.contains("\"2\""));
        org.junit.jupiter.api.Assertions.assertTrue(csv.contains("\"Work\""));
    }

    @org.junit.jupiter.api.Test
    void exportTasksToCsv_WithNulls() {
        pl.taskmanager.taskmanager.dto.TaskResponse task = new pl.taskmanager.taskmanager.dto.TaskResponse();
        task.id = 1L;
        task.title = "Test Task";
        task.status = null;
        task.dueDate = null;
        task.category = null;
        task.createdAt = null;
        task.updatedAt = null;

        byte[] result = csvService.exportTasksToCsv(java.util.List.of(task));
        String csv = new String(result, java.nio.charset.StandardCharsets.UTF_8);

        org.junit.jupiter.api.Assertions.assertTrue(csv.contains("\"1\""));
        org.junit.jupiter.api.Assertions.assertTrue(csv.contains("\"Test Task\""));
    }
}
