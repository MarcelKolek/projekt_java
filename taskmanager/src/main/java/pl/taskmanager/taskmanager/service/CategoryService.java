package pl.taskmanager.taskmanager.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.taskmanager.taskmanager.dto.CategoryRequest;
import pl.taskmanager.taskmanager.entity.Category;
import pl.taskmanager.taskmanager.exception.ResourceNotFoundException;
import pl.taskmanager.taskmanager.repository.CategoryRepository;
import pl.taskmanager.taskmanager.repository.TaskRepository;

import java.util.List;

@Service
public class CategoryService {

    private static final Logger log = LoggerFactory.getLogger(CategoryService.class);

    private final CategoryRepository categoryRepository;
    private final TaskRepository taskRepository;

    public CategoryService(CategoryRepository categoryRepository, TaskRepository taskRepository) {
        this.categoryRepository = categoryRepository;
        this.taskRepository = taskRepository;
    }

    @Transactional(readOnly = true)
    public List<Category> getAll(pl.taskmanager.taskmanager.entity.User user) {
        log.debug("Fetching all categories for user={}", user.getUsername());
        return categoryRepository.findAllByUser(user);
    }

    @Transactional(readOnly = true)
    public Category getById(Long id, pl.taskmanager.taskmanager.entity.User user) {
        log.debug("Fetching category id={} for user={}", id, user.getUsername());
        Category c = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category id=" + id + " not found"));
        if (!c.getUser().getId().equals(user.getId())) {
            throw new ResourceNotFoundException("Category id=" + id + " not found");
        }
        return c;
    }

    @Transactional
    public Category create(CategoryRequest req, pl.taskmanager.taskmanager.entity.User user) {
        log.info("Creating category name={} for user={}", req.name, user.getUsername());

        Category c = new Category();
        c.setName(req.name);
        c.setColor(normalizeHex(req.color));
        c.setUser(user);

        return categoryRepository.save(c);
    }

    @Transactional
    public Category update(Long id, CategoryRequest req, pl.taskmanager.taskmanager.entity.User user) {
        log.info("Updating category id={} for user={}", id, user.getUsername());

        Category c = getById(id, user);
        c.setName(req.name);
        c.setColor(normalizeHex(req.color));

        return categoryRepository.save(c);
    }

    @Transactional
    public void delete(Long id, pl.taskmanager.taskmanager.entity.User user) {
        log.info("Deleting category id={} for user={}", id, user.getUsername());

        getById(id, user);

        // po usunięciu kategorii category_id = NULL
        int updated = taskRepository.clearCategoryForTasks(id);
        log.info("Cleared category for {} tasks (categoryId={})", updated, id);

        categoryRepository.deleteById(id);
    }

    private String normalizeHex(String color) {
        return color == null ? null : color.trim().toLowerCase();
    }
}
