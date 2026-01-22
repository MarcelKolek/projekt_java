package pl.taskmanager.taskmanager.controller.api;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.taskmanager.taskmanager.dto.TaskRequest;
import pl.taskmanager.taskmanager.entity.Category;
import pl.taskmanager.taskmanager.entity.Task;
import pl.taskmanager.taskmanager.entity.TaskStatus;
import pl.taskmanager.taskmanager.exception.ResourceNotFoundException;
import pl.taskmanager.taskmanager.repository.CategoryRepository;
import pl.taskmanager.taskmanager.repository.TaskRepository;

import jakarta.validation.Valid;


import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/tasks")
public class TaskApiController {

    private final TaskRepository taskRepository;
    private final CategoryRepository categoryRepository;

    public TaskApiController(TaskRepository taskRepository, CategoryRepository categoryRepository) {
        this.taskRepository = taskRepository;
        this.categoryRepository = categoryRepository;
    }

    @GetMapping
    public ResponseEntity<Page<Task>> getAll(
            @RequestParam(required = false) TaskStatus status,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String q,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate dueBefore,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate dueAfter,
            @PageableDefault(size = 10) Pageable pageable
    ) {
        return ResponseEntity.ok(
                taskRepository.search(
                        status,
                        categoryId,
                        q,
                        dueBefore,
                        dueAfter,
                        pageable
                )
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<Task> getById(@PathVariable Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task id=" + id + " not found"));
        return ResponseEntity.ok(task);
    }

    @PostMapping
    public ResponseEntity<Task> create(@Valid @RequestBody TaskRequest req) {
        Task task = new Task();
        applyRequest(task, req);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(taskRepository.save(task));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Task> update(@Valid @PathVariable Long id, @RequestBody TaskRequest req) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task id=" + id + " not found"));
        applyRequest(task, req);
        return ResponseEntity.ok(taskRepository.save(task));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!taskRepository.existsById(id)) {
            throw new ResourceNotFoundException("Task id=" + id + " not found");
        }
        taskRepository.deleteById(id);
        return ResponseEntity.noContent().build();
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
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Category id=" + req.categoryId + " not found"));
            task.setCategory(cat);
        }
    }
}
