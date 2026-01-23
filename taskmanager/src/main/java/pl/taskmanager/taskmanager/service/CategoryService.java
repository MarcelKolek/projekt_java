package pl.taskmanager.taskmanager.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.taskmanager.taskmanager.dto.CategoryRequest;
import pl.taskmanager.taskmanager.dto.CategoryResponse;
import pl.taskmanager.taskmanager.entity.Category;
import pl.taskmanager.taskmanager.entity.User;
import pl.taskmanager.taskmanager.exception.ResourceNotFoundException;
import pl.taskmanager.taskmanager.repository.CategoryRepository;
import pl.taskmanager.taskmanager.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryService {

    private static final Logger log = LoggerFactory.getLogger(CategoryService.class);

    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final pl.taskmanager.taskmanager.dao.TaskJdbcDao taskJdbcDao;

    public CategoryService(CategoryRepository categoryRepository, UserRepository userRepository, pl.taskmanager.taskmanager.dao.TaskJdbcDao taskJdbcDao) {
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
        this.taskJdbcDao = taskJdbcDao;
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> getAll(String username) {
        log.debug("Fetching all categories for user={}", username);
        User user = findUser(username);
        return categoryRepository.findAllByUser(user).stream()
                .map(CategoryResponse::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CategoryResponse getById(Long id, String username) {
        log.debug("Fetching category id={} for user={}", id, username);
        User user = findUser(username);
        return new CategoryResponse(getCategoryEntity(id, user));
    }

    @Transactional
    public CategoryResponse create(CategoryRequest req, String username) {
        log.info("Creating category name={} for user={}", req.name, username);
        User user = findUser(username);

        Category c = new Category();
        c.setName(req.name);
        c.setColor(normalizeHex(req.color));
        c.setUser(user);

        return new CategoryResponse(categoryRepository.save(c));
    }

    @Transactional
    public CategoryResponse update(Long id, CategoryRequest req, String username) {
        log.info("Updating category id={} for user={}", id, username);
        User user = findUser(username);

        Category c = getCategoryEntity(id, user);
        c.setName(req.name);
        c.setColor(normalizeHex(req.color));

        return new CategoryResponse(categoryRepository.save(c));
    }

    @Transactional
    public void delete(Long id, String username) {
        log.info("Deleting category id={} for user={}", id, username);
        User user = findUser(username);
        getCategoryEntity(id, user);

        // po usunięciu kategorii category_id = NULL z JdbcTemplate
        int updated = taskJdbcDao.clearCategoryForTasks(id);
        log.info("Cleared category for {} tasks (categoryId={})", updated, id);

        categoryRepository.deleteById(id);
    }

    public Category getCategoryEntity(Long id, User user) {
        Category c = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category id=" + id + " not found"));
        if (!c.getUser().getId().equals(user.getId())) {
            throw new ResourceNotFoundException("Category id=" + id + " not found");
        }
        return c;
    }

    private User findUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new org.springframework.security.core.userdetails.UsernameNotFoundException("User not found: " + username));
    }

    private String normalizeHex(String color) {
        return color == null ? null : color.trim().toLowerCase();
    }
}
