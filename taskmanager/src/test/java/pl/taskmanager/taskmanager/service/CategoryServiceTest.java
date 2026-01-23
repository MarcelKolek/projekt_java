package pl.taskmanager.taskmanager.service;

class CategoryServiceTest {

    @org.mockito.Mock
    private pl.taskmanager.taskmanager.repository.CategoryRepository categoryRepository;

    @org.mockito.Mock
    private pl.taskmanager.taskmanager.service.UserService userService;

    @org.mockito.Mock
    private pl.taskmanager.taskmanager.dao.TaskJdbcDao taskJdbcDao;

    @org.mockito.InjectMocks
    private pl.taskmanager.taskmanager.service.CategoryService categoryService;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        org.mockito.MockitoAnnotations.openMocks(this);
    }

    @org.junit.jupiter.api.Test
    void shouldCreateCategory() {
        pl.taskmanager.taskmanager.dto.CategoryRequest req = new pl.taskmanager.taskmanager.dto.CategoryRequest();
        req.name = "Test";
        req.color = "#ffffff";

        pl.taskmanager.taskmanager.entity.User user = new pl.taskmanager.taskmanager.entity.User();
        user.setUsername("user");

        org.mockito.Mockito.when(userService.findByUsername("user")).thenReturn(user);

        pl.taskmanager.taskmanager.entity.Category saved = new pl.taskmanager.taskmanager.entity.Category("Test", "#ffffff");
        saved.setUser(user);
        org.mockito.Mockito.when(categoryRepository.save(org.mockito.ArgumentMatchers.any(pl.taskmanager.taskmanager.entity.Category.class))).thenReturn(saved);

        pl.taskmanager.taskmanager.dto.CategoryResponse result = categoryService.create(req, "user");

        org.assertj.core.api.Assertions.assertThat(result.name).isEqualTo("Test");
        org.mockito.Mockito.verify(categoryRepository, org.mockito.Mockito.times(1)).save(org.mockito.ArgumentMatchers.any(pl.taskmanager.taskmanager.entity.Category.class));
    }

    @org.junit.jupiter.api.Test
    void shouldDeleteCategoryAndClearTasks() {
        Long catId = 1L;
        pl.taskmanager.taskmanager.entity.User user = new pl.taskmanager.taskmanager.entity.User();
        user.setId(10L);
        user.setUsername("user");

        org.mockito.Mockito.when(userService.findByUsername("user")).thenReturn(user);

        pl.taskmanager.taskmanager.entity.Category cat = new pl.taskmanager.taskmanager.entity.Category("Test", "#ffffff");
        cat.setUser(user);
        org.mockito.Mockito.when(categoryRepository.findById(catId)).thenReturn(java.util.Optional.of(cat));

        categoryService.delete(catId, "user");

        org.mockito.Mockito.verify(taskJdbcDao, org.mockito.Mockito.times(1)).clearCategoryForTasks(catId);
        org.mockito.Mockito.verify(categoryRepository, org.mockito.Mockito.times(1)).deleteById(catId);
    }

    @org.junit.jupiter.api.Test
    void shouldUpdateCategory() {
        Long catId = 1L;
        pl.taskmanager.taskmanager.entity.User user = new pl.taskmanager.taskmanager.entity.User();
        user.setId(10L);
        user.setUsername("user");

        org.mockito.Mockito.when(userService.findByUsername("user")).thenReturn(user);

        pl.taskmanager.taskmanager.entity.Category cat = new pl.taskmanager.taskmanager.entity.Category("Old", "#000000");
        cat.setId(catId);
        cat.setUser(user);
        org.mockito.Mockito.when(categoryRepository.findById(catId)).thenReturn(java.util.Optional.of(cat));
        org.mockito.Mockito.when(categoryRepository.save(org.mockito.ArgumentMatchers.any(pl.taskmanager.taskmanager.entity.Category.class))).thenReturn(cat);

        pl.taskmanager.taskmanager.dto.CategoryRequest req = new pl.taskmanager.taskmanager.dto.CategoryRequest();
        req.name = "New";
        req.color = "#ffffff";

        categoryService.update(catId, req, "user");

        org.assertj.core.api.Assertions.assertThat(cat.getName()).isEqualTo("New");
        org.mockito.Mockito.verify(categoryRepository).save(cat);
    }

    @org.junit.jupiter.api.Test
    void shouldThrowWhenUpdateStrangerCategory() {
        Long catId = 1L;
        pl.taskmanager.taskmanager.entity.User owner = new pl.taskmanager.taskmanager.entity.User();
        owner.setId(10L);
        owner.setUsername("owner");

        pl.taskmanager.taskmanager.entity.User stranger = new pl.taskmanager.taskmanager.entity.User();
        stranger.setId(20L);
        stranger.setUsername("stranger");

        org.mockito.Mockito.when(userService.findByUsername("stranger")).thenReturn(stranger);

        pl.taskmanager.taskmanager.entity.Category cat = new pl.taskmanager.taskmanager.entity.Category("Test", "#000000");
        cat.setUser(owner);
        org.mockito.Mockito.when(categoryRepository.findById(catId)).thenReturn(java.util.Optional.of(cat));

        org.assertj.core.api.Assertions.assertThatThrownBy(() -> categoryService.update(catId, new pl.taskmanager.taskmanager.dto.CategoryRequest(), "stranger"))
                .isInstanceOf(pl.taskmanager.taskmanager.exception.ResourceNotFoundException.class);
    }

    @org.junit.jupiter.api.Test
    void shouldGetAllCategories() {
        pl.taskmanager.taskmanager.entity.User user = new pl.taskmanager.taskmanager.entity.User();
        user.setUsername("user");

        org.mockito.Mockito.when(userService.findByUsername("user")).thenReturn(user);
        org.mockito.Mockito.when(categoryRepository.findAllByUser(user)).thenReturn(java.util.List.of(new pl.taskmanager.taskmanager.entity.Category()));
        
        java.util.List<pl.taskmanager.taskmanager.dto.CategoryResponse> result = categoryService.getAll("user");
        org.assertj.core.api.Assertions.assertThat(result).hasSize(1);
    }

    @org.junit.jupiter.api.Test
    void shouldGetCategoryById() {
        Long catId = 1L;
        pl.taskmanager.taskmanager.entity.User user = new pl.taskmanager.taskmanager.entity.User();
        user.setId(10L);
        user.setUsername("user");

        org.mockito.Mockito.when(userService.findByUsername("user")).thenReturn(user);

        pl.taskmanager.taskmanager.entity.Category cat = new pl.taskmanager.taskmanager.entity.Category("Work", "#ff0000");
        cat.setUser(user);
        org.mockito.Mockito.when(categoryRepository.findById(catId)).thenReturn(java.util.Optional.of(cat));

        pl.taskmanager.taskmanager.dto.CategoryResponse result = categoryService.getById(catId, "user");
        org.assertj.core.api.Assertions.assertThat(result.name).isEqualTo("Work");
    }
}
