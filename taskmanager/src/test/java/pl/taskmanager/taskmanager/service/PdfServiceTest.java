package pl.taskmanager.taskmanager.service;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

import pl.taskmanager.taskmanager.dto.TaskResponse;
import pl.taskmanager.taskmanager.entity.TaskStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PdfServiceTest {

    private final PdfService pdfService = new PdfService();

    @Test
    void exportTasksToPdf_Success() throws IOException {
        TaskResponse task = new TaskResponse();
        task.id = 1L;
        task.title = "Test Task";
        task.status = TaskStatus.TODO;
        task.dueDate = LocalDate.now();

        byte[] result = pdfService.exportTasksToPdf(List.of(task), "testuser");

        assertNotNull(result);
        assertTrue(result.length > 0);
        assertEquals("%PDF", new String(result, 0, 4));
    }

    @Test
    void exportTasksToPdf_WithNullDate() throws IOException {
        TaskResponse task = new TaskResponse();
        task.id = 1L;
        task.title = "Test Task";
        task.status = TaskStatus.IN_PROGRESS;
        task.dueDate = null;

        byte[] result = pdfService.exportTasksToPdf(List.of(task), "testuser");

        assertNotNull(result);
        assertTrue(result.length > 0);
        assertEquals("%PDF", new String(result, 0, 4));
    }
}
