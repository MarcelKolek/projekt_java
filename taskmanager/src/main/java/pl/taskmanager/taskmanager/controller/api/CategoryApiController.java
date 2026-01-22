package pl.taskmanager.taskmanager.controller.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.taskmanager.taskmanager.dto.CategoryRequest;
import pl.taskmanager.taskmanager.entity.Category;
import pl.taskmanager.taskmanager.service.CategoryService;

import java.util.List;

@Tag(name = "Categories", description = "Operacje na kategoriach zadań")
@RestController
@RequestMapping("/api/v1/categories")
public class CategoryApiController {

    private final CategoryService categoryService;

    public CategoryApiController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @Operation(
            summary = "Lista kategorii",
            description = "Zwraca listę wszystkich kategorii zadań."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista kategorii zwrócona poprawnie")
    })
    @GetMapping
    public ResponseEntity<List<Category>> getAll() {
        return ResponseEntity.ok(categoryService.getAll());
    }

    @Operation(
            summary = "Pobierz kategorię po ID",
            description = "Zwraca kategorię o podanym ID."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Kategoria znaleziona"),
            @ApiResponse(responseCode = "404", description = "Kategoria nie istnieje", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<Category> getById(
            @Parameter(description = "ID kategorii", example = "1")
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(categoryService.getById(id));
    }

    @Operation(
            summary = "Utwórz nową kategorię",
            description = "Tworzy nową kategorię z nazwą i kolorem."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Kategoria utworzona"),
            @ApiResponse(responseCode = "400", description = "Błąd walidacji danych", content = @Content)
    })
    @PostMapping
    public ResponseEntity<Category> create(@Valid @RequestBody CategoryRequest req) {
        Category saved = categoryService.create(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @Operation(
            summary = "Edytuj kategorię",
            description = "Aktualizuje nazwę i kolor kategorii."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Kategoria zaktualizowana"),
            @ApiResponse(responseCode = "400", description = "Błąd walidacji danych", content = @Content),
            @ApiResponse(responseCode = "404", description = "Kategoria nie istnieje", content = @Content)
    })
    @PutMapping("/{id}")
    public ResponseEntity<Category> update(
            @Parameter(description = "ID kategorii", example = "1")
            @PathVariable Long id,
            @Valid @RequestBody CategoryRequest req
    ) {
        return ResponseEntity.ok(categoryService.update(id, req));
    }

    @Operation(
            summary = "Usuń kategorię",
            description = "Usuwa kategorię. Zadania powiązane z tą kategorią mają ustawione categoryId = NULL."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Kategoria usunięta"),
            @ApiResponse(responseCode = "404", description = "Kategoria nie istnieje", content = @Content)
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @Parameter(description = "ID kategorii", example = "1")
            @PathVariable Long id
    ) {
        categoryService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
