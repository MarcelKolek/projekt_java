package pl.taskmanager.taskmanager.service;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import pl.taskmanager.taskmanager.dao.TaskJdbcDao;
import pl.taskmanager.taskmanager.dto.CategoryRequest;
import pl.taskmanager.taskmanager.dto.CategoryResponse;
import pl.taskmanager.taskmanager.entity.Category;
import pl.taskmanager.taskmanager.entity.User;
import pl.taskmanager.taskmanager.exception.ResourceNotFoundException;
import pl.taskmanager.taskmanager.repository.CategoryRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private UserService userService;

    @Mock
    private TaskJdbcDao taskJdbcDao;

    @InjectMocks
    private CategoryService categoryService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldCreateCategory() {
        CategoryRequest req = new CategoryRequest();
        req.name = "Test";
        req.color = "#ffffff";

        User user = new User();
        user.setUsername("user");

        when(userService.findByUsername("user")).thenReturn(user);

        Category saved = new Category("Test", "#ffffff");
        saved.setUser(user);
        when(categoryRepository.save(any(Category.class))).thenReturn(saved);

        CategoryResponse result = categoryService.create(req, "user");

        assertThat(result.name).isEqualTo("Test");
        verify(categoryRepository, times(1)).save(any(Category.class));
    }

    @Test
    void shouldDeleteCategoryAndClearTasks() {
        Long catId = 1L;

        User user = new User();
        user.setId(10L);
        user.setUsername("user");

        when(userService.findByUsername("user")).thenReturn(user);

        Category cat = new Category("Test", "#ffffff");
        cat.setUser(user);
        when(categoryRepository.findById(catId)).thenReturn(Optional.of(cat));

        categoryService.delete(catId, "user");

        verify(taskJdbcDao, times(1)).clearCategoryForTasks(catId);
        verify(categoryRepository, times(1)).deleteById(catId);
    }

    @Test
    void shouldUpdateCategory() {
        Long catId = 1L;

        User user = new User();
        user.setId(10L);
        user.setUsername("user");

        when(userService.findByUsername("user")).thenReturn(user);

        Category cat = new Category("Old", "#000000");
        cat.setId(catId);
        cat.setUser(user);

        when(categoryRepository.findById(catId)).thenReturn(Optional.of(cat));
        when(categoryRepository.save(any(Category.class))).thenReturn(cat);

        CategoryRequest req = new CategoryRequest();
        req.name = "New";
        req.color = "#ffffff";

        categoryService.update(catId, req, "user");

        assertThat(cat.getName()).isEqualTo("New");
        verify(categoryRepository).save(cat);
    }

    @Test
    void shouldThrowWhenUpdateStrangerCategory() {
        Long catId = 1L;

        User owner = new User();
        owner.setId(10L);
        owner.setUsername("owner");

        User stranger = new User();
        stranger.setId(20L);
        stranger.setUsername("stranger");

        when(userService.findByUsername("stranger")).thenReturn(stranger);

        Category cat = new Category("Test", "#000000");
        cat.setUser(owner);
        when(categoryRepository.findById(catId)).thenReturn(Optional.of(cat));

        assertThatThrownBy(() -> categoryService.update(catId, new CategoryRequest(), "stranger"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void shouldGetAllCategories() {
        User user = new User();
        user.setUsername("user");

        when(userService.findByUsername("user")).thenReturn(user);
        when(categoryRepository.findAllByUser(user)).thenReturn(List.of(new Category()));

        List<CategoryResponse> result = categoryService.getAll("user");

        assertThat(result).hasSize(1);
    }

    @Test
    void shouldGetCategoryById() {
        Long catId = 1L;

        User user = new User();
        user.setId(10L);
        user.setUsername("user");

        when(userService.findByUsername("user")).thenReturn(user);

        Category cat = new Category("Work", "#ff0000");
        cat.setUser(user);
        when(categoryRepository.findById(catId)).thenReturn(Optional.of(cat));

        CategoryResponse result = categoryService.getById(catId, "user");

        assertThat(result.name).isEqualTo("Work");
    }
}
