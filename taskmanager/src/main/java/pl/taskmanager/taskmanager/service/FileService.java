package pl.taskmanager.taskmanager.service;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import pl.taskmanager.taskmanager.exception.ResourceNotFoundException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Service
public class FileService {

    private final Path uploadDir = Paths.get("uploads");

    public String storeFile(MultipartFile file, Long taskId) throws IOException {
        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }

        String originalFilename = file.getOriginalFilename();
        String storedFilename = taskId + "_" + originalFilename;
        Path destination = uploadDir.resolve(storedFilename);

        Files.copy(
                file.getInputStream(),
                destination,
                StandardCopyOption.REPLACE_EXISTING
        );

        return storedFilename;
    }

    public Resource loadFileAsResource(String filename) throws IOException {
        Path filePath = uploadDir.resolve(filename);

        if (!Files.exists(filePath)) {
            throw new ResourceNotFoundException("Plik nie istnieje");
        }

        return new UrlResource(filePath.toUri());
    }

    public void deleteFile(String filename) throws IOException {
        Path filePath = uploadDir.resolve(filename);
        Files.deleteIfExists(filePath);
    }
}
