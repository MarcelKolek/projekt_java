package pl.taskmanager.taskmanager.controller.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;

import org.springframework.format.annotation.DateTimeFormat;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.web.multipart.MultipartFile;

import pl.taskmanager.taskmanager.dto.TaskRequest;
import pl.taskmanager.taskmanager.dto.TaskResponse;
import pl.taskmanager.taskmanager.dto.TaskStatsResponse;

import pl.taskmanager.taskmanager.entity.TaskStatus;

import pl.taskmanager.taskmanager.service.CsvService;
import pl.taskmanager.taskmanager.service.FileService;
import pl.taskmanager.taskmanager.service.PdfService;
import pl.taskmanager.taskmanager.service.TaskService;

import java.io.IOException;
import java.time.LocalDate;

@Tag(name = "Tasks", description = "Operacje na zadaniach")
@RestController
@RequestMapping("/api/v1/tasks")
public class TaskApiController {

    private static final Logger log = LoggerFactory.getLogger(TaskApiController.class);

    private final TaskService taskService;
    private final CsvService csvService;
    private final PdfService pdfService;
    private final FileService fileService;

    public TaskApiController(
            TaskService taskService,
            CsvService csvService,
            PdfService pdfService,
            FileService fileService
    ) {
        this.taskService = taskService;
        this.csvService = csvService;
        this.pdfService = pdfService;
        this.fileService = fileService;
    }

    @Operation(
            summary = "Lista zadań (paginacja + filtry + wyszukiwanie)",
            description = "Zwraca paginowaną listę zadań zalogowanego użytkownika. Obsługuje filtry: status, categoryId, " +
                    "dueBefore/dueAfter oraz wyszukiwanie po tytule (q). Sortowanie i paginacja przez standardowe " +
                    "parametry Spring: page, size, sort."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista zadań zwrócona poprawnie"),
            @ApiResponse(responseCode = "400", description = "Błędne parametry zapytania", content = @io.swagger.v3.oas.annotations.media.Content)
    })
    @GetMapping
    public ResponseEntity<Page<TaskResponse>> getAll(
            @Parameter(description = "Status zadania: TODO, IN_PROGRESS, DONE")
            @RequestParam(required = false) TaskStatus status,

            @Parameter(description = "ID kategorii")
            @RequestParam(required = false) Long categoryId,

            @Parameter(description = "Wyszukiwanie po tytule (LIKE, case-insensitive)")
            @RequestParam(required = false) String q,

            @Parameter(description = "Tylko zadania z dueDate < dueBefore (yyyy-MM-dd)")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate dueBefore,

            @Parameter(description = "Tylko zadania z dueDate > dueAfter (yyyy-MM-dd)")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate dueAfter,

            @Parameter(hidden = true)
            @PageableDefault(size = 10) Pageable pageable,

            @AuthenticationPrincipal UserDetails userDetails
    ) {
        log.debug("GET /api/v1/tasks username={}, status={}, categoryId={}, q={}",
                userDetails.getUsername(), status, categoryId, q);

        return ResponseEntity.ok(
                taskService.list(userDetails.getUsername(), status, categoryId, q, dueBefore, dueAfter, pageable)
        );
    }

    @Operation(summary = "Pobierz zadanie po ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Zadanie znalezione"),
            @ApiResponse(responseCode = "404", description = "Zadanie nie istnieje", content = @io.swagger.v3.oas.annotations.media.Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<TaskResponse> getById(
            @Parameter(description = "ID zadania", example = "1")
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(taskService.getById(id, userDetails.getUsername()));
    }

    @Operation(summary = "Utwórz nowe zadanie")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Zadanie utworzone"),
            @ApiResponse(responseCode = "400", description = "Błąd walidacji / błędne dane", content = @io.swagger.v3.oas.annotations.media.Content),
            @ApiResponse(responseCode = "404", description = "Nie znaleziono kategorii", content = @io.swagger.v3.oas.annotations.media.Content)
    })
    @PostMapping(consumes = {"application/json", "multipart/form-data"})
    public ResponseEntity<TaskResponse> create(
            @Valid @RequestPart("task") TaskRequest req,
            @RequestPart(value = "file", required = false) MultipartFile file,
            @AuthenticationPrincipal UserDetails userDetails
    ) throws IOException {

        log.info("Creating new task: {} for user: {}", req.title, userDetails.getUsername());

        TaskResponse saved = taskService.create(req, userDetails.getUsername());

        if (file != null && !file.isEmpty()) {
            saved = taskService.updateWithFile(
                    saved.id,
                    fileService.storeFile(file, saved.id),
                    userDetails.getUsername()
            );
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @Operation(summary = "Edytuj zadanie (lub zmień status)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Zadanie zaktualizowane"),
            @ApiResponse(responseCode = "400", description = "Błąd walidacji / błędne dane", content = @io.swagger.v3.oas.annotations.media.Content),
            @ApiResponse(responseCode = "404", description = "Zadanie lub kategoria nie istnieje", content = @io.swagger.v3.oas.annotations.media.Content)
    })
    @PutMapping(value = "/{id}", consumes = {"application/json", "multipart/form-data"})
    public ResponseEntity<TaskResponse> update(
            @Parameter(description = "ID zadania", example = "1")
            @PathVariable Long id,
            @Valid @RequestPart("task") TaskRequest req,
            @RequestPart(value = "file", required = false) MultipartFile file,
            @AuthenticationPrincipal UserDetails userDetails
    ) throws IOException {

        TaskResponse saved = taskService.update(id, req, userDetails.getUsername());

        if (file != null && !file.isEmpty()) {
            saved = taskService.updateWithFile(
                    saved.id,
                    fileService.storeFile(file, saved.id),
                    userDetails.getUsername()
            );
        }

        return ResponseEntity.ok(saved);
    }

    @Operation(summary = "Usuń zadanie")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Zadanie usunięte"),
            @ApiResponse(responseCode = "404", description = "Zadanie nie istnieje", content = @io.swagger.v3.oas.annotations.media.Content)
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        taskService.delete(id, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Eksport wszystkich zadań do CSV")
    @GetMapping("/export/csv")
    public ResponseEntity<byte[]> exportCsv(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        byte[] bytes = csvService.exportTasksToCsv(
                taskService.findAllByUser(userDetails.getUsername())
        );

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, "text/csv; charset=utf-8")
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"tasks.csv\"")
                .body(bytes);
    }

    @Operation(summary = "Eksport wszystkich zadań do PDF")
    @GetMapping("/export/pdf")
    public ResponseEntity<byte[]> exportPdf(
            @AuthenticationPrincipal UserDetails userDetails
    ) throws IOException {

        byte[] bytes = pdfService.exportTasksToPdf(
                taskService.findAllByUser(userDetails.getUsername()),
                userDetails.getUsername()
        );

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, "application/pdf")
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"tasks.pdf\"")
                .body(bytes);
    }

    @Operation(
            summary = "Statystyki zadań (dashboard)",
            description = "Zwraca liczniki: total/TODO/IN_PROGRESS/DONE oraz procent wykonania."
    )
    @GetMapping("/stats")
    public ResponseEntity<TaskStatsResponse> stats(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(taskService.getStats(userDetails.getUsername()));
    }

    @Operation(summary = "Upload pliku")
    @PostMapping(value = "/upload", consumes = "multipart/form-data")
    public ResponseEntity<String> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("taskId") Long taskId,
            @AuthenticationPrincipal UserDetails userDetails
    ) throws IOException {

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("Plik jest pusty");
        }

        String storedFilename = fileService.storeFile(file, taskId);
        taskService.updateWithFile(taskId, storedFilename, userDetails.getUsername());

        return ResponseEntity.ok("Plik zapisany: " + storedFilename);
    }

    @Operation(summary = "Pobierz plik")
    @GetMapping("/download/{filename}")
    public ResponseEntity<Resource> downloadFile(
            @PathVariable String filename
    ) throws IOException {

        Resource resource = fileService.loadFileAsResource(filename);

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + resource.getFilename() + "\""
                )
                .body(resource);
    }

    @Operation(summary = "Usuń plik")
    @DeleteMapping("/{id}/attachment")
    public ResponseEntity<Void> deleteAttachment(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails
    ) throws IOException {

        TaskResponse task = taskService.getById(id, userDetails.getUsername());

        if (task.attachmentFilename != null) {
            fileService.deleteFile(task.attachmentFilename);
            taskService.updateWithFile(id, null, userDetails.getUsername());
        }

        return ResponseEntity.noContent().build();
    }
}
