package pl.taskmanager.taskmanager.entity;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import java.time.LocalDate;
import java.util.Set;
import pl.taskmanager.taskmanager.dto.*;

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

        assertThat(task)
            .returns("T", Task::getTitle)
            .returns("D", Task::getDescription)
            .returns(cat, Task::getCategory)
            .returns(user, Task::getUser);
        assertThat(task.getDueDate()).isNotNull();
        assertThat(task.getTags()).hasSize(1);
    }

    @Test
    void testUser() {
        User user = new User();
        user.setUsername("u");
        user.setPassword("p");
        user.setEmail("e");
        user.setRoles(Set.of("R"));
        
        assertThat(user)
            .returns("u", User::getUsername)
            .returns("p", User::getPassword)
            .returns("e", User::getEmail)
            .returns(Set.of("R"), User::getRoles)
            .returns(null, User::getId);
    }

    @Test
    void testCategory() {
        Category cat = new Category("N", "C");
        cat.setName("NN");
        cat.setColor("CC");
        User user = new User();
        cat.setUser(user);

        assertThat(cat)
            .returns("NN", Category::getName)
            .returns("CC", Category::getColor)
            .returns(user, Category::getUser);
    }

    @Test
    void testTag() {
        Tag tag = new Tag();
        tag.setId(1L);
        tag.setName("Tag");
        assertThat(tag)
            .returns(1L, Tag::getId)
            .returns("Tag", Tag::getName);
    }

    @Test
    void testTaskStatsResponse() {
        TaskStatsResponse stats = new TaskStatsResponse(10L, 5L, 3L, 2L, 20.0);
        assertThat(stats)
            .returns(10L, s -> s.total)
            .returns(5L, s -> s.todo)
            .returns(3L, s -> s.inProgress)
            .returns(2L, s -> s.done)
            .returns(20.0, s -> s.percentDone);
    }

    @Test
    void testTaskRequest() {
        TaskRequest req = new TaskRequest();
        req.title = "T";
        req.description = "D";
        req.status = TaskStatus.DONE;
        req.dueDate = LocalDate.now();
        req.categoryId = 1L;
        
        assertThat(req)
            .returns("T", r -> r.title)
            .returns("D", r -> r.description)
            .returns(TaskStatus.DONE, r -> r.status)
            .returns(1L, r -> r.categoryId);
        assertThat(req.dueDate).isNotNull();
    }

    @Test
    void testCategoryRequest() {
        CategoryRequest req = new CategoryRequest();
        req.name = "N";
        req.color = "#ffffff";
        assertThat(req)
            .returns("N", r -> r.name)
            .returns("#ffffff", r -> r.color);
    }

    @Test
    void testRegisterRequest() {
        RegisterRequest req = new RegisterRequest();
        req.setUsername("u");
        req.setPassword("p");
        req.setEmail("e@e.com");
        assertThat(req)
            .returns("u", RegisterRequest::getUsername)
            .returns("p", RegisterRequest::getPassword)
            .returns("e@e.com", RegisterRequest::getEmail);
    }
}
