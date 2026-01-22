error id: file://<WORKSPACE>/taskmanager/src/main/java/pl/taskmanager/taskmanager/controller/api/TaskApiController.java:_empty_/ResourceNotFoundException#
file://<WORKSPACE>/taskmanager/src/main/java/pl/taskmanager/taskmanager/controller/api/TaskApiController.java
empty definition using pc, found symbol in pc: _empty_/ResourceNotFoundException#
empty definition using semanticdb
empty definition using fallback
non-local guesses:

offset: 2837
uri: file://<WORKSPACE>/taskmanager/src/main/java/pl/taskmanager/taskmanager/controller/api/TaskApiController.java
text:
```scala
package pl.taskmanager.taskmanager.controller.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.taskmanager.taskmanager.dto.TaskRequest;
import pl.taskmanager.taskmanager.entity.Category;
import pl.taskmanager.taskmanager.entity.Task;
import pl.taskmanager.taskmanager.exception.ResourceNotFoundException;
import pl.taskmanager.taskmanager.repository.CategoryRepository;
import pl.taskmanager.taskmanager.repository.TaskRepository;

import java.util.List;

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
    public ResponseEntity<List<Task>> getAll() {
        return ResponseEntity.ok(taskRepository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Task> getById(@PathVariable Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task id=" + id + " not found"));
        return ResponseEntity.ok(task);
    }

    @PostMapping
    public ResponseEntity<Task> create(@RequestBody TaskRequest req) {
        Task task = new Task();
        applyRequest(task, req);
        Task saved = taskRepository.save(task);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Task> update(@PathVariable Long id, @RequestBody TaskRequest req) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task id=" + id + " not found"));
        applyRequest(task, req);
        Task saved = taskRepository.save(task);
        return ResponseEntity.ok(saved);
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
                    .orElseThrow(() -> new ResourceNotFoun@@dException("Category id=" + req.categoryId + " not found"));
            task.setCategory(cat);
        }
    }
}

```


#### Short summary: 

empty definition using pc, found symbol in pc: _empty_/ResourceNotFoundException#