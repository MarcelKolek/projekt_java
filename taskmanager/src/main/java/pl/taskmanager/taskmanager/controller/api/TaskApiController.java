package pl.taskmanager.taskmanager.controller.api;

@io.swagger.v3.oas.annotations.tags.Tag(name = "Tasks", description = "Operacje na zadaniach")
@org.springframework.web.bind.annotation.RestController
@org.springframework.web.bind.annotation.RequestMapping("/api/v1/tasks")
public class TaskApiController {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TaskApiController.class);

    private final pl.taskmanager.taskmanager.service.TaskService taskService;
    private final pl.taskmanager.taskmanager.service.CsvService csvService;
    private final pl.taskmanager.taskmanager.service.PdfService pdfService;
    private final pl.taskmanager.taskmanager.service.FileService fileService;

    public TaskApiController(
            pl.taskmanager.taskmanager.service.TaskService taskService,
            pl.taskmanager.taskmanager.service.CsvService csvService,
            pl.taskmanager.taskmanager.service.PdfService pdfService,
            pl.taskmanager.taskmanager.service.FileService fileService
    ) {
        this.taskService = taskService;
        this.csvService = csvService;
        this.pdfService = pdfService;
        this.fileService = fileService;
    }

    @io.swagger.v3.oas.annotations.Operation(
            summary = "Lista zadań (paginacja + filtry + wyszukiwanie)",
            description = "Zwraca paginowaną listę zadań zalogowanego użytkownika. Obsługuje filtry: status, categoryId, dueBefore/dueAfter oraz wyszukiwanie po tytule (q). " +
                    "Sortowanie i paginacja przez standardowe parametry Spring: page, size, sort."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lista zadań zwrócona poprawnie"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Błędne parametry zapytania", content = @io.swagger.v3.oas.annotations.media.Content)
    })
    @org.springframework.web.bind.annotation.GetMapping
    public org.springframework.http.ResponseEntity<org.springframework.data.domain.Page<pl.taskmanager.taskmanager.dto.TaskResponse>> getAll(
            @io.swagger.v3.oas.annotations.Parameter(description = "Status zadania: TODO, IN_PROGRESS, DONE")
            @org.springframework.web.bind.annotation.RequestParam(required = false) pl.taskmanager.taskmanager.entity.TaskStatus status,

            @io.swagger.v3.oas.annotations.Parameter(description = "ID kategorii")
            @org.springframework.web.bind.annotation.RequestParam(required = false) java.lang.Long categoryId,

            @io.swagger.v3.oas.annotations.Parameter(description = "Wyszukiwanie po tytule (LIKE, case-insensitive)")
            @org.springframework.web.bind.annotation.RequestParam(required = false) java.lang.String q,

            @io.swagger.v3.oas.annotations.Parameter(description = "Tylko zadania z dueDate < dueBefore (format yyyy-MM-dd)")
            @org.springframework.web.bind.annotation.RequestParam(required = false)
            @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE)
            java.time.LocalDate dueBefore,

            @io.swagger.v3.oas.annotations.Parameter(description = "Tylko zadania z dueDate > dueAfter (format yyyy-MM-dd)")
            @org.springframework.web.bind.annotation.RequestParam(required = false)
            @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE)
            java.time.LocalDate dueAfter,

            @io.swagger.v3.oas.annotations.Parameter(hidden = true)
            @org.springframework.data.web.PageableDefault(size = 10) org.springframework.data.domain.Pageable pageable,

            @org.springframework.security.core.annotation.AuthenticationPrincipal org.springframework.security.core.userdetails.UserDetails userDetails
    ) {
        log.debug("GET /api/v1/tasks username={}, status={}, categoryId={}, q={}", userDetails.getUsername(), status, categoryId, q);
        return org.springframework.http.ResponseEntity.ok(
                taskService.list(userDetails.getUsername(), status, categoryId, q, dueBefore, dueAfter, pageable)
        );
    }

    @io.swagger.v3.oas.annotations.Operation(summary = "Pobierz zadanie po ID")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Zadanie znalezione"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Zadanie nie istnieje", content = @io.swagger.v3.oas.annotations.media.Content)
    })
    @org.springframework.web.bind.annotation.GetMapping("/{id}")
    public org.springframework.http.ResponseEntity<pl.taskmanager.taskmanager.dto.TaskResponse> getById(
            @io.swagger.v3.oas.annotations.Parameter(description = "ID zadania", example = "1")
            @org.springframework.web.bind.annotation.PathVariable java.lang.Long id,
            @org.springframework.security.core.annotation.AuthenticationPrincipal org.springframework.security.core.userdetails.UserDetails userDetails
    ) {
        pl.taskmanager.taskmanager.dto.TaskResponse task = taskService.getById(id, userDetails.getUsername());
        return org.springframework.http.ResponseEntity.ok(task);
    }

    @io.swagger.v3.oas.annotations.Operation(summary = "Utwórz nowe zadanie")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Zadanie utworzone"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Błąd walidacji / błędne dane", content = @io.swagger.v3.oas.annotations.media.Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Nie znaleziono kategorii (categoryId)", content = @io.swagger.v3.oas.annotations.media.Content)
    })
    @org.springframework.web.bind.annotation.PostMapping(consumes = {"application/json", "multipart/form-data"})
    public org.springframework.http.ResponseEntity<pl.taskmanager.taskmanager.dto.TaskResponse> create(
            @jakarta.validation.Valid @org.springframework.web.bind.annotation.RequestPart("task") pl.taskmanager.taskmanager.dto.TaskRequest req,
            @org.springframework.web.bind.annotation.RequestPart(value = "file", required = false) org.springframework.web.multipart.MultipartFile file,
            @org.springframework.security.core.annotation.AuthenticationPrincipal org.springframework.security.core.userdetails.UserDetails userDetails
    ) throws java.io.IOException {
        log.info("Creating new task: {} for user: {}", req.title, userDetails.getUsername());
        pl.taskmanager.taskmanager.dto.TaskResponse saved = taskService.create(req, userDetails.getUsername());

        if (file != null && !file.isEmpty()) {
            saved = taskService.updateWithFile(saved.id, fileService.storeFile(file, saved.id), userDetails.getUsername());
        }

        return org.springframework.http.ResponseEntity.status(org.springframework.http.HttpStatus.CREATED).body(saved);
    }

    @io.swagger.v3.oas.annotations.Operation(summary = "Edytuj zadanie (lub zmień status)")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Zadanie zaktualizowane"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Błąd walidacji / błędne dane", content = @io.swagger.v3.oas.annotations.media.Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Zadanie lub kategoria nie istnieje", content = @io.swagger.v3.oas.annotations.media.Content)
    })
    @org.springframework.web.bind.annotation.PutMapping(value = "/{id}", consumes = {"application/json", "multipart/form-data"})
    public org.springframework.http.ResponseEntity<pl.taskmanager.taskmanager.dto.TaskResponse> update(
            @io.swagger.v3.oas.annotations.Parameter(description = "ID zadania", example = "1")
            @org.springframework.web.bind.annotation.PathVariable java.lang.Long id,
            @jakarta.validation.Valid @org.springframework.web.bind.annotation.RequestPart("task") pl.taskmanager.taskmanager.dto.TaskRequest req,
            @org.springframework.web.bind.annotation.RequestPart(value = "file", required = false) org.springframework.web.multipart.MultipartFile file,
            @org.springframework.security.core.annotation.AuthenticationPrincipal org.springframework.security.core.userdetails.UserDetails userDetails
    ) throws java.io.IOException {
        pl.taskmanager.taskmanager.dto.TaskResponse saved = taskService.update(id, req, userDetails.getUsername());

        if (file != null && !file.isEmpty()) {
            saved = taskService.updateWithFile(saved.id, fileService.storeFile(file, saved.id), userDetails.getUsername());
        }

        return org.springframework.http.ResponseEntity.ok(saved);
    }

    @io.swagger.v3.oas.annotations.Operation(summary = "Usuń zadanie")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "Zadanie usunięte"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Zadanie nie istnieje", content = @io.swagger.v3.oas.annotations.media.Content)
    })
    @org.springframework.web.bind.annotation.DeleteMapping("/{id}")
    public org.springframework.http.ResponseEntity<java.lang.Void> delete(
            @io.swagger.v3.oas.annotations.Parameter(description = "ID zadania", example = "1")
            @org.springframework.web.bind.annotation.PathVariable java.lang.Long id,
            @org.springframework.security.core.annotation.AuthenticationPrincipal org.springframework.security.core.userdetails.UserDetails userDetails
    ) {
        taskService.delete(id, userDetails.getUsername());
        return org.springframework.http.ResponseEntity.noContent().build();
    }

    @io.swagger.v3.oas.annotations.Operation(summary = "Eksport wszystkich zadań do CSV", description = "Zwraca plik tasks.csv ze wszystkimi zadaniami zalogowanego użytkownika.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Plik CSV zwrócony poprawnie")
    })
    @org.springframework.web.bind.annotation.GetMapping("/export/csv")
    public org.springframework.http.ResponseEntity<byte[]> exportCsv(
            @org.springframework.security.core.annotation.AuthenticationPrincipal org.springframework.security.core.userdetails.UserDetails userDetails
    ) {
        byte[] bytes = csvService.exportTasksToCsv(taskService.findAllByUser(userDetails.getUsername()));
        return org.springframework.http.ResponseEntity.ok()
                .header(org.springframework.http.HttpHeaders.CONTENT_TYPE, "text/csv; charset=utf-8")
                .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"tasks.csv\"")
                .body(bytes);
    }

    @io.swagger.v3.oas.annotations.Operation(summary = "Eksport wszystkich zadań do PDF", description = "Zwraca plik tasks.pdf ze wszystkimi zadaniami zalogowanego użytkownika.")
    @org.springframework.web.bind.annotation.GetMapping("/export/pdf")
    public org.springframework.http.ResponseEntity<byte[]> exportPdf(
            @org.springframework.security.core.annotation.AuthenticationPrincipal org.springframework.security.core.userdetails.UserDetails userDetails
    ) throws java.io.IOException {
        byte[] bytes = pdfService.exportTasksToPdf(
                taskService.findAllByUser(userDetails.getUsername()),
                userDetails.getUsername()
        );
        return org.springframework.http.ResponseEntity.ok()
                .header(org.springframework.http.HttpHeaders.CONTENT_TYPE, "application/pdf")
                .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"tasks.pdf\"")
                .body(bytes);
    }

    @io.swagger.v3.oas.annotations.Operation(
            summary = "Statystyki zadań (dashboard)",
            description = "Zwraca liczniki zalogowanego użytkownika: total/TODO/IN_PROGRESS/DONE oraz procent wykonania (DONE). Liczone przez DAO JdbcTemplate w warstwie serwisu."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Statystyki zwrócone poprawnie")
    })
    @org.springframework.web.bind.annotation.GetMapping("/stats")
    public org.springframework.http.ResponseEntity<pl.taskmanager.taskmanager.dto.TaskStatsResponse> stats(
            @org.springframework.security.core.annotation.AuthenticationPrincipal org.springframework.security.core.userdetails.UserDetails userDetails
    ) {
        return org.springframework.http.ResponseEntity.ok(taskService.getStats(userDetails.getUsername()));
    }

    @io.swagger.v3.oas.annotations.Operation(summary = "Upload pliku", description = "Przykładowy upload pliku na serwer.")
    @org.springframework.web.bind.annotation.PostMapping(value = "/upload", consumes = "multipart/form-data")
    public org.springframework.http.ResponseEntity<java.lang.String> uploadFile(
            @org.springframework.web.bind.annotation.RequestParam("file") org.springframework.web.multipart.MultipartFile file,
            @org.springframework.web.bind.annotation.RequestParam("taskId") java.lang.Long taskId,
            @org.springframework.security.core.annotation.AuthenticationPrincipal org.springframework.security.core.userdetails.UserDetails userDetails
    ) throws java.io.IOException {
        if (file.isEmpty()) {
            return org.springframework.http.ResponseEntity.badRequest().body("Plik jest pusty");
        }

        java.lang.String storedFilename = fileService.storeFile(file, taskId);
        taskService.updateWithFile(taskId, storedFilename, userDetails.getUsername());

        return org.springframework.http.ResponseEntity.ok("Plik zapisany: " + storedFilename);
    }

    @io.swagger.v3.oas.annotations.Operation(summary = "Pobierz plik", description = "Pobiera wcześniej wgrany plik z serwera.")
    @org.springframework.web.bind.annotation.GetMapping("/download/{filename}")
    public org.springframework.http.ResponseEntity<org.springframework.core.io.Resource> downloadFile(
            @org.springframework.web.bind.annotation.PathVariable java.lang.String filename
    ) throws java.io.IOException {
        org.springframework.core.io.Resource resource = fileService.loadFileAsResource(filename);
        return org.springframework.http.ResponseEntity.ok()
                .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

    @io.swagger.v3.oas.annotations.Operation(summary = "Usuń plik", description = "Usuwa załącznik z zadania.")
    @org.springframework.web.bind.annotation.DeleteMapping("/{id}/attachment")
    public org.springframework.http.ResponseEntity<java.lang.Void> deleteAttachment(
            @org.springframework.web.bind.annotation.PathVariable java.lang.Long id,
            @org.springframework.security.core.annotation.AuthenticationPrincipal org.springframework.security.core.userdetails.UserDetails userDetails
    ) throws java.io.IOException {
        pl.taskmanager.taskmanager.dto.TaskResponse task = taskService.getById(id, userDetails.getUsername());
        if (task.attachmentFilename != null) {
            fileService.deleteFile(task.attachmentFilename);
            taskService.updateWithFile(id, null, userDetails.getUsername());
        }
        return org.springframework.http.ResponseEntity.noContent().build();
    }
}
