package pl.taskmanager.taskmanager.service;

class PdfServiceTest {

    private final PdfService pdfService = new PdfService();

    @org.junit.jupiter.api.Test
    void exportTasksToPdf_Success() throws java.io.IOException {
        pl.taskmanager.taskmanager.dto.TaskResponse task = new pl.taskmanager.taskmanager.dto.TaskResponse();
        task.id = 1L;
        task.title = "Test Task";
        task.status = pl.taskmanager.taskmanager.entity.TaskStatus.TODO;
        task.dueDate = java.time.LocalDate.now();

        byte[] result = pdfService.exportTasksToPdf(java.util.List.of(task), "testuser");

        org.junit.jupiter.api.Assertions.assertNotNull(result);
        org.junit.jupiter.api.Assertions.assertTrue(result.length > 0);
        org.junit.jupiter.api.Assertions.assertEquals("%PDF", new String(result, 0, 4));
    }

    @org.junit.jupiter.api.Test
    void exportTasksToPdf_WithNullDate() throws java.io.IOException {
        pl.taskmanager.taskmanager.dto.TaskResponse task = new pl.taskmanager.taskmanager.dto.TaskResponse();
        task.id = 1L;
        task.title = "Test Task";
        task.status = pl.taskmanager.taskmanager.entity.TaskStatus.IN_PROGRESS;
        task.dueDate = null;

        byte[] result = pdfService.exportTasksToPdf(java.util.List.of(task), "testuser");

        org.junit.jupiter.api.Assertions.assertNotNull(result);
        org.junit.jupiter.api.Assertions.assertTrue(result.length > 0);
        org.junit.jupiter.api.Assertions.assertEquals("%PDF", new String(result, 0, 4));
    }
}
