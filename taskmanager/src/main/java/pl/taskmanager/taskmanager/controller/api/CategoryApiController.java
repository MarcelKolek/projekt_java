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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import pl.taskmanager.taskmanager.dto.CategoryRequest;
import pl.taskmanager.taskmanager.entity.Category;
import pl.taskmanager.taskmanager.entity.User;
import pl.taskmanager.taskmanager.service.CategoryService;
import pl.taskmanager.taskmanager.service.UserService;

import java.util.List;

@Tag(name = "Categories", description = "Operacje na kategoriach zadań")
@RestController
@RequestMapping("/api/v1/categories")
public class CategoryApiController {

    private final CategoryService categoryService;
    private final UserService userService;

    public CategoryApiController(CategoryService categoryService, UserService userService) {
        this.categoryService = categoryService;
        this.userService = userService;
    }

    @Operation(
            summary = "Lista kategorii",
            description = "Zwraca listę wszystkich kategorii zadań zalogowanego użytkownika."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista kategorii zwrócona poprawnie")
    })
    @GetMapping
    public ResponseEntity<List<Category>> getAll(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.findByUsername(userDetails.getUsername());
        return ResponseEntity.ok(categoryService.getAll(user));
    }

    @Operation(
            summary = "Pobierz kategorię po ID",
            description = "Zwraca kategorię o podanym ID dla zalogowanego użytkownika."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Kategoria znaleziona"),
            @ApiResponse(responseCode = "404", description = "Kategoria nie istnieje", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<Category> getById(
            @Parameter(description = "ID kategorii", example = "1")
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        User user = userService.findByUsername(userDetails.getUsername());
        return ResponseEntity.ok(categoryService.getById(id, user));
    }

    @Operation(
            summary = "Utwórz nową kategorię",
            description = "Tworzy nową kategorię dla zalogowanego użytkownika."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Kategoria utworzona"),
            @ApiResponse(responseCode = "400", description = "Błąd walidacji danych", content = @Content)
    })
    @PostMapping
    public ResponseEntity<Category> create(
            @Valid @RequestBody CategoryRequest req,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        User user = userService.findByUsername(userDetails.getUsername());
        Category saved = categoryService.create(req, user);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @Operation(
            summary = "Edytuj kategorię",
            description = "Aktualizuje nazwę i kolor kategorii dla zalogowanego użytkownika."
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
            @Valid @RequestBody CategoryRequest req,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        User user = userService.findByUsername(userDetails.getUsername());
        return ResponseEntity.ok(categoryService.update(id, req, user));
    }

    @Operation(
            summary = "Usuń kategorię",
            description = "Usuwa kategorię dla zalogowanego użytkownika."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Kategoria usunięta"),
            @ApiResponse(responseCode = "404", description = "Kategoria nie istnieje", content = @Content)
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @Parameter(description = "ID kategorii", example = "1")
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        User user = userService.findByUsername(userDetails.getUsername());
        categoryService.delete(id, user);
        return ResponseEntity.noContent().build();
    }
}
