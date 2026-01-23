package pl.taskmanager.taskmanager.service;

@org.springframework.stereotype.Service
public class CategoryService {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CategoryService.class);

    private final pl.taskmanager.taskmanager.repository.CategoryRepository categoryRepository;
    private final pl.taskmanager.taskmanager.service.UserService userService;
    private final pl.taskmanager.taskmanager.dao.TaskJdbcDao taskJdbcDao;

    public CategoryService(pl.taskmanager.taskmanager.repository.CategoryRepository categoryRepository, pl.taskmanager.taskmanager.service.UserService userService, pl.taskmanager.taskmanager.dao.TaskJdbcDao taskJdbcDao) {
        this.categoryRepository = categoryRepository;
        this.userService = userService;
        this.taskJdbcDao = taskJdbcDao;
    }

    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public java.util.List<pl.taskmanager.taskmanager.dto.CategoryResponse> getAll(String username) {
        log.debug("Fetching all categories for user={}", username);
        pl.taskmanager.taskmanager.entity.User user = userService.findByUsername(username);
        return categoryRepository.findAllByUser(user).stream()
                .map(pl.taskmanager.taskmanager.dto.CategoryResponse::new)
                .collect(java.util.stream.Collectors.toList());
    }

    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public pl.taskmanager.taskmanager.dto.CategoryResponse getById(Long id, String username) {
        log.debug("Fetching category id={} for user={}", id, username);
        pl.taskmanager.taskmanager.entity.User user = userService.findByUsername(username);
        return new pl.taskmanager.taskmanager.dto.CategoryResponse(getCategoryEntity(id, user));
    }

    @org.springframework.transaction.annotation.Transactional
    public pl.taskmanager.taskmanager.dto.CategoryResponse create(pl.taskmanager.taskmanager.dto.CategoryRequest req, String username) {
        log.info("Creating category name={} for user={}", req.name, username);
        pl.taskmanager.taskmanager.entity.User user = userService.findByUsername(username);

        pl.taskmanager.taskmanager.entity.Category c = new pl.taskmanager.taskmanager.entity.Category();
        c.setName(req.name);
        c.setColor(normalizeHex(req.color));
        c.setUser(user);

        return new pl.taskmanager.taskmanager.dto.CategoryResponse(categoryRepository.save(c));
    }

    @org.springframework.transaction.annotation.Transactional
    public pl.taskmanager.taskmanager.dto.CategoryResponse update(Long id, pl.taskmanager.taskmanager.dto.CategoryRequest req, String username) {
        log.info("Updating category id={} for user={}", id, username);
        pl.taskmanager.taskmanager.entity.User user = userService.findByUsername(username);

        pl.taskmanager.taskmanager.entity.Category c = getCategoryEntity(id, user);
        c.setName(req.name);
        c.setColor(normalizeHex(req.color));

        return new pl.taskmanager.taskmanager.dto.CategoryResponse(categoryRepository.save(c));
    }

    @org.springframework.transaction.annotation.Transactional
    public void delete(Long id, String username) {
        log.info("Deleting category id={} for user={}", id, username);
        pl.taskmanager.taskmanager.entity.User user = userService.findByUsername(username);
        getCategoryEntity(id, user);

        // po usunięciu kategorii category_id = NULL z JdbcTemplate
        int updated = taskJdbcDao.clearCategoryForTasks(id);
        log.info("Cleared category for {} tasks (categoryId={})", updated, id);

        categoryRepository.deleteById(id);
    }

    public pl.taskmanager.taskmanager.entity.Category getCategoryEntity(Long id, pl.taskmanager.taskmanager.entity.User user) {
        pl.taskmanager.taskmanager.entity.Category c = categoryRepository.findById(id)
                .orElseThrow(() -> new pl.taskmanager.taskmanager.exception.ResourceNotFoundException("Category id=" + id + " not found"));
        if (!c.getUser().getId().equals(user.getId())) {
            throw new pl.taskmanager.taskmanager.exception.ResourceNotFoundException("Category id=" + id + " not found");
        }
        return c;
    }

    private String normalizeHex(String color) {
        return color == null ? null : color.trim().toLowerCase();
    }
}
