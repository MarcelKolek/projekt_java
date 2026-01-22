package pl.taskmanager.taskmanager.entity;

import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class EntityTest {

    @Test
    void testTask() {
        Task task = new Task();
        task.setTitle("T");
        task.setDescription("D");
        task.setDueDate(LocalDate.now());
        Category cat = new Category();
        task.setCategory(cat);
        User user = new User();
        task.setUser(user);
        Tag tag = new Tag();
        task.setTags(Set.of(tag));

        assertThat(task.getTitle()).isEqualTo("T");
        assertThat(task.getDescription()).isEqualTo("D");
        assertThat(task.getDueDate()).isNotNull();
        assertThat(task.getCategory()).isEqualTo(cat);
        assertThat(task.getUser()).isEqualTo(user);
        assertThat(task.getTags()).hasSize(1);
    }

    @Test
    void testUser() {
        User user = new User();
        user.setUsername("u");
        user.setPassword("p");
        user.setEmail("e");
        user.setRoles(Set.of("R"));
        
        assertThat(user.getUsername()).isEqualTo("u");
        assertThat(user.getPassword()).isEqualTo("p");
        assertThat(user.getEmail()).isEqualTo("e");
        assertThat(user.getRoles()).contains("R");
        assertThat(user.getId()).isNull();
    }

    @Test
    void testCategory() {
        Category cat = new Category("N", "C");
        cat.setName("NN");
        cat.setColor("CC");
        User user = new User();
        cat.setUser(user);

        assertThat(cat.getName()).isEqualTo("NN");
        assertThat(cat.getColor()).isEqualTo("CC");
        assertThat(cat.getUser()).isEqualTo(user);
    }

    @Test
    void testTag() {
        Tag tag = new Tag();
        tag.setId(1L);
        tag.setName("Tag");
        assertThat(tag.getId()).isEqualTo(1L);
        assertThat(tag.getName()).isEqualTo("Tag");
    }

    @Test
    void testTaskStatsResponse() {
        pl.taskmanager.taskmanager.dto.TaskStatsResponse stats = new pl.taskmanager.taskmanager.dto.TaskStatsResponse(10, 5, 3, 2, 20.0);
        assertThat(stats.total).isEqualTo(10);
        assertThat(stats.todo).isEqualTo(5);
        assertThat(stats.inProgress).isEqualTo(3);
        assertThat(stats.done).isEqualTo(2);
        assertThat(stats.percentDone).isEqualTo(20.0);
    }

    @Test
    void testTaskRequest() {
        pl.taskmanager.taskmanager.dto.TaskRequest req = new pl.taskmanager.taskmanager.dto.TaskRequest();
        req.title = "T";
        req.description = "D";
        req.status = TaskStatus.DONE;
        req.dueDate = LocalDate.now();
        req.categoryId = 1L;
        assertThat(req.title).isEqualTo("T");
        assertThat(req.description).isEqualTo("D");
        assertThat(req.status).isEqualTo(TaskStatus.DONE);
        assertThat(req.dueDate).isNotNull();
        assertThat(req.categoryId).isEqualTo(1L);
    }

    @Test
    void testCategoryRequest() {
        pl.taskmanager.taskmanager.dto.CategoryRequest req = new pl.taskmanager.taskmanager.dto.CategoryRequest();
        req.name = "N";
        req.color = "#ffffff";
        assertThat(req.name).isEqualTo("N");
        assertThat(req.color).isEqualTo("#ffffff");
    }

    @Test
    void testRegisterRequest() {
        pl.taskmanager.taskmanager.dto.RegisterRequest req = new pl.taskmanager.taskmanager.dto.RegisterRequest();
        req.setUsername("u");
        req.setPassword("p");
        req.setEmail("e@e.com");
        assertThat(req.getUsername()).isEqualTo("u");
        assertThat(req.getPassword()).isEqualTo("p");
        assertThat(req.getEmail()).isEqualTo("e@e.com");
    }
}
