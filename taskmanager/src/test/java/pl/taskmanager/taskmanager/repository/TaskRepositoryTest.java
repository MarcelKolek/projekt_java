package pl.taskmanager.taskmanager.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import pl.taskmanager.taskmanager.entity.Category;
import pl.taskmanager.taskmanager.entity.Task;
import pl.taskmanager.taskmanager.entity.TaskStatus;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class TaskRepositoryTest {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private jakarta.persistence.EntityManager entityManager;

    private pl.taskmanager.taskmanager.entity.User testUser;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        testUser = new pl.taskmanager.taskmanager.entity.User();
        testUser.setUsername("testuser");
        testUser.setPassword("pass");
        testUser = userRepository.save(testUser);
    }

    @Test
    void shouldSaveTask() {
        Task task = new Task();
        task.setTitle("Test Task");
        task.setStatus(TaskStatus.TODO);
        task.setUser(testUser);
        Task saved = taskRepository.save(task);
        assertThat(saved.getId()).isNotNull();
    }

    @Test
    void shouldFindById() {
        Task task = new Task();
        task.setTitle("Find Me");
        task.setStatus(TaskStatus.TODO);
        task.setUser(testUser);
        Task saved = taskRepository.save(task);
        Optional<Task> found = taskRepository.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getTitle()).isEqualTo("Find Me");
    }

    @Test
    void shouldUpdateTask() {
        Task task = new Task();
        task.setTitle("Old Title");
        task.setStatus(TaskStatus.TODO);
        task.setUser(testUser);
        Task saved = taskRepository.save(task);
        saved.setTitle("New Title");
        taskRepository.save(saved);
        Task updated = taskRepository.findById(saved.getId()).get();
        assertThat(updated.getTitle()).isEqualTo("New Title");
    }

    @Test
    void shouldDeleteTask() {
        Task task = new Task();
        task.setTitle("Delete Me");
        task.setStatus(TaskStatus.TODO);
        task.setUser(testUser);
        Task saved = taskRepository.save(task);
        taskRepository.deleteById(saved.getId());
        assertThat(taskRepository.findById(saved.getId())).isEmpty();
    }

    @Test
    void shouldClearCategoryForTasks() {
        Category cat = new Category("Work", "#ff0000");
        cat.setUser(testUser);
        cat = categoryRepository.save(cat);
        Task task = new Task();
        task.setTitle("Task with Cat");
        task.setStatus(TaskStatus.TODO);
        task.setCategory(cat);
        task.setUser(testUser);
        taskRepository.save(task);

        int updated = taskRepository.clearCategoryForTasks(cat.getId());
        assertThat(updated).isEqualTo(1);
        
        // Trzeba wyczyścić sesję JPA aby zobaczyć zmiany w bazie (albo użyć refresh)
        taskRepository.flush();
        entityManager.clear();
        // Clear is not available directly on repo, but repo.saveAll will do it too or just refetch
        Task updatedTask = taskRepository.findAll().get(0);
        assertThat(updatedTask.getCategory()).isNull();
        // Note: clearing via @Query doesn't update managed entities in memory automatically
    }
    
    @Test
    void shouldSaveCategory() {
        Category cat = new Category("Home", "#00ff00");
        cat.setUser(testUser);
        Category saved = categoryRepository.save(cat);
        assertThat(saved.getId()).isNotNull();
    }

    @Test
    void shouldFindCategoryById() {
        Category cat = new Category("Shop", "#0000ff");
        cat.setUser(testUser);
        Category saved = categoryRepository.save(cat);
        assertThat(categoryRepository.findById(saved.getId())).isPresent();
    }

    @Test
    void shouldUpdateCategory() {
        Category cat = new Category("Old", "#111111");
        cat.setUser(testUser);
        Category saved = categoryRepository.save(cat);
        saved.setName("New");
        categoryRepository.save(saved);
        assertThat(categoryRepository.findById(saved.getId()).get().getName()).isEqualTo("New");
    }

    @Test
    void shouldDeleteCategory() {
        Category cat = new Category("Tmp", "#222222");
        cat.setUser(testUser);
        Category saved = categoryRepository.save(cat);
        categoryRepository.deleteById(saved.getId());
        assertThat(categoryRepository.findById(saved.getId())).isEmpty();
    }

    @Test
    void shouldListAllTasks() {
        Task t1 = new Task(); t1.setTitle("T1"); t1.setStatus(TaskStatus.TODO); t1.setUser(testUser);
        Task t2 = new Task(); t2.setTitle("T2"); t2.setStatus(TaskStatus.TODO); t2.setUser(testUser);
        taskRepository.save(t1);
        taskRepository.save(t2);
        assertThat(taskRepository.findAll()).hasSize(2);
    }
}
