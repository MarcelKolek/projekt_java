package pl.taskmanager.taskmanager.controller.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.taskmanager.taskmanager.dto.TaskRequest;
import pl.taskmanager.taskmanager.dto.TaskStatsResponse;
import pl.taskmanager.taskmanager.entity.Category;
import pl.taskmanager.taskmanager.entity.Task;
import pl.taskmanager.taskmanager.entity.TaskStatus;
import pl.taskmanager.taskmanager.exception.ResourceNotFoundException;
import pl.taskmanager.taskmanager.repository.CategoryRepository;
import pl.taskmanager.taskmanager.repository.TaskRepository;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

@Tag(name = "Tasks", description = "Operacje na zadaniach")
@RestController
@RequestMapping("/api/v1/tasks")
public class TaskApiController {

    private final TaskRepository taskRepository;
    private final CategoryRepository categoryRepository;

    public TaskApiController(TaskRepository taskRepository, CategoryRepository categoryRepository) {
        this.taskRepository = taskRepository;
        this.categoryRepository = categoryRepository;
    }

    @Operation(
            summary = "Lista zadań (paginacja + filtry + wyszukiwanie)",
            description = "Zwraca paginowaną listę zadań. Obsługuje filtry: status, categoryId, dueBefore/dueAfter oraz wyszukiwanie po tytule (q). " +
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
            @PageableDefault(size = 10) Pageable pageable
    ) {
        return ResponseEntity.ok(
                taskRepository.search(status, categoryId, q, dueBefore, dueAfter, pageable)
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
            @PathVariable Long id
    ) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task id=" + id + " not found"));
        return ResponseEntity.ok(task);
    }

    @Operation(summary = "Utwórz nowe zadanie")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Zadanie utworzone"),
            @ApiResponse(responseCode = "400", description = "Błąd walidacji / błędne dane", content = @Content),
            @ApiResponse(responseCode = "404", description = "Nie znaleziono kategorii (categoryId)", content = @Content)
    })
    @PostMapping
    public ResponseEntity<Task> create(@Valid @RequestBody TaskRequest req) {
        Task task = new Task();
        applyRequest(task, req);
        Task saved = taskRepository.save(task);
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
            @Valid @RequestBody TaskRequest req
    ) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task id=" + id + " not found"));
        applyRequest(task, req);
        Task saved = taskRepository.save(task);
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
            @PathVariable Long id
    ) {
        if (!taskRepository.existsById(id)) {
            throw new ResourceNotFoundException("Task id=" + id + " not found");
        }
        taskRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Eksport wszystkich zadań do CSV", description = "Zwraca plik tasks.csv ze wszystkimi zadaniami.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Plik CSV zwrócony poprawnie")
    })
    @GetMapping("/export/csv")
    public ResponseEntity<byte[]> exportCsv() {
        StringBuilder sb = new StringBuilder();
        sb.append("id,title,description,status,dueDate,categoryId,categoryName,createdAt,updatedAt\n");

        for (Task t : taskRepository.findAll()) {
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

    @Operation(summary = "Statystyki zadań (dashboard)", description = "Zwraca liczniki: total/TODO/IN_PROGRESS/DONE oraz procent wykonania (DONE).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Statystyki zwrócone poprawnie")
    })
    @GetMapping("/stats")
    public ResponseEntity<TaskStatsResponse> stats() {
        var tasks = taskRepository.findAll();

        long total = tasks.size();
        long todo = tasks.stream().filter(t -> t.getStatus() == TaskStatus.TODO).count();
        long inProgress = tasks.stream().filter(t -> t.getStatus() == TaskStatus.IN_PROGRESS).count();
        long done = tasks.stream().filter(t -> t.getStatus() == TaskStatus.DONE).count();

        double percentDone = (total == 0) ? 0.0 : (done * 100.0 / total);

        return ResponseEntity.ok(new TaskStatsResponse(total, todo, inProgress, done, percentDone));
    }

    private void applyRequest(Task task, TaskRequest req) {
        task.setTitle(req.title);
        task.setDescription(req.description);
        if (req.status != null) task.setStatus(req.status);
        task.setDueDate(req.dueDate);

        if (req.categoryId == null) {
            task.setCategory(null);
        } else {
            Category cat = categoryRepository.findById(req.categoryId)
                    .orElseThrow(() -> new ResourceNotFoundException("Category id=" + req.categoryId + " not found"));
            task.setCategory(cat);
        }
    }

    private String csv(Object value) {
        if (value == null) return "";
        String s = String.valueOf(value).replace("\"", "\"\"");
        return "\"" + s + "\"";
    }
}
