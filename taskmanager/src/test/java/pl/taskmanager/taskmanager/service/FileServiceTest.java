package pl.taskmanager.taskmanager.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;

import pl.taskmanager.taskmanager.exception.ResourceNotFoundException;

import static org.junit.jupiter.api.Assertions.*;

class FileServiceTest {

    private final FileService fileService = new FileService();
    private final Path uploadDir = Paths.get("uploads");

    @BeforeEach
    void setUp() throws IOException {
        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }
    }

    @AfterEach
    void tearDown() throws IOException {
        // optional cleanup hook (kept for symmetry / future use)
    }

    @Test
    void storeFile_Success() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.txt",
                "text/plain",
                "Hello World".getBytes()
        );

        String storedName = fileService.storeFile(file, 1L);

        assertEquals("1_test.txt", storedName);
        assertTrue(Files.exists(uploadDir.resolve(storedName)));

        Files.deleteIfExists(uploadDir.resolve(storedName));
    }

    @Test
    void loadFileAsResource_Success() throws IOException {
        String filename = "test_load.txt";
        Path path = uploadDir.resolve(filename);
        Files.write(path, "content".getBytes());

        Resource resource = fileService.loadFileAsResource(filename);

        assertNotNull(resource);
        assertTrue(resource.exists());

        Files.deleteIfExists(path);
    }

    @Test
    void loadFileAsResource_NotFound() {
        assertThrows(ResourceNotFoundException.class, () ->
                fileService.loadFileAsResource("non_existent.txt")
        );
    }

    @Test
    void deleteFile_Success() throws IOException {
        String filename = "test_delete.txt";
        Path path = uploadDir.resolve(filename);
        Files.write(path, "content".getBytes());

        fileService.deleteFile(filename);

        assertFalse(Files.exists(path));
    }
}
