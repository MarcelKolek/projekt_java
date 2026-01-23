package pl.taskmanager.taskmanager.service;

@org.springframework.stereotype.Service
public class FileService {

    private final java.nio.file.Path uploadDir = java.nio.file.Paths.get("uploads");

    public String storeFile(org.springframework.web.multipart.MultipartFile file, Long taskId) throws java.io.IOException {
        if (!java.nio.file.Files.exists(uploadDir)) {
            java.nio.file.Files.createDirectories(uploadDir);
        }

        String originalFilename = file.getOriginalFilename();
        String storedFilename = taskId + "_" + originalFilename;
        java.nio.file.Path destination = uploadDir.resolve(storedFilename);

        java.nio.file.Files.copy(file.getInputStream(), destination, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        return storedFilename;
    }

    public org.springframework.core.io.Resource loadFileAsResource(String filename) throws java.io.IOException {
        java.nio.file.Path filePath = uploadDir.resolve(filename);
        if (!java.nio.file.Files.exists(filePath)) {
            throw new pl.taskmanager.taskmanager.exception.ResourceNotFoundException("Plik nie istnieje");
        }
        return new org.springframework.core.io.UrlResource(filePath.toUri());
    }

    public void deleteFile(String filename) throws java.io.IOException {
        java.nio.file.Path filePath = uploadDir.resolve(filename);
        java.nio.file.Files.deleteIfExists(filePath);
    }
}
