package pl.taskmanager.taskmanager.controller.api;

@io.swagger.v3.oas.annotations.tags.Tag(name = "Categories", description = "Operacje na kategoriach zadań")
@org.springframework.web.bind.annotation.RestController
@org.springframework.web.bind.annotation.RequestMapping("/api/v1/categories")
public class CategoryApiController {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CategoryApiController.class);

    private final pl.taskmanager.taskmanager.service.CategoryService categoryService;

    public CategoryApiController(pl.taskmanager.taskmanager.service.CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @io.swagger.v3.oas.annotations.Operation(
            summary = "Lista kategorii",
            description = "Zwraca listę wszystkich kategorii zadań zalogowanego użytkownika."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lista kategorii zwrócona poprawnie")
    })
    @org.springframework.web.bind.annotation.GetMapping
    public org.springframework.http.ResponseEntity<java.util.List<pl.taskmanager.taskmanager.dto.CategoryResponse>> getAll(@org.springframework.security.core.annotation.AuthenticationPrincipal org.springframework.security.core.userdetails.UserDetails userDetails) {
        log.debug("GET /api/v1/categories for user={}", userDetails.getUsername());
        return org.springframework.http.ResponseEntity.ok(categoryService.getAll(userDetails.getUsername()));
    }

    @io.swagger.v3.oas.annotations.Operation(
            summary = "Pobierz kategorię po ID",
            description = "Zwraca kategorię o podanym ID dla zalogowanego użytkownika."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Kategoria znaleziona"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Kategoria nie istnieje", content = @io.swagger.v3.oas.annotations.media.Content)
    })
    @org.springframework.web.bind.annotation.GetMapping("/{id}")
    public org.springframework.http.ResponseEntity<pl.taskmanager.taskmanager.dto.CategoryResponse> getById(
            @io.swagger.v3.oas.annotations.Parameter(description = "ID kategorii", example = "1")
            @org.springframework.web.bind.annotation.PathVariable java.lang.Long id,
            @org.springframework.security.core.annotation.AuthenticationPrincipal org.springframework.security.core.userdetails.UserDetails userDetails
    ) {
        return org.springframework.http.ResponseEntity.ok(categoryService.getById(id, userDetails.getUsername()));
    }

    @io.swagger.v3.oas.annotations.Operation(
            summary = "Utwórz nową kategorię",
            description = "Tworzy nową kategorię dla zalogowanego użytkownika."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Kategoria utworzona"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Błąd walidacji danych", content = @io.swagger.v3.oas.annotations.media.Content)
    })
    @org.springframework.web.bind.annotation.PostMapping
    public org.springframework.http.ResponseEntity<pl.taskmanager.taskmanager.dto.CategoryResponse> create(
            @jakarta.validation.Valid @org.springframework.web.bind.annotation.RequestBody pl.taskmanager.taskmanager.dto.CategoryRequest req,
            @org.springframework.security.core.annotation.AuthenticationPrincipal org.springframework.security.core.userdetails.UserDetails userDetails
    ) {
        log.info("Creating category: {} for user: {}", req.name, userDetails.getUsername());
        pl.taskmanager.taskmanager.dto.CategoryResponse saved = categoryService.create(req, userDetails.getUsername());
        return org.springframework.http.ResponseEntity.status(org.springframework.http.HttpStatus.CREATED).body(saved);
    }

    @io.swagger.v3.oas.annotations.Operation(
            summary = "Edytuj kategorię",
            description = "Aktualizuje nazwę i kolor kategorii dla zalogowanego użytkownika."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Kategoria zaktualizowana"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Błąd walidacji danych", content = @io.swagger.v3.oas.annotations.media.Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Kategoria nie istnieje", content = @io.swagger.v3.oas.annotations.media.Content)
    })
    @org.springframework.web.bind.annotation.PutMapping("/{id}")
    public org.springframework.http.ResponseEntity<pl.taskmanager.taskmanager.dto.CategoryResponse> update(
            @io.swagger.v3.oas.annotations.Parameter(description = "ID kategorii", example = "1")
            @org.springframework.web.bind.annotation.PathVariable java.lang.Long id,
            @jakarta.validation.Valid @org.springframework.web.bind.annotation.RequestBody pl.taskmanager.taskmanager.dto.CategoryRequest req,
            @org.springframework.security.core.annotation.AuthenticationPrincipal org.springframework.security.core.userdetails.UserDetails userDetails
    ) {
        log.info("Updating category id: {} for user: {}", id, userDetails.getUsername());
        return org.springframework.http.ResponseEntity.ok(categoryService.update(id, req, userDetails.getUsername()));
    }

    @io.swagger.v3.oas.annotations.Operation(
            summary = "Usuń kategorię",
            description = "Usuwa kategorię dla zalogowanego użytkownika."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "Kategoria usunięta"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Kategoria nie istnieje", content = @io.swagger.v3.oas.annotations.media.Content)
    })
    @org.springframework.web.bind.annotation.DeleteMapping("/{id}")
    public org.springframework.http.ResponseEntity<java.lang.Void> delete(
            @io.swagger.v3.oas.annotations.Parameter(description = "ID kategorii", example = "1")
            @org.springframework.web.bind.annotation.PathVariable java.lang.Long id,
            @org.springframework.security.core.annotation.AuthenticationPrincipal org.springframework.security.core.userdetails.UserDetails userDetails
    ) {
        log.info("Deleting category id: {} for user: {}", id, userDetails.getUsername());
        categoryService.delete(id, userDetails.getUsername());
        return org.springframework.http.ResponseEntity.noContent().build();
    }
}
