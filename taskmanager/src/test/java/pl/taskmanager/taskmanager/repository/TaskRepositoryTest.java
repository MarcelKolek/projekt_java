package pl.taskmanager.taskmanager.repository;

@org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
class TaskRepositoryTest {

    @org.springframework.beans.factory.annotation.Autowired
    private TaskRepository taskRepository;

    @org.springframework.beans.factory.annotation.Autowired
    private CategoryRepository categoryRepository;

    @org.springframework.beans.factory.annotation.Autowired
    private UserRepository userRepository;

    @org.springframework.beans.factory.annotation.Autowired
    private jakarta.persistence.EntityManager entityManager;

    private pl.taskmanager.taskmanager.entity.User testUser;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        testUser = new pl.taskmanager.taskmanager.entity.User();
        testUser.setUsername("testuser");
        testUser.setPassword("pass");
        testUser = userRepository.save(testUser);
    }

    @org.junit.jupiter.api.Test
    void shouldSaveTask() {
        pl.taskmanager.taskmanager.entity.Task task = new pl.taskmanager.taskmanager.entity.Task();
        task.setTitle("Test Task");
        task.setStatus(pl.taskmanager.taskmanager.entity.TaskStatus.TODO);
        task.setUser(testUser);
        pl.taskmanager.taskmanager.entity.Task saved = taskRepository.save(task);
        org.assertj.core.api.Assertions.assertThat(saved.getId()).isNotNull();
    }

    @org.junit.jupiter.api.Test
    void shouldFindById() {
        pl.taskmanager.taskmanager.entity.Task task = new pl.taskmanager.taskmanager.entity.Task();
        task.setTitle("Find Me");
        task.setStatus(pl.taskmanager.taskmanager.entity.TaskStatus.TODO);
        task.setUser(testUser);
        pl.taskmanager.taskmanager.entity.Task saved = taskRepository.save(task);
        java.util.Optional<pl.taskmanager.taskmanager.entity.Task> found = taskRepository.findById(saved.getId());
        org.assertj.core.api.Assertions.assertThat(found).isPresent();
        org.assertj.core.api.Assertions.assertThat(found.get().getTitle()).isEqualTo("Find Me");
    }

    @org.junit.jupiter.api.Test
    void shouldUpdateTask() {
        pl.taskmanager.taskmanager.entity.Task task = new pl.taskmanager.taskmanager.entity.Task();
        task.setTitle("Old Title");
        task.setStatus(pl.taskmanager.taskmanager.entity.TaskStatus.TODO);
        task.setUser(testUser);
        pl.taskmanager.taskmanager.entity.Task saved = taskRepository.save(task);
        saved.setTitle("New Title");
        taskRepository.save(saved);
        pl.taskmanager.taskmanager.entity.Task updated = taskRepository.findById(saved.getId()).get();
        org.assertj.core.api.Assertions.assertThat(updated.getTitle()).isEqualTo("New Title");
    }

    @org.junit.jupiter.api.Test
    void shouldDeleteTask() {
        pl.taskmanager.taskmanager.entity.Task task = new pl.taskmanager.taskmanager.entity.Task();
        task.setTitle("Delete Me");
        task.setStatus(pl.taskmanager.taskmanager.entity.TaskStatus.TODO);
        task.setUser(testUser);
        pl.taskmanager.taskmanager.entity.Task saved = taskRepository.save(task);
        taskRepository.deleteById(saved.getId());
        org.assertj.core.api.Assertions.assertThat(taskRepository.findById(saved.getId())).isEmpty();
    }

    @org.junit.jupiter.api.Test
    void shouldClearCategoryForTasks() {
        pl.taskmanager.taskmanager.entity.Category cat = new pl.taskmanager.taskmanager.entity.Category("Work", "#ff0000");
        cat.setUser(testUser);
        cat = categoryRepository.save(cat);
        pl.taskmanager.taskmanager.entity.Task task = new pl.taskmanager.taskmanager.entity.Task();
        task.setTitle("Task with Cat");
        task.setStatus(pl.taskmanager.taskmanager.entity.TaskStatus.TODO);
        task.setCategory(cat);
        task.setUser(testUser);
        taskRepository.save(task);

        int updated = taskRepository.clearCategoryForTasks(cat.getId());
        org.assertj.core.api.Assertions.assertThat(updated).isEqualTo(1);
        
        taskRepository.flush();
        entityManager.clear();
        pl.taskmanager.taskmanager.entity.Task updatedTask = taskRepository.findAll().get(0);
        org.assertj.core.api.Assertions.assertThat(updatedTask.getCategory()).isNull();
    }
    
    @org.junit.jupiter.api.Test
    void shouldSaveCategory() {
        pl.taskmanager.taskmanager.entity.Category cat = new pl.taskmanager.taskmanager.entity.Category("Home", "#00ff00");
        cat.setUser(testUser);
        pl.taskmanager.taskmanager.entity.Category saved = categoryRepository.save(cat);
        org.assertj.core.api.Assertions.assertThat(saved.getId()).isNotNull();
    }

    @org.junit.jupiter.api.Test
    void shouldFindCategoryById() {
        pl.taskmanager.taskmanager.entity.Category cat = new pl.taskmanager.taskmanager.entity.Category("Shop", "#0000ff");
        cat.setUser(testUser);
        pl.taskmanager.taskmanager.entity.Category saved = categoryRepository.save(cat);
        org.assertj.core.api.Assertions.assertThat(categoryRepository.findById(saved.getId())).isPresent();
    }

    @org.junit.jupiter.api.Test
    void shouldUpdateCategory() {
        pl.taskmanager.taskmanager.entity.Category cat = new pl.taskmanager.taskmanager.entity.Category("Old", "#111111");
        cat.setUser(testUser);
        pl.taskmanager.taskmanager.entity.Category saved = categoryRepository.save(cat);
        saved.setName("New");
        categoryRepository.save(saved);
        org.assertj.core.api.Assertions.assertThat(categoryRepository.findById(saved.getId()).get().getName()).isEqualTo("New");
    }

    @org.junit.jupiter.api.Test
    void shouldDeleteCategory() {
        pl.taskmanager.taskmanager.entity.Category cat = new pl.taskmanager.taskmanager.entity.Category("Tmp", "#222222");
        cat.setUser(testUser);
        pl.taskmanager.taskmanager.entity.Category saved = categoryRepository.save(cat);
        categoryRepository.deleteById(saved.getId());
        org.assertj.core.api.Assertions.assertThat(categoryRepository.findById(saved.getId())).isEmpty();
    }

    @org.junit.jupiter.api.Test
    void shouldListAllTasks() {
        pl.taskmanager.taskmanager.entity.Task t1 = new pl.taskmanager.taskmanager.entity.Task(); t1.setTitle("T1"); t1.setStatus(pl.taskmanager.taskmanager.entity.TaskStatus.TODO); t1.setUser(testUser);
        pl.taskmanager.taskmanager.entity.Task t2 = new pl.taskmanager.taskmanager.entity.Task(); t2.setTitle("T2"); t2.setStatus(pl.taskmanager.taskmanager.entity.TaskStatus.TODO); t2.setUser(testUser);
        taskRepository.save(t1);
        taskRepository.save(t2);
        org.assertj.core.api.Assertions.assertThat(taskRepository.findAll()).hasSize(2);
    }

    @org.junit.jupiter.api.Test
    void shouldSearchTasksByStatus() {
        pl.taskmanager.taskmanager.entity.Task t1 = new pl.taskmanager.taskmanager.entity.Task(); t1.setTitle("T1"); t1.setStatus(pl.taskmanager.taskmanager.entity.TaskStatus.TODO); t1.setUser(testUser);
        pl.taskmanager.taskmanager.entity.Task t2 = new pl.taskmanager.taskmanager.entity.Task(); t2.setTitle("T2"); t2.setStatus(pl.taskmanager.taskmanager.entity.TaskStatus.DONE); t2.setUser(testUser);
        taskRepository.save(t1);
        taskRepository.save(t2);
        
        org.springframework.data.domain.Page<pl.taskmanager.taskmanager.entity.Task> result = taskRepository.search(
            testUser.getUsername(), pl.taskmanager.taskmanager.entity.TaskStatus.TODO, null, null, null, null, org.springframework.data.domain.PageRequest.of(0, 10)
        );
        org.assertj.core.api.Assertions.assertThat(result.getContent()).hasSize(1);
        org.assertj.core.api.Assertions.assertThat(result.getContent().get(0).getTitle()).isEqualTo("T1");
    }

    @org.junit.jupiter.api.Test
    void shouldSearchTasksByQuery() {
        pl.taskmanager.taskmanager.entity.Task t1 = new pl.taskmanager.taskmanager.entity.Task(); t1.setTitle("Apple"); t1.setStatus(pl.taskmanager.taskmanager.entity.TaskStatus.TODO); t1.setUser(testUser);
        pl.taskmanager.taskmanager.entity.Task t2 = new pl.taskmanager.taskmanager.entity.Task(); t2.setTitle("Banana"); t2.setStatus(pl.taskmanager.taskmanager.entity.TaskStatus.TODO); t2.setUser(testUser);
        taskRepository.save(t1);
        taskRepository.save(t2);
        
        org.springframework.data.domain.Page<pl.taskmanager.taskmanager.entity.Task> result = taskRepository.search(
            testUser.getUsername(), null, null, "apple", null, null, org.springframework.data.domain.PageRequest.of(0, 10)
        );
        org.assertj.core.api.Assertions.assertThat(result.getContent()).hasSize(1);
        org.assertj.core.api.Assertions.assertThat(result.getContent().get(0).getTitle()).isEqualTo("Apple");
    }
}
