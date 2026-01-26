package pl.taskmanager.taskmanager.controller.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import pl.taskmanager.taskmanager.dto.CategoryRequest;
import pl.taskmanager.taskmanager.dto.CategoryResponse;
import pl.taskmanager.taskmanager.service.CategoryService;

import java.util.List;

@Tag(name = "Categories", description = "Operacje na kategoriach zadań")
@RestController
@RequestMapping("/api/v1/categories")
public class CategoryApiController {

    private static final Logger log = LoggerFactory.getLogger(CategoryApiController.class);

    private final CategoryService categoryService;

    public CategoryApiController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @Operation(
            summary = "Lista kategorii",
            description = "Zwraca listę wszystkich kategorii zadań zalogowanego użytkownika."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista kategorii zwrócona poprawnie")
    })
    @GetMapping
    public ResponseEntity<List<CategoryResponse>> getAll(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        log.debug("GET /api/v1/categories for user={}", userDetails.getUsername());
        return ResponseEntity.ok(categoryService.getAll(userDetails.getUsername()));
    }

    @Operation(
            summary = "Pobierz kategorię po ID",
            description = "Zwraca kategorię o podanym ID dla zalogowanego użytkownika."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Kategoria znaleziona"),
            @ApiResponse(responseCode = "404", description = "Kategoria nie istnieje", content = @io.swagger.v3.oas.annotations.media.Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponse> getById(
            @Parameter(description = "ID kategorii", example = "1")
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ResponseEntity.ok(categoryService.getById(id, userDetails.getUsername()));
    }

    @Operation(
            summary = "Utwórz nową kategorię",
            description = "Tworzy nową kategorię dla zalogowanego użytkownika."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Kategoria utworzona"),
            @ApiResponse(responseCode = "400", description = "Błąd walidacji danych", content = @io.swagger.v3.oas.annotations.media.Content)
    })
    @PostMapping
    public ResponseEntity<CategoryResponse> create(
            @Valid @RequestBody CategoryRequest req,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        log.info("Creating category: {} for user: {}", req.name, userDetails.getUsername());
        CategoryResponse saved = categoryService.create(req, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @Operation(
            summary = "Edytuj kategorię",
            description = "Aktualizuje nazwę i kolor kategorii dla zalogowanego użytkownika."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Kategoria zaktualizowana"),
            @ApiResponse(responseCode = "400", description = "Błąd walidacji danych", content = @io.swagger.v3.oas.annotations.media.Content),
            @ApiResponse(responseCode = "404", description = "Kategoria nie istnieje", content = @io.swagger.v3.oas.annotations.media.Content)
    })
    @PutMapping("/{id}")
    public ResponseEntity<CategoryResponse> update(
            @Parameter(description = "ID kategorii", example = "1")
            @PathVariable Long id,
            @Valid @RequestBody CategoryRequest req,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        log.info("Updating category id: {} for user: {}", id, userDetails.getUsername());
        return ResponseEntity.ok(categoryService.update(id, req, userDetails.getUsername()));
    }

    @Operation(
            summary = "Usuń kategorię",
            description = "Usuwa kategorię dla zalogowanego użytkownika."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Kategoria usunięta"),
            @ApiResponse(responseCode = "404", description = "Kategoria nie istnieje", content = @io.swagger.v3.oas.annotations.media.Content)
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @Parameter(description = "ID kategorii", example = "1")
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        log.info("Deleting category id: {} for user: {}", id, userDetails.getUsername());
        categoryService.delete(id, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }
}
