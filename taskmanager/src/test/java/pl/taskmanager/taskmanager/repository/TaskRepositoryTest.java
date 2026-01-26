package pl.taskmanager.taskmanager.repository;

import java.util.Optional;

import jakarta.persistence.EntityManager;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import pl.taskmanager.taskmanager.entity.Category;
import pl.taskmanager.taskmanager.entity.Task;
import pl.taskmanager.taskmanager.entity.TaskStatus;
import pl.taskmanager.taskmanager.entity.User;

@DataJpaTest
class TaskRepositoryTest {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EntityManager entityManager;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
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

        Assertions.assertThat(saved.getId()).isNotNull();
    }

    @Test
    void shouldFindById() {
        Task task = new Task();
        task.setTitle("Find Me");
        task.setStatus(TaskStatus.TODO);
        task.setUser(testUser);

        Task saved = taskRepository.save(task);

        Optional<Task> found = taskRepository.findById(saved.getId());

        Assertions.assertThat(found).isPresent();
        Assertions.assertThat(found.get().getTitle()).isEqualTo("Find Me");
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

        Assertions.assertThat(updated.getTitle()).isEqualTo("New Title");
    }

    @Test
    void shouldDeleteTask() {
        Task task = new Task();
        task.setTitle("Delete Me");
        task.setStatus(TaskStatus.TODO);
        task.setUser(testUser);

        Task saved = taskRepository.save(task);

        taskRepository.deleteById(saved.getId());

        Assertions.assertThat(taskRepository.findById(saved.getId())).isEmpty();
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
        Assertions.assertThat(updated).isEqualTo(1);

        taskRepository.flush();
        entityManager.clear();

        Task updatedTask = taskRepository.findAll().get(0);
        Assertions.assertThat(updatedTask.getCategory()).isNull();
    }

    @Test
    void shouldSaveCategory() {
        Category cat = new Category("Home", "#00ff00");
        cat.setUser(testUser);

        Category saved = categoryRepository.save(cat);

        Assertions.assertThat(saved.getId()).isNotNull();
    }

    @Test
    void shouldFindCategoryById() {
        Category cat = new Category("Shop", "#0000ff");
        cat.setUser(testUser);

        Category saved = categoryRepository.save(cat);

        Assertions.assertThat(categoryRepository.findById(saved.getId())).isPresent();
    }

    @Test
    void shouldUpdateCategory() {
        Category cat = new Category("Old", "#111111");
        cat.setUser(testUser);

        Category saved = categoryRepository.save(cat);

        saved.setName("New");
        categoryRepository.save(saved);

        Assertions.assertThat(categoryRepository.findById(saved.getId()).get().getName())
                .isEqualTo("New");
    }

    @Test
    void shouldDeleteCategory() {
        Category cat = new Category("Tmp", "#222222");
        cat.setUser(testUser);

        Category saved = categoryRepository.save(cat);

        categoryRepository.deleteById(saved.getId());

        Assertions.assertThat(categoryRepository.findById(saved.getId())).isEmpty();
    }

    @Test
    void shouldListAllTasks() {
        Task t1 = new Task();
        t1.setTitle("T1");
        t1.setStatus(TaskStatus.TODO);
        t1.setUser(testUser);

        Task t2 = new Task();
        t2.setTitle("T2");
        t2.setStatus(TaskStatus.TODO);
        t2.setUser(testUser);

        taskRepository.save(t1);
        taskRepository.save(t2);

        Assertions.assertThat(taskRepository.findAll()).hasSize(2);
    }

    @Test
    void shouldSearchTasksByStatus() {
        Task t1 = new Task();
        t1.setTitle("T1");
        t1.setStatus(TaskStatus.TODO);
        t1.setUser(testUser);

        Task t2 = new Task();
        t2.setTitle("T2");
        t2.setStatus(TaskStatus.DONE);
        t2.setUser(testUser);

        taskRepository.save(t1);
        taskRepository.save(t2);

        Page<Task> result = taskRepository.search(
                testUser.getUsername(),
                TaskStatus.TODO,
                null,
                null,
                null,
                null,
                PageRequest.of(0, 10)
        );

        Assertions.assertThat(result.getContent()).hasSize(1);
        Assertions.assertThat(result.getContent().get(0).getTitle()).isEqualTo("T1");
    }

    @Test
    void shouldSearchTasksByQuery() {
        Task t1 = new Task();
        t1.setTitle("Apple");
        t1.setStatus(TaskStatus.TODO);
        t1.setUser(testUser);

        Task t2 = new Task();
        t2.setTitle("Banana");
        t2.setStatus(TaskStatus.TODO);
        t2.setUser(testUser);

        taskRepository.save(t1);
        taskRepository.save(t2);

        Page<Task> result = taskRepository.search(
                testUser.getUsername(),
                null,
                null,
                "apple",
                null,
                null,
                PageRequest.of(0, 10)
        );

        Assertions.assertThat(result.getContent()).hasSize(1);
        Assertions.assertThat(result.getContent().get(0).getTitle()).isEqualTo("Apple");
    }
}
