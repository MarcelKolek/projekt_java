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
import pl.taskmanager.taskmanager.dto.TaskStatsResponse;
import pl.taskmanager.taskmanager.entity.Category;
import pl.taskmanager.taskmanager.entity.Task;
import pl.taskmanager.taskmanager.entity.TaskStatus;
import pl.taskmanager.taskmanager.entity.User;
import pl.taskmanager.taskmanager.exception.ResourceNotFoundException;
import pl.taskmanager.taskmanager.repository.CategoryRepository;
import pl.taskmanager.taskmanager.repository.TaskRepository;
import pl.taskmanager.taskmanager.service.UserService;

import org.springframework.web.multipart.MultipartFile;
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

    private final TaskRepository taskRepository;
    private final CategoryRepository categoryRepository;
    private final TaskStatsJdbcDao taskStatsJdbcDao;
    private final UserService userService;
    private final pl.taskmanager.taskmanager.service.TaskService taskService;

    public TaskApiController(TaskRepository taskRepository,
                             CategoryRepository categoryRepository,
                             TaskStatsJdbcDao taskStatsJdbcDao,
                             UserService userService,
                             pl.taskmanager.taskmanager.service.TaskService taskService) {
        this.taskRepository = taskRepository;
        this.categoryRepository = categoryRepository;
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
    public ResponseEntity<Page<Task>> getAll(
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
    public ResponseEntity<Task> getById(
            @Parameter(description = "ID zadania", example = "1")
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        Task task = taskService.getById(id, userDetails.getUsername());
        return ResponseEntity.ok(task);
    }

    @Operation(summary = "Utwórz nowe zadanie")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Zadanie utworzone"),
            @ApiResponse(responseCode = "400", description = "Błąd walidacji / błędne dane", content = @Content),
            @ApiResponse(responseCode = "404", description = "Nie znaleziono kategorii (categoryId)", content = @Content)
    })
    @PostMapping
    public ResponseEntity<Task> create(
            @Valid @RequestBody TaskRequest req,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        log.info("Creating new task: {} for user: {}", req.title, userDetails.getUsername());
        User user = userService.findByUsername(userDetails.getUsername());
        Task task = new Task();
        task.setUser(user);
        applyRequest(task, req, user);
        Task saved = taskService.save(task);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @Operation(summary = "Edytuj zadanie (lub zmień status)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Zadanie zaktualizowane"),
            @ApiResponse(responseCode = "400", description = "Błąd walidacji / błędne dane", content = @Content),
            @ApiResponse(responseCode = "404", description = "Zadanie lub kategoria nie istnieje", content = @Content)
    })
    @PutMapping("/{id}")
    public ResponseEntity<Task> update(
            @Parameter(description = "ID zadania", example = "1")
            @PathVariable Long id,
            @Valid @RequestBody TaskRequest req,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task id=" + id + " not found"));
        if (!task.getUser().getUsername().equals(userDetails.getUsername())) {
            throw new ResourceNotFoundException("Task id=" + id + " not found");
        }
        User user = task.getUser();
        applyRequest(task, req, user);
        Task saved = taskService.save(task);
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

        User user = userService.findByUsername(userDetails.getUsername());
        for (Task t : taskRepository.findAllByUser(user)) {
            sb.append(csv(t.getId())).append(",");
            sb.append(csv(t.getTitle())).append(",");
            sb.append(csv(t.getDescription())).append(",");
            sb.append(csv(t.getStatus() != null ? t.getStatus().name() : null)).append(",");
            sb.append(csv(t.getDueDate() != null ? t.getDueDate().toString() : null)).append(",");

            Long catId = (t.getCategory() != null ? t.getCategory().getId() : null);
            String catName = (t.getCategory() != null ? t.getCategory().getName() : null);
            sb.append(csv(catId)).append(",");
            sb.append(csv(catName)).append(",");

            sb.append(csv(t.getCreatedAt() != null ? t.getCreatedAt().toString() : null)).append(",");
            sb.append(csv(t.getUpdatedAt() != null ? t.getUpdatedAt().toString() : null)).append("\n");
        }

        byte[] bytes = sb.toString().getBytes(StandardCharsets.UTF_8);

        return ResponseEntity.ok()
                .header("Content-Type", "text/csv; charset=utf-8")
                .header("Content-Disposition", "attachment; filename=\"tasks.csv\"")
                .body(bytes);
    }

    @Operation(summary = "Statystyki zadań (dashboard)", description = "Zwraca liczniki zalogowanego użytkownika: total/TODO/IN_PROGRESS/DONE oraz procent wykonania (DONE).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Statystyki zwrócone poprawnie")
    })
    @GetMapping("/stats")
    public ResponseEntity<TaskStatsResponse> stats(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.findByUsername(userDetails.getUsername());
        var tasks = taskRepository.findAllByUser(user);

        long total = tasks.size();
        long todo = tasks.stream().filter(t -> t.getStatus() == TaskStatus.TODO).count();
        long inProgress = tasks.stream().filter(t -> t.getStatus() == TaskStatus.IN_PROGRESS).count();
        long done = tasks.stream().filter(t -> t.getStatus() == TaskStatus.DONE).count();

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
        User user = userService.findByUsername(userDetails.getUsername());
        return ResponseEntity.ok(taskStatsJdbcDao.getStats(user.getId()));
    }

    @Operation(summary = "Upload pliku", description = "Przykładowy upload pliku na serwer.")
    @PostMapping(value = "/upload", consumes = "multipart/form-data")
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("Plik jest pusty");
        }
        Path uploadDir = Paths.get("uploads");
        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }
        Path destination = uploadDir.resolve(file.getOriginalFilename());
        Files.copy(file.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);
        return ResponseEntity.ok("Plik zapisany: " + destination.toString());
    }

    private void applyRequest(Task task, TaskRequest req, User user) {
        task.setTitle(req.title);
        task.setDescription(req.description);
        if (req.status != null) task.setStatus(req.status);
        task.setDueDate(req.dueDate);

        if (req.categoryId == null) {
            task.setCategory(null);
        } else {
            Category cat = categoryRepository.findById(req.categoryId)
                    .orElseThrow(() -> new ResourceNotFoundException("Category id=" + req.categoryId + " not found"));
            
            // Check if category belongs to user
            if (!cat.getUser().getId().equals(user.getId())) {
                throw new ResourceNotFoundException("Category id=" + req.categoryId + " not found");
            }
            
            task.setCategory(cat);
        }
    }

    private String csv(Object value) {
        if (value == null) return "";
        String s = String.valueOf(value).replace("\"", "\"\"");
        return "\"" + s + "\"";
    }
}
