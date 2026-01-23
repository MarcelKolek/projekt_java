package pl.taskmanager.taskmanager.controller.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import pl.taskmanager.taskmanager.dao.TaskStatsJdbcDao;
import pl.taskmanager.taskmanager.dto.TaskRequest;
import pl.taskmanager.taskmanager.dto.TaskResponse;
import pl.taskmanager.taskmanager.dto.TaskStatsResponse;
import pl.taskmanager.taskmanager.entity.TaskStatus;
import pl.taskmanager.taskmanager.exception.ResourceNotFoundException;
import pl.taskmanager.taskmanager.service.TaskService;
import pl.taskmanager.taskmanager.service.UserService;

import org.springframework.web.multipart.MultipartFile;

import com.lowagie.text.Document;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.pdf.PdfPTable;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

@Tag(name = "Tasks", description = "Operacje na zadaniach")
@RestController
@RequestMapping("/api/v1/tasks")
public class TaskApiController {

    private static final Logger log = LoggerFactory.getLogger(TaskApiController.class);

    private final TaskStatsJdbcDao taskStatsJdbcDao;
    private final UserService userService;
    private final TaskService taskService;

    public TaskApiController(TaskStatsJdbcDao taskStatsJdbcDao,
                             UserService userService,
                             TaskService taskService) {
        this.taskStatsJdbcDao = taskStatsJdbcDao;
        this.userService = userService;
        this.taskService = taskService;
    }

    @Operation(
            summary = "Lista zadań (paginacja + filtry + wyszukiwanie)",
            description = "Zwraca paginowaną listę zadań zalogowanego użytkownika. Obsługuje filtry: status, categoryId, dueBefore/dueAfter oraz wyszukiwanie po tytule (q). " +
                    "Sortowanie i paginacja przez standardowe parametry Spring: page, size, sort."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista zadań zwrócona poprawnie"),
            @ApiResponse(responseCode = "400", description = "Błędne parametry zapytania", content = @Content)
    })
    @GetMapping
    public ResponseEntity<Page<TaskResponse>> getAll(
            @Parameter(description = "Status zadania: TODO, IN_PROGRESS, DONE")
            @RequestParam(required = false) TaskStatus status,

            @Parameter(description = "ID kategorii")
            @RequestParam(required = false) Long categoryId,

            @Parameter(description = "Wyszukiwanie po tytule (LIKE, case-insensitive)")
            @RequestParam(required = false) String q,

            @Parameter(description = "Tylko zadania z dueDate < dueBefore (format yyyy-MM-dd)")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate dueBefore,

            @Parameter(description = "Tylko zadania z dueDate > dueAfter (format yyyy-MM-dd)")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate dueAfter,

            @Parameter(hidden = true)
            @PageableDefault(size = 10) Pageable pageable,

            @AuthenticationPrincipal UserDetails userDetails
    ) {
        log.debug("GET /api/v1/tasks username={}, status={}, categoryId={}, q={}", userDetails.getUsername(), status, categoryId, q);
        return ResponseEntity.ok(
                taskService.list(userDetails.getUsername(), status, categoryId, q, dueBefore, dueAfter, pageable)
        );
    }

    @Operation(summary = "Pobierz zadanie po ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Zadanie znalezione"),
            @ApiResponse(responseCode = "404", description = "Zadanie nie istnieje", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<TaskResponse> getById(
            @Parameter(description = "ID zadania", example = "1")
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        TaskResponse task = taskService.getById(id, userDetails.getUsername());
        return ResponseEntity.ok(task);
    }

    @Operation(summary = "Utwórz nowe zadanie")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Zadanie utworzone"),
            @ApiResponse(responseCode = "400", description = "Błąd walidacji / błędne dane", content = @Content),
            @ApiResponse(responseCode = "404", description = "Nie znaleziono kategorii (categoryId)", content = @Content)
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
            saved = taskService.updateWithFile(saved.id, handleFileUpload(file, saved.id), userDetails.getUsername());
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @Operation(summary = "Edytuj zadanie (lub zmień status)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Zadanie zaktualizowane"),
            @ApiResponse(responseCode = "400", description = "Błąd walidacji / błędne dane", content = @Content),
            @ApiResponse(responseCode = "404", description = "Zadanie lub kategoria nie istnieje", content = @Content)
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
            saved = taskService.updateWithFile(saved.id, handleFileUpload(file, saved.id), userDetails.getUsername());
        }

        return ResponseEntity.ok(saved);
    }

    @Operation(summary = "Usuń zadanie")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Zadanie usunięte"),
            @ApiResponse(responseCode = "404", description = "Zadanie nie istnieje", content = @Content)
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @Parameter(description = "ID zadania", example = "1")
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        taskService.delete(id, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Eksport wszystkich zadań do CSV", description = "Zwraca plik tasks.csv ze wszystkimi zadaniami zalogowanego użytkownika.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Plik CSV zwrócony poprawnie")
    })
    @GetMapping("/export/csv")
    public ResponseEntity<byte[]> exportCsv(@AuthenticationPrincipal UserDetails userDetails) {
        StringBuilder sb = new StringBuilder();
        sb.append("id,title,description,status,dueDate,categoryId,categoryName,createdAt,updatedAt\n");

        for (TaskResponse t : taskService.findAllByUser(userDetails.getUsername())) {
            sb.append(csv(t.id)).append(",");
            sb.append(csv(t.title)).append(",");
            sb.append(csv(t.description)).append(",");
            sb.append(csv(t.status != null ? t.status.name() : null)).append(",");
            sb.append(csv(t.dueDate != null ? t.dueDate.toString() : null)).append(",");

            Long catId = (t.category != null ? t.category.id : null);
            String catName = (t.category != null ? t.category.name : null);
            sb.append(csv(catId)).append(",");
            sb.append(csv(catName)).append(",");

            sb.append(csv(t.createdAt != null ? t.createdAt.toString() : null)).append(",");
            sb.append(csv(t.updatedAt != null ? t.updatedAt.toString() : null)).append("\n");
        }

        byte[] bytes = sb.toString().getBytes(StandardCharsets.UTF_8);

        return ResponseEntity.ok()
                .header("Content-Type", "text/csv; charset=utf-8")
                .header("Content-Disposition", "attachment; filename=\"tasks.csv\"")
                .body(bytes);
    }

    @Operation(summary = "Eksport wszystkich zadań do PDF", description = "Zwraca plik tasks.pdf ze wszystkimi zadaniami zalogowanego użytkownika.")
    @GetMapping("/export/pdf")
    public ResponseEntity<byte[]> exportPdf(@AuthenticationPrincipal UserDetails userDetails) throws IOException {
        var tasks = taskService.findAllByUser(userDetails.getUsername());

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document();
            PdfWriter.getInstance(document, out);
            document.open();
            document.add(new Paragraph("Lista Zadań - " + userDetails.getUsername()));
            document.add(new Paragraph(" "));

            PdfPTable table = new PdfPTable(4);
            table.addCell("ID");
            table.addCell("Tytuł");
            table.addCell("Status");
            table.addCell("Termin");

            for (TaskResponse t : tasks) {
                table.addCell(String.valueOf(t.id));
                table.addCell(t.title);
                table.addCell(t.status.name());
                table.addCell(t.dueDate != null ? t.dueDate.toString() : "");
            }

            document.add(table);
            document.close();

            return ResponseEntity.ok()
                    .header("Content-Type", "application/pdf")
                    .header("Content-Disposition", "attachment; filename=\"tasks.pdf\"")
                    .body(out.toByteArray());
        }
    }

    @Operation(summary = "Statystyki zadań (dashboard)", description = "Zwraca liczniki zalogowanego użytkownika: total/TODO/IN_PROGRESS/DONE oraz procent wykonania (DONE).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Statystyki zwrócone poprawnie")
    })
    @GetMapping("/stats")
    public ResponseEntity<TaskStatsResponse> stats(@AuthenticationPrincipal UserDetails userDetails) {
        var tasks = taskService.findAllByUser(userDetails.getUsername());

        long total = tasks.size();
        long todo = tasks.stream().filter(t -> t.status == TaskStatus.TODO).count();
        long inProgress = tasks.stream().filter(t -> t.status == TaskStatus.IN_PROGRESS).count();
        long done = tasks.stream().filter(t -> t.status == TaskStatus.DONE).count();

        double percentDone = (total == 0) ? 0.0 : (done * 100.0 / total);

        return ResponseEntity.ok(new TaskStatsResponse(total, todo, inProgress, done, percentDone));
    }

    @Operation(summary = "Statystyki zadań (dashboard) - JdbcTemplate",
            description = "Zwraca statystyki liczone przez DAO z JdbcTemplate dla zalogowanego użytkownika."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Statystyki zwrócone poprawnie")
    })
    @GetMapping("/stats/jdbc")
    public ResponseEntity<TaskStatsResponse> statsJdbc(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(taskStatsJdbcDao.getStats(userService.findIdByUsername(userDetails.getUsername())));
    }

    @Operation(summary = "Upload pliku", description = "Przykładowy upload pliku na serwer.")
    @PostMapping(value = "/upload", consumes = "multipart/form-data")
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file, @RequestParam("taskId") Long taskId, @AuthenticationPrincipal UserDetails userDetails) throws IOException {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("Plik jest pusty");
        }
        
        String storedFilename = handleFileUpload(file, taskId);
        taskService.updateWithFile(taskId, storedFilename, userDetails.getUsername());
        
        return ResponseEntity.ok("Plik zapisany: " + storedFilename);
    }

    private String handleFileUpload(MultipartFile file, Long taskId) throws IOException {
        Path uploadDir = Paths.get("uploads");
        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }

        String originalFilename = file.getOriginalFilename();
        String storedFilename = taskId + "_" + originalFilename;
        Path destination = uploadDir.resolve(storedFilename);

        Files.copy(file.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);
        return storedFilename;
    }

    @Operation(summary = "Pobierz plik", description = "Pobiera wcześniej wgrany plik z serwera.")
    @GetMapping("/download/{filename}")
    public ResponseEntity<org.springframework.core.io.Resource> downloadFile(@PathVariable String filename) throws IOException {
        Path filePath = Paths.get("uploads").resolve(filename);
        if (!Files.exists(filePath)) {
            throw new ResourceNotFoundException("Plik nie istnieje");
        }
        org.springframework.core.io.Resource resource = new org.springframework.core.io.UrlResource(filePath.toUri());
        return ResponseEntity.ok()
                .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

    @Operation(summary = "Usuń plik", description = "Usuwa załącznik z zadania.")
    @DeleteMapping("/{id}/attachment")
    public ResponseEntity<Void> deleteAttachment(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) throws IOException {
        TaskResponse task = taskService.getById(id, userDetails.getUsername());
        if (task.attachmentFilename != null) {
            Path filePath = Paths.get("uploads").resolve(task.attachmentFilename);
            Files.deleteIfExists(filePath);
            taskService.updateWithFile(id, null, userDetails.getUsername());
        }
        return ResponseEntity.noContent().build();
    }

    private String csv(Object value) {
        if (value == null) return "";
        String s = String.valueOf(value).replace("\"", "\"\"");
        return "\"" + s + "\"";
    }
}
