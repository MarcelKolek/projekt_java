package pl.taskmanager.taskmanager.service;

class FileServiceTest {

    private final FileService fileService = new FileService();
    private final java.nio.file.Path uploadDir = java.nio.file.Paths.get("uploads");

    @org.junit.jupiter.api.BeforeEach
    void setUp() throws java.io.IOException {
        if (!java.nio.file.Files.exists(uploadDir)) {
            java.nio.file.Files.createDirectories(uploadDir);
        }
    }

    @org.junit.jupiter.api.AfterEach
    void tearDown() throws java.io.IOException {
    }

    @org.junit.jupiter.api.Test
    void storeFile_Success() throws java.io.IOException {
        org.springframework.mock.web.MockMultipartFile file = new org.springframework.mock.web.MockMultipartFile("file", "test.txt", "text/plain", "Hello World".getBytes());
        String storedName = fileService.storeFile(file, 1L);

        org.junit.jupiter.api.Assertions.assertEquals("1_test.txt", storedName);
        org.junit.jupiter.api.Assertions.assertTrue(java.nio.file.Files.exists(uploadDir.resolve(storedName)));
        
        java.nio.file.Files.deleteIfExists(uploadDir.resolve(storedName));
    }

    @org.junit.jupiter.api.Test
    void loadFileAsResource_Success() throws java.io.IOException {
        String filename = "test_load.txt";
        java.nio.file.Path path = uploadDir.resolve(filename);
        java.nio.file.Files.write(path, "content".getBytes());

        org.springframework.core.io.Resource resource = fileService.loadFileAsResource(filename);
        org.junit.jupiter.api.Assertions.assertNotNull(resource);
        org.junit.jupiter.api.Assertions.assertTrue(resource.exists());

        java.nio.file.Files.deleteIfExists(path);
    }

    @org.junit.jupiter.api.Test
    void loadFileAsResource_NotFound() {
        org.junit.jupiter.api.Assertions.assertThrows(pl.taskmanager.taskmanager.exception.ResourceNotFoundException.class, () -> {
            fileService.loadFileAsResource("non_existent.txt");
        });
    }

    @org.junit.jupiter.api.Test
    void deleteFile_Success() throws java.io.IOException {
        String filename = "test_delete.txt";
        java.nio.file.Path path = uploadDir.resolve(filename);
        java.nio.file.Files.write(path, "content".getBytes());

        fileService.deleteFile(filename);
        org.junit.jupiter.api.Assertions.assertFalse(java.nio.file.Files.exists(path));
    }
}
