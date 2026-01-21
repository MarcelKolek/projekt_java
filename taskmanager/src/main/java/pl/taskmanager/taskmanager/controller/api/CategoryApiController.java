package pl.taskmanager.taskmanager.controller.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.taskmanager.taskmanager.entity.Category;
import pl.taskmanager.taskmanager.exception.ResourceNotFoundException;
import pl.taskmanager.taskmanager.repository.CategoryRepository;

import java.util.List;

@RestController
@RequestMapping("/api/v1/categories")
public class CategoryApiController {

    private final CategoryRepository categoryRepository;

    public CategoryApiController(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @GetMapping
    public ResponseEntity<List<Category>> getAll() {
        return ResponseEntity.ok(categoryRepository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Category> getById(@PathVariable Long id) {
        Category cat = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category id=" + id + " not found"));
        return ResponseEntity.ok(cat);
    }

    @PostMapping
    public ResponseEntity<Category> create(@RequestBody Category category) {
        Category saved = categoryRepository.save(category);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Category> update(@PathVariable Long id, @RequestBody Category input) {
        Category cat = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category id=" + id + " not found"));

        cat.setName(input.getName());
        cat.setColor(input.getColor());

        Category saved = categoryRepository.save(cat);
        return ResponseEntity.ok(saved);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new ResourceNotFoundException("Category id=" + id + " not found");
        }
        categoryRepository.deleteById(id);
        return ResponseEntity.noContent().build(); // 204
    }
}
