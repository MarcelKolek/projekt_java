error id: file://<WORKSPACE>/taskmanager/src/main/java/pl/taskmanager/taskmanager/controller/api/CategoryApiController.java:_empty_/CategoryService#getAll#
file://<WORKSPACE>/taskmanager/src/main/java/pl/taskmanager/taskmanager/controller/api/CategoryApiController.java
empty definition using pc, found symbol in pc: _empty_/CategoryService#getAll#
empty definition using semanticdb
empty definition using fallback
non-local guesses:

offset: 807
uri: file://<WORKSPACE>/taskmanager/src/main/java/pl/taskmanager/taskmanager/controller/api/CategoryApiController.java
text:
```scala
package pl.taskmanager.taskmanager.controller.api;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.taskmanager.taskmanager.dto.CategoryRequest;
import pl.taskmanager.taskmanager.entity.Category;
import pl.taskmanager.taskmanager.service.CategoryService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/categories")
public class CategoryApiController {

    private final CategoryService categoryService;

    public CategoryApiController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public ResponseEntity<List<Category>> getAll() {
        return ResponseEntity.ok(categoryService.getAl@@l());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Category> getById(@PathVariable Long id) {
        return ResponseEntity.ok(categoryService.getById(id));
    }

    @PostMapping
    public ResponseEntity<Category> create(@Valid @RequestBody CategoryRequest req) {
        Category saved = categoryService.create(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Category> update(@PathVariable Long id, @Valid @RequestBody CategoryRequest req) {
        return ResponseEntity.ok(categoryService.update(id, req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        categoryService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

```


#### Short summary: 

empty definition using pc, found symbol in pc: _empty_/CategoryService#getAll#